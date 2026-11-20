package com.example.webApplication.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.StockDto;
import com.example.webApplication.Dto.StockRequest;
import com.example.webApplication.Exception.InvalidRequestException;
import com.example.webApplication.Service.InventoryService;

/**
 * Staff and admin correct the stock of a variant to the counted quantity (proposal 4.1, 4.3).
 */
@RestController
@RequestMapping("/api/v1/admin/inventory/variants")
@PreAuthorize("hasAnyRole('STAFF','ADMIN')")
public class InventoryAdminController {

    private final InventoryService inventoryService;

    public InventoryAdminController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PutMapping("/{variantId}/stock")
    public ResponseEntity<StockDto> setStock(@PathVariable long variantId, @RequestBody StockRequest request) {
        if (request.stockQuantity() == null || request.stockQuantity() < 0) {
            throw new InvalidRequestException("stockQuantity must be zero or more");
        }
        inventoryService.initStock(variantId, request.stockQuantity());
        return ResponseEntity.ok(new StockDto(variantId, request.stockQuantity()));
    }
}
