package com.example.webApplication.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Dto.VoucherFilter;
import com.example.webApplication.Dto.VoucherRequest;
import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;
import com.example.webApplication.Exception.ConflictException;
import com.example.webApplication.Exception.InvalidRequestException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.VoucherRepo;

import jakarta.persistence.criteria.Predicate;

/**
 * Voucher CRUD for admins (proposal 5.2, 6.3, 6.5).
 * Soft delete renames the code to "<code>__deleted_<id>" so the code can be used again;
 * restoring puts the code back when nobody has taken it in the meantime.
 */
@Service
public class VoucherService {

    static final String DELETED_MARK = "__deleted_";
    /** Keeps "code + __deleted_ + id" inside the 50 characters of the column. */
    static final int MAX_CODE_LENGTH = 30;

    private final VoucherRepo voucherRepo;

    public VoucherService(VoucherRepo voucherRepo) {
        this.voucherRepo = voucherRepo;
    }

    @Transactional(readOnly = true)
    public Page<Voucher> search(VoucherFilter filter, Pageable pageable) {
        return voucherRepo.findAll(toSpecification(filter), pageable);
    }

    @Transactional(readOnly = true)
    public Voucher get(long id) {
        return voucherRepo.findById(id).orElseThrow(() -> notFound(id));
    }

    @Transactional
    public Voucher create(VoucherRequest request) {
        validate(request, null);
        Voucher voucher = new Voucher();
        apply(voucher, request);
        if (voucherRepo.existsByCode(voucher.getCode())) {
            throw new ConflictException("Voucher code " + voucher.getCode() + " already exists");
        }
        return voucherRepo.save(voucher);
    }

    @Transactional
    public Voucher update(long id, VoucherRequest request) {
        Voucher voucher = get(id);
        validate(request, voucher);
        String newCode = request.code().trim();
        if (!newCode.equals(voucher.getCode())
                && voucherRepo.findByCode(newCode).filter(other -> other.getId() != id).isPresent()) {
            throw new ConflictException("Voucher code " + newCode + " already exists");
        }
        apply(voucher, request);
        return voucherRepo.save(voucher);
    }

    /** Moves the voucher to the trash. */
    @Transactional
    public void softDelete(long id, String deletedBy) {
        Voucher voucher = get(id);
        voucher.setCode(voucher.getCode() + DELETED_MARK + voucher.getId());
        voucher.setDeletedAt(Instant.now());
        voucher.setDeletedBy(deletedBy);
        voucherRepo.save(voucher);
    }

    @Transactional(readOnly = true)
    public Page<Voucher> trash(Pageable pageable) {
        return voucherRepo.findDeleted(pageable);
    }

    @Transactional
    public Voucher restore(long id) {
        Voucher deleted = voucherRepo.findDeletedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher " + id + " is not in the trash"));
        String original = originalCode(deleted.getCode());
        if (voucherRepo.existsByCode(original)) {
            throw new ConflictException("Code " + original + " is used by another voucher now");
        }
        voucherRepo.restore(id, original);
        return get(id);
    }

    /** Deletes a voucher in the trash for good. */
    @Transactional
    public void purge(long id) {
        voucherRepo.findDeletedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Voucher " + id + " is not in the trash"));
        try {
            voucherRepo.purge(id);
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Voucher " + id + " is used by existing orders and cannot be deleted for good");
        }
    }

    static String originalCode(String deletedCode) {
        int at = deletedCode.lastIndexOf(DELETED_MARK);
        return at < 0 ? deletedCode : deletedCode.substring(0, at);
    }

    private void validate(VoucherRequest r, Voucher existing) {
        List<String> errors = new ArrayList<>();
        if (r.code() == null || r.code().isBlank()) {
            errors.add("code is required");
        } else if (r.code().trim().length() > MAX_CODE_LENGTH) {
            errors.add("code must be at most " + MAX_CODE_LENGTH + " characters");
        }
        if (r.type() == null) {
            errors.add("type is required");
        }
        if (r.discountValue() == null || r.discountValue().signum() < 0) {
            errors.add("discountValue must be zero or more");
        } else if (r.type() == VoucherType.PERCENT && r.discountValue().compareTo(new BigDecimal("100")) > 0) {
            errors.add("discountValue of a percent voucher must be between 0 and 100");
        }
        if (r.maxDiscountAmount() != null && r.maxDiscountAmount().signum() < 0) {
            errors.add("maxDiscountAmount must be zero or more");
        }
        if (r.minOrderAmount() != null && r.minOrderAmount().signum() < 0) {
            errors.add("minOrderAmount must be zero or more");
        }
        if (r.startDate() == null || r.endDate() == null) {
            errors.add("startDate and endDate are required");
        } else if (!r.endDate().isAfter(r.startDate())) {
            errors.add("endDate must be after startDate");
        }
        if (r.usageLimit() == null || r.usageLimit() < 1) {
            errors.add("usageLimit must be at least 1");
        } else if (existing != null && r.usageLimit() < existing.getUsedCount()) {
            errors.add("usageLimit cannot be lower than the " + existing.getUsedCount() + " uses already made");
        }
        if (!errors.isEmpty()) {
            throw new InvalidRequestException(String.join("; ", errors));
        }
    }

    private void apply(Voucher voucher, VoucherRequest r) {
        voucher.setCode(r.code().trim());
        voucher.setType(r.type());
        voucher.setDiscountValue(r.discountValue());
        voucher.setMaxDiscountAmount(r.maxDiscountAmount());
        voucher.setMinOrderAmount(r.minOrderAmount() == null ? BigDecimal.ZERO : r.minOrderAmount());
        voucher.setStartDate(r.startDate());
        voucher.setEndDate(r.endDate());
        voucher.setUsageLimit(r.usageLimit());
        voucher.setActive(r.active() == null || r.active());
    }

    private static Specification<Voucher> toSpecification(VoucherFilter f) {
        return (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            if (f.code() != null && !f.code().isBlank()) {
                ps.add(cb.like(cb.lower(root.get("code")), "%" + f.code().trim().toLowerCase() + "%"));
            }
            if (f.type() != null) {
                ps.add(cb.equal(root.get("type"), f.type()));
            }
            if (f.active() != null) {
                ps.add(cb.equal(root.get("isActive"), f.active()));
            }
            if (f.validFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<Instant>get("endDate"), f.validFrom()));
            }
            if (f.validTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.<Instant>get("startDate"), f.validTo()));
            }
            if (f.minOrderFrom() != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<BigDecimal>get("minOrderAmount"), f.minOrderFrom()));
            }
            if (f.minOrderTo() != null) {
                ps.add(cb.lessThanOrEqualTo(root.<BigDecimal>get("minOrderAmount"), f.minOrderTo()));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
    }

    private static ResourceNotFoundException notFound(long id) {
        return new ResourceNotFoundException("Voucher " + id + " does not exist");
    }
}
