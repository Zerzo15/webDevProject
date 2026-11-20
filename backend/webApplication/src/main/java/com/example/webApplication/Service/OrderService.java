package com.example.webApplication.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.webApplication.Dto.OrderDetailDto;
import com.example.webApplication.Dto.OrderHistoryDto;
import com.example.webApplication.Dto.OrderLineDto;
import com.example.webApplication.Dto.PlaceOrderRequest;
import com.example.webApplication.Dto.PriceBreakdown;
import com.example.webApplication.Entity.Cart;
import com.example.webApplication.Entity.CartItem;
import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderItem;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.OrderStatusHistory;
import com.example.webApplication.Entity.Product;
import com.example.webApplication.Entity.ProductVariant;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Entity.Voucher;
import com.example.webApplication.Entity.VoucherUsage;
import com.example.webApplication.Exception.ConflictException;
import com.example.webApplication.Exception.InvalidRequestException;
import com.example.webApplication.Exception.InvalidVoucherException;
import com.example.webApplication.Exception.InvalidVoucherException.Reason;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.CartItemRepo;
import com.example.webApplication.Repository.CartRepo;
import com.example.webApplication.Repository.OrderItemRepo;
import com.example.webApplication.Repository.OrderRepo;
import com.example.webApplication.Repository.OrderStatusHistoryRepo;
import com.example.webApplication.Repository.ProductVariantRepo;
import com.example.webApplication.Repository.VoucherRepo;
import com.example.webApplication.Repository.VoucherUsageRepo;

import jakarta.persistence.criteria.Predicate;

/**
 * Placing and following orders (proposal 5.1, 6.1, 6.2, 6.3).
 * Placing an order is one transaction: stock, voucher use, order, lines, history and the empty cart
 * are all saved together or not at all. Order.status is only changed through OrderStateMachine,
 * and every change writes one row of order_status_history.
 */
@Service
public class OrderService {

    static final int MAX_ADDRESS_LENGTH = 1000;
    static final int MAX_KEY_LENGTH = 100;
    static final String PAYMENT_METHOD = "COD";

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final OrderStatusHistoryRepo historyRepo;
    private final VoucherRepo voucherRepo;
    private final VoucherUsageRepo voucherUsageRepo;
    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final ProductVariantRepo variantRepo;
    private final InventoryService inventory;
    private final OrderStateMachine stateMachine;
    private final VoucherCalculator calculator;
    private final TransactionTemplate transaction;
    private final BigDecimal shippingFee;

    public OrderService(OrderRepo orderRepo, OrderItemRepo orderItemRepo, OrderStatusHistoryRepo historyRepo,
            VoucherRepo voucherRepo, VoucherUsageRepo voucherUsageRepo, CartRepo cartRepo, CartItemRepo cartItemRepo,
            ProductVariantRepo variantRepo, InventoryService inventory, OrderStateMachine stateMachine, VoucherCalculator calculator,
            PlatformTransactionManager transactionManager, @Value("${order.shipping-fee}") BigDecimal shippingFee) {
        this.orderRepo = orderRepo;
        this.orderItemRepo = orderItemRepo;
        this.historyRepo = historyRepo;
        this.voucherRepo = voucherRepo;
        this.voucherUsageRepo = voucherUsageRepo;
        this.cartRepo = cartRepo;
        this.cartItemRepo = cartItemRepo;
        this.variantRepo = variantRepo;
        this.inventory = inventory;
        this.stateMachine = stateMachine;
        this.calculator = calculator;
        this.transaction = new TransactionTemplate(transactionManager);
        this.shippingFee = shippingFee;
    }

    // ---------------------------------------------------------------- preview

    /** The total the customer would pay for the current cart with the given voucher code (may be null). */
    @Transactional(readOnly = true)
    public PriceBreakdown preview(User user, String voucherCode) {
        List<Line> lines = readCart(user);
        Voucher voucher = findVoucher(voucherCode);
        return calculator.calculate(voucher, subtotal(lines), shippingFee, Instant.now());
    }

    // ----------------------------------------------------------------- place

    /**
     * Places an order from the cart of the user. A second call with the same idempotency key
     * returns the order of the first call instead of creating another one.
     */
    public OrderDetailDto place(User user, PlaceOrderRequest request, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > MAX_KEY_LENGTH) {
            throw new InvalidRequestException("Idempotency-Key header is required (at most " + MAX_KEY_LENGTH + " characters)");
        }
        String address = request == null || request.shippingAddress() == null ? "" : request.shippingAddress().trim();
        if (address.isEmpty() || address.length() > MAX_ADDRESS_LENGTH) {
            throw new InvalidRequestException("shippingAddress is required (at most " + MAX_ADDRESS_LENGTH + " characters)");
        }

        OrderDetailDto existing = findByKey(user, idempotencyKey);
        if (existing != null) {
            return existing;
        }
        try {
            return transaction.execute(status -> doPlace(user, address, request.voucherCode(), idempotencyKey));
        } catch (DataIntegrityViolationException e) {
            // Another request with the same key won the race.
            OrderDetailDto winner = findByKey(user, idempotencyKey);
            if (winner != null) {
                return winner;
            }
            throw e;
        }
    }

    private OrderDetailDto findByKey(User user, String key) {
        return transaction.execute(status -> orderRepo.findByIdempotencyKey(key).map(order -> {
            if (order.getUser().getId() != user.getId()) {
                throw new ConflictException("This Idempotency-Key was already used");
            }
            return toDetail(order);
        }).orElse(null));
    }

    private OrderDetailDto doPlace(User user, String address, String voucherCode, String idempotencyKey) {
        List<Line> lines = new ArrayList<>(readCart(user));
        Instant now = Instant.now();
        Voucher voucher = findVoucher(voucherCode);
        PriceBreakdown price = calculator.calculate(voucher, subtotal(lines), shippingFee, now);

        // Take the stock first, in a fixed order (by variant id) so two orders cannot deadlock.
        // Any failure rolls the whole transaction back, which also gives the stock back.
        lines.sort(Comparator.comparingLong(Line::variantId));
        for (Line line : lines) {
            inventory.decrease(line.variantId(), line.quantity());
        }
        if (voucher != null && voucherRepo.useOnce(voucher.getId()) == 0) {
            throw new InvalidVoucherException(Reason.USAGE_LIMIT_REACHED, "Voucher has no uses left");
        }

        Order order = new Order();
        order.setUser(user);
        order.setVoucher(voucher == null ? null : voucherRepo.getReferenceById(voucher.getId()));
        order.setStatus(OrderStatus.PENDING_CONFIRMATION);
        order.setPaymentMethod(PAYMENT_METHOD);
        order.setShippingAddressText(address);
        order.setShippingFee(price.shippingFee());
        order.setDiscountAmount(price.discount());
        order.setShippingDiscount(price.shippingDiscount());
        order.setTotalAmount(price.total());
        order.setIdempotencyKey(idempotencyKey);
        order = orderRepo.save(order);

        for (Line line : lines) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProductVariant(variantRepo.getReferenceById(line.variantId()));
            item.setQuantity(line.quantity());
            item.setUnitPrice(line.unitPrice());
            orderItemRepo.save(item);
        }
        if (voucher != null) {
            VoucherUsage usage = new VoucherUsage();
            usage.setVoucher(voucherRepo.getReferenceById(voucher.getId()));
            usage.setUser(user);
            usage.setOrder(order);
            voucherUsageRepo.save(usage);
        }
        recordHistory(order, null, OrderStatus.PENDING_CONFIRMATION, user.getEmail(), "Order placed");
        cartItemRepo.deleteByCartId(cartRepo.findByUserId(user.getId()).orElseThrow().getId());
        return toDetail(order);
    }

    // ------------------------------------------------------- status changes

    /**
     * Changes the status through the state machine and writes the history row.
     * Cancelling also gives the stock and the voucher use back. The caller owns the transaction.
     * The order passed in may be detached afterwards; read it again with findById.
     */
    @Transactional
    public void changeStatus(Order order, OrderStatus target, OrderActor actor, String changedBy, String reason) {
        OrderStatus from = order.getStatus();
        stateMachine.transition(order, target, actor);
        orderRepo.save(order);
        recordHistory(order, from, target, changedBy, reason);

        if (target == OrderStatus.CANCELLED) {
            // Collect what is needed first: the calls below clear the persistence context.
            long orderId = order.getId();
            Long voucherId = order.getVoucher() == null ? null : order.getVoucher().getId();
            List<Line> items = new ArrayList<>();
            for (OrderItem item : orderItemRepo.findByOrderIdOrderByIdAsc(orderId)) {
                items.add(new Line(item.getProductVariant().getId(), item.getQuantity(), null));
            }
            items.sort(Comparator.comparingLong(Line::variantId));
            for (Line item : items) {
                inventory.increase(item.variantId(), item.quantity());
            }
            if (voucherId != null) {
                voucherRepo.releaseOnce(voucherId);
                voucherUsageRepo.deleteByOrderId(orderId);
            }
        }
    }

    // ---------------------------------------------------------- my orders

    @Transactional(readOnly = true)
    public Page<Order> listMine(User user, OrderStatus status, Instant createdFrom, Instant createdTo,
            Pageable pageable) {
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> ps = new ArrayList<>();
            ps.add(cb.equal(root.get("user").get("id"), user.getId()));
            if (status != null) {
                ps.add(cb.equal(root.get("status"), status));
            }
            if (createdFrom != null) {
                ps.add(cb.greaterThanOrEqualTo(root.<Instant>get("createdAt"), createdFrom));
            }
            if (createdTo != null) {
                ps.add(cb.lessThanOrEqualTo(root.<Instant>get("createdAt"), createdTo));
            }
            return cb.and(ps.toArray(new Predicate[0]));
        };
        return orderRepo.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public OrderDetailDto getMine(User user, long orderId) {
        return toDetail(findMine(user, orderId));
    }

    /** The customer cancels an order. The state machine only allows this while it waits for confirmation. */
    @Transactional
    public OrderDetailDto cancelMine(User user, long orderId, String reason) {
        Order order = findMine(user, orderId);
        String why = reason == null || reason.isBlank() ? "Cancelled by customer" : reason.trim();
        changeStatus(order, OrderStatus.CANCELLED, OrderActor.CUSTOMER, user.getEmail(), why);
        return toDetail(orderRepo.findById(orderId).orElseThrow());
    }

    /** The customer confirms the goods arrived ("Da nhan"). */
    @Transactional
    public OrderDetailDto confirmReceived(User user, long orderId) {
        Order order = findMine(user, orderId);
        changeStatus(order, OrderStatus.COMPLETED, OrderActor.CUSTOMER, user.getEmail(), "Customer confirmed receipt");
        return toDetail(orderRepo.findById(orderId).orElseThrow());
    }

    private Order findMine(User user, long orderId) {
        // An order of another user is reported as not found, not as forbidden.
        return orderRepo.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order " + orderId + " does not exist"));
    }

    // ------------------------------------------------------------- helpers

    /** An order as the API shows it. Must run inside a transaction (it reads lazy associations). */
    public OrderDetailDto toDetail(Order order) {
        List<OrderLineDto> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO.setScale(2);
        for (OrderItem item : orderItemRepo.findByOrderIdOrderByIdAsc(order.getId())) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();
            BigDecimal lineTotal = VoucherCalculator.lineTotal(item.getUnitPrice(), item.getQuantity());
            subtotal = subtotal.add(lineTotal);
            lines.add(new OrderLineDto(variant.getId(), product.getId(), product.getName(), variant.getSize(),
                    variant.getColor(), item.getUnitPrice(), item.getQuantity(), lineTotal));
        }
        List<OrderHistoryDto> history = new ArrayList<>();
        for (OrderStatusHistory h : historyRepo.findByOrderIdOrderByIdAsc(order.getId())) {
            history.add(new OrderHistoryDto(h.getFromStatus(), h.getToStatus(), h.getChangedBy(), h.getReason(),
                    h.getChangedAt()));
        }
        Voucher voucher = order.getVoucher();
        return new OrderDetailDto(order.getId(), order.getStatus(), order.getPaymentMethod(),
                order.getShippingAddressText(), order.getUser().getEmail(), voucher == null ? null : voucher.getCode(),
                subtotal, order.getDiscountAmount(), order.getShippingFee(), order.getShippingDiscount(),
                order.getTotalAmount(), order.getTrackingCode(), order.getCreatedAt(), lines, history);
    }

    private void recordHistory(Order order, OrderStatus from, OrderStatus to, String changedBy, String reason) {
        OrderStatusHistory entry = new OrderStatusHistory();
        entry.setOrder(order);
        entry.setFromStatus(from);
        entry.setToStatus(to);
        entry.setChangedBy(changedBy);
        entry.setReason(reason);
        historyRepo.save(entry);
    }

    private Voucher findVoucher(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        return voucherRepo.findByCode(code.trim())
                .orElseThrow(() -> new InvalidVoucherException(Reason.NOT_FOUND, "Voucher code does not exist"));
    }

    private List<Line> readCart(User user) {
        Cart cart = cartRepo.findByUserId(user.getId()).orElse(null);
        List<CartItem> items = cart == null ? List.of() : cartItemRepo.findByCartIdOrderByIdAsc(cart.getId());
        if (items.isEmpty()) {
            throw new InvalidRequestException("Your cart is empty");
        }
        List<Line> lines = new ArrayList<>();
        for (CartItem item : items) {
            ProductVariant variant = item.getProductVariant();
            Product product = variant.getProduct();
            if (!product.isActive()) {
                throw new InvalidRequestException("\"" + product.getName() + "\" is not available any more");
            }
            lines.add(new Line(variant.getId(), item.getQuantity(),
                    product.getBasePrice().setScale(2, java.math.RoundingMode.HALF_UP)));
        }
        return lines;
    }

    private static BigDecimal subtotal(List<Line> lines) {
        BigDecimal sum = BigDecimal.ZERO.setScale(2);
        for (Line line : lines) {
            sum = sum.add(VoucherCalculator.lineTotal(line.unitPrice(), line.quantity()));
        }
        return sum;
    }

    /** What the order needs to know about a cart line, copied out so it survives a cleared persistence context. */
    private record Line(long variantId, int quantity, BigDecimal unitPrice) {
    }
}
