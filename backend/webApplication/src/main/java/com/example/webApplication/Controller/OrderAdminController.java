package com.example.webApplication.Controller;

import java.math.BigDecimal;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.OrderAdminFilter;
import com.example.webApplication.Dto.OrderAdminRowDto;
import com.example.webApplication.Dto.OrderDetailDto;
import com.example.webApplication.Dto.OrderTransitionRequest;
import com.example.webApplication.Dto.PageResponse;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Service.OrderActor;
import com.example.webApplication.Service.OrderAdminService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Orders of all customers for staff and admin (proposal 4.3).
 */
@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class OrderAdminController {

    private static final Set<String> SORTABLE = Set.of("id", "createdAt", "totalAmount", "status", "paymentMethod");

    private final OrderAdminService orderAdminService;

    public OrderAdminController(OrderAdminService orderAdminService) {
        this.orderAdminService = orderAdminService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<OrderAdminRowDto>> list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdTo,
            @RequestParam(required = false) BigDecimal totalFrom,
            @RequestParam(required = false) BigDecimal totalTo,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        List<String> sort = PagingSupport.rawSortParams(request);
        Pageable pageable = PagingSupport.pageable(page, size, sort, SORTABLE, Sort.by(Sort.Direction.DESC, "id"));
        OrderAdminFilter filter = new OrderAdminFilter(status, createdFrom, createdTo, totalFrom, totalTo,
                paymentMethod, keyword);
        return ResponseEntity.ok(PageResponse.of(orderAdminService.search(filter, pageable), row -> row));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailDto> get(@PathVariable long id) {
        return ResponseEntity.ok(orderAdminService.get(id));
    }

    /** Confirms, ships, delivers or cancels an order. The state machine decides what the caller may do. */
    @PostMapping("/{id}/transitions")
    public ResponseEntity<OrderDetailDto> transition(@PathVariable long id, @RequestBody OrderTransitionRequest request,
            Authentication authentication) {
        OrderActor actor = actorOf(authentication);
        return ResponseEntity.ok(orderAdminService.transition(id, request.target(), actor,
                authentication.getName(), request.reason()));
    }

    private static OrderActor actorOf(Authentication authentication) {
        boolean admin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        return admin ? OrderActor.ADMIN : OrderActor.STAFF;
    }
}
