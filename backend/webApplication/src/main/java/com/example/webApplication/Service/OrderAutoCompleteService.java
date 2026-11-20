package com.example.webApplication.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.webApplication.Entity.Order;
import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Repository.OrderRepo;

/**
 * Completes delivered orders on behalf of the customer when the customer did not confirm receipt
 * within the configured number of days (proposal 6.1: N = 7). The actor is OrderActor.SYSTEM.
 * Each order is completed in its own transaction, so one failure does not stop the others.
 */
@Service
public class OrderAutoCompleteService {

    private static final Logger log = LoggerFactory.getLogger(OrderAutoCompleteService.class);
    static final String SYSTEM_NAME = "SYSTEM";

    private final OrderRepo orderRepo;
    private final OrderService orderService;
    private final TransactionTemplate transaction;
    private final int days;

    public OrderAutoCompleteService(OrderRepo orderRepo, OrderService orderService,
            PlatformTransactionManager transactionManager, @Value("${order.auto-complete-days}") int days) {
        this.orderRepo = orderRepo;
        this.orderService = orderService;
        this.transaction = new TransactionTemplate(transactionManager);
        this.days = days;
    }

    /**
     * @param now the current time
     * @return how many orders were completed
     */
    public int completeDeliveredOrders(Instant now) {
        Instant before = now.minus(Duration.ofDays(days));
        List<Long> ids = transaction.execute(status -> orderRepo.findInStatusSince(OrderStatus.DELIVERED, before)
                .stream().map(Order::getId).toList());
        int completed = 0;
        for (Long id : ids) {
            try {
                transaction.executeWithoutResult(status -> {
                    Order order = orderRepo.findByIdForUpdate(id).orElseThrow();
                    orderService.changeStatus(order, OrderStatus.COMPLETED, OrderActor.SYSTEM, SYSTEM_NAME,
                            "Completed automatically: no confirmation within " + days + " days of delivery");
                });
                completed++;
            } catch (RuntimeException e) {
                // For example the customer confirmed receipt a moment earlier. Try again at the next run.
                log.warn("Could not auto-complete order {}: {}", id, e.getMessage());
            }
        }
        if (completed > 0) {
            log.info("Auto-completed {} delivered order(s)", completed);
        }
        return completed;
    }
}
