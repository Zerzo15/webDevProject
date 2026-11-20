package com.example.webApplication.Controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.CancelOrderRequest;
import com.example.webApplication.Dto.OrderDetailDto;
import com.example.webApplication.Dto.OrderSummaryDto;
import com.example.webApplication.Dto.PageResponse;
import com.example.webApplication.Dto.PlaceOrderRequest;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Service.CurrentUserService;
import com.example.webApplication.Service.OrderService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * The orders of the logged-in customer (proposal 5.1). The owner always comes from the token.
 */
@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasRole('CUSTOMER')")
public class OrderController {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "totalAmount", "status");

    private final OrderService orderService;
    private final CurrentUserService currentUser;

    public OrderController(OrderService orderService, CurrentUserService currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<OrderDetailDto> place(Authentication authentication,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody PlaceOrderRequest request) {
        OrderDetailDto order = orderService.place(currentUser.require(authentication), request, idempotencyKey);
        return ResponseEntity.created(URI.create("/api/v1/orders/" + order.id())).body(order);
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderSummaryDto>> list(Authentication authentication,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        List<String> sort = PagingSupport.rawSortParams(request);
        Pageable pageable = PagingSupport.pageable(page, size, sort, SORTABLE, Sort.by(Sort.Direction.DESC, "id"));
        return ResponseEntity.ok(PageResponse.of(
                orderService.listMine(currentUser.require(authentication), status, createdFrom, createdTo, pageable),
                OrderSummaryDto::from));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDto> get(Authentication authentication, @PathVariable long id) {
        return ResponseEntity.ok(orderService.getMine(currentUser.require(authentication), id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDetailDto> cancel(Authentication authentication, @PathVariable long id,
            @RequestBody(required = false) CancelOrderRequest request) {
        String reason = request == null ? null : request.reason();
        return ResponseEntity.ok(orderService.cancelMine(currentUser.require(authentication), id, reason));
    }

    @PostMapping("/{id}/received")
    public ResponseEntity<OrderDetailDto> received(Authentication authentication, @PathVariable long id) {
        return ResponseEntity.ok(orderService.confirmReceived(currentUser.require(authentication), id));
    }
}
