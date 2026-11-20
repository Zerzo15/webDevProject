package com.example.webApplication.Service;

import java.time.Instant;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Runs the auto-complete on a schedule when the application is running by itself.
 * Switch it off with order.auto-complete.enabled=false (for example when a cloud scheduler calls the service).
 */
@Component
@ConditionalOnProperty(name = "order.auto-complete.enabled", havingValue = "true", matchIfMissing = true)
public class OrderAutoCompleteJob {

    private final OrderAutoCompleteService service;

    public OrderAutoCompleteJob(OrderAutoCompleteService service) {
        this.service = service;
    }

    @Scheduled(cron = "${order.auto-complete.cron}")
    public void run() {
        service.completeDeliveredOrders(Instant.now());
    }
}
