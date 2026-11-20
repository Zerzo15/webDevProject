package com.example.webApplication.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.CheckoutPreviewRequest;
import com.example.webApplication.Dto.PriceBreakdown;
import com.example.webApplication.Service.CurrentUserService;
import com.example.webApplication.Service.OrderService;

/**
 * Shows the total of the cart, with an optional voucher, before the customer places the order.
 * It uses the same calculation as placing the order, so the two never differ.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@PreAuthorize("hasRole('CUSTOMER')")
public class CheckoutController {

    private final OrderService orderService;
    private final CurrentUserService currentUser;

    public CheckoutController(OrderService orderService, CurrentUserService currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @PostMapping("/preview")
    public ResponseEntity<PriceBreakdown> preview(Authentication authentication,
            @RequestBody(required = false) CheckoutPreviewRequest request) {
        String code = request == null ? null : request.voucherCode();
        return ResponseEntity.ok(orderService.preview(currentUser.require(authentication), code));
    }
}
