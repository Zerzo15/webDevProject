package com.example.webApplication.Controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.PageResponse;
import com.example.webApplication.Dto.VoucherDto;
import com.example.webApplication.Dto.VoucherFilter;
import com.example.webApplication.Dto.VoucherRequest;
import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherType;
import com.example.webApplication.Service.VoucherService;

/**
 * Voucher management. Only admins can manage vouchers (proposal 4.3).
 */
@RestController
@RequestMapping("/api/v1/admin/vouchers")
@PreAuthorize("hasRole('ADMIN')")
public class VoucherAdminController {

    private static final Set<String> SORTABLE = Set.of("id", "code", "type", "discountValue", "minOrderAmount",
            "startDate", "endDate", "usageLimit", "usedCount", "createdAt");

    private final VoucherService voucherService;

    public VoucherAdminController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<VoucherDto>> list(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) VoucherType type,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant validFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant validTo,
            @RequestParam(required = false) BigDecimal minOrderFrom,
            @RequestParam(required = false) BigDecimal minOrderTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) List<String> sort) {
        Pageable pageable = PagingSupport.pageable(page, size, sort, SORTABLE, Sort.by(Sort.Direction.DESC, "id"));
        VoucherFilter filter = new VoucherFilter(code, type, active, validFrom, validTo, minOrderFrom, minOrderTo);
        return ResponseEntity.ok(PageResponse.of(voucherService.search(filter, pageable), VoucherDto::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VoucherDto> get(@PathVariable long id) {
        return ResponseEntity.ok(VoucherDto.from(voucherService.get(id)));
    }

    @PostMapping
    public ResponseEntity<VoucherDto> create(@RequestBody VoucherRequest request) {
        Voucher created = voucherService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/admin/vouchers/" + created.getId()))
                .body(VoucherDto.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VoucherDto> update(@PathVariable long id, @RequestBody VoucherRequest request) {
        return ResponseEntity.ok(VoucherDto.from(voucherService.update(id, request)));
    }

    /** Soft delete: moves the voucher to the trash. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id, Authentication authentication) {
        voucherService.softDelete(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/trash")
    public ResponseEntity<PageResponse<VoucherDto>> trash(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PagingSupport.pageable(page, size, null, SORTABLE, Sort.unsorted());
        return ResponseEntity.ok(PageResponse.of(voucherService.trash(pageable), VoucherDto::from));
    }

    @PostMapping("/{id}/restore")
    public ResponseEntity<VoucherDto> restore(@PathVariable long id) {
        return ResponseEntity.ok(VoucherDto.from(voucherService.restore(id)));
    }

    /** Deletes a voucher that is already in the trash for good. */
    @DeleteMapping("/{id}/purge")
    public ResponseEntity<Void> purge(@PathVariable long id) {
        voucherService.purge(id);
        return ResponseEntity.noContent().build();
    }
}
