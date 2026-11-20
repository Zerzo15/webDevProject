package com.example.webApplication.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.example.webApplication.Entity.OrderStatus;
import com.example.webApplication.Entity.OrderStatusHistory;
import com.example.webApplication.Entity.Product;
import com.example.webApplication.Entity.ProductVariant;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Entity.UserRoleStatus;
import com.example.webApplication.Repository.OrderRepo;
import com.example.webApplication.Repository.OrderStatusHistoryRepo;
import com.example.webApplication.Repository.ProductRepo;
import com.example.webApplication.Repository.ProductVariantRepo;
import com.example.webApplication.Repository.UserRepo;

/**
 * Runs against a real MySQL, so it is skipped when SPRING_DATASOURCE_URL is not set.
 * order.auto-complete-days is 7 in application.properties.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class OrderAutoCompleteServiceTest {

    @Autowired
    private OrderAutoCompleteService service;
    @Autowired
    private MockMvc mvc;
    @Autowired
    private JdbcTemplate jdbc;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVariantRepo variantRepo;
    @Autowired
    private InventoryService inventory;
    @Autowired
    private OrderRepo orderRepo;
    @Autowired
    private OrderStatusHistoryRepo historyRepo;

    // Milliseconds only: MySQL datetime(6) rounds finer values, which would blur the "exactly 7 days" case.
    private final Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
    private User customer;
    private Product product;
    private long variantId;
    private int keyCounter;

    @BeforeEach
    void createData() {
        long n = System.nanoTime();
        customer = new User();
        customer.setEmail("auto" + n + "@test.local");
        customer.setPhone("09" + (n % 100000000L));
        customer.setFullName("auto");
        customer.setPasswordHash("not-a-real-hash");
        customer.setRole(UserRoleStatus.CUSTOMER);
        customer.setEmailVerified(true);
        customer = userRepo.save(customer);

        product = new Product();
        product.setName("Auto-complete test hat");
        product.setSlug("auto-test-" + n);
        product.setBasePrice(new BigDecimal("50000.00"));
        product = productRepo.save(product);
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize("F");
        v.setColor("Grey");
        v.setStockQuantity(0);
        variantId = variantRepo.save(v).getId();
        inventory.initStock(variantId, 50);
    }

    @AfterEach
    void cleanUp() {
        userRepo.deleteById(customer.getId());
        productRepo.deleteById(product.getId());
    }

    private long placeOrder() throws Exception {
        var asCustomer = user(customer.getEmail()).roles("CUSTOMER");
        mvc.perform(post("/api/v1/cart/items").with(asCustomer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":1}")).andExpect(status().isOk());
        String json = mvc.perform(post("/api/v1/orders").with(asCustomer).header("Idempotency-Key", "auto" + System.nanoTime() + "-" + (++keyCounter))
                .contentType(MediaType.APPLICATION_JSON).content("{\"shippingAddress\":\"1 Test Street\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    }

    /** Puts the order in the status and records that it entered it the given time ago. */
    private long orderIn(String status, long daysAgo, long extraSeconds) throws Exception {
        long id = placeOrder();
        jdbc.update("update orders set status = ? where id = ?", status, id);
        // Written through Hibernate, like the application does, so time zones are handled the same way.
        OrderStatusHistory entry = new OrderStatusHistory();
        entry.setOrder(orderRepo.getReferenceById(id));
        entry.setFromStatus(OrderStatus.SHIPPING);
        entry.setToStatus(OrderStatus.valueOf(status));
        entry.setChangedBy("staff@test.local");
        entry.setReason("test");
        entry.setChangedAt(now.minus(daysAgo, ChronoUnit.DAYS).plusSeconds(extraSeconds));
        historyRepo.save(entry);
        return id;
    }

    private String statusOf(long id) {
        return jdbc.queryForObject("select status from orders where id = ?", String.class, id);
    }

    @Test
    void aDeliveredOrderOlderThanSevenDaysIsCompletedBySystem() throws Exception {
        long old = orderIn("DELIVERED", 8, 0);

        int completed = service.completeDeliveredOrders(now);

        assertTrue(completed >= 1);
        assertEquals("COMPLETED", statusOf(old));
        List<Map<String, Object>> history = jdbc.queryForList(
                "select from_status, to_status, changed_by, reason from order_status_history where order_id = ? order by id desc limit 1", old);
        assertEquals("DELIVERED", history.get(0).get("from_status"));
        assertEquals("COMPLETED", history.get(0).get("to_status"));
        assertEquals("SYSTEM", history.get(0).get("changed_by"));
        assertTrue(history.get(0).get("reason").toString().contains("7 days"));
    }

    @Test
    void aRecentlyDeliveredOrderIsLeftAlone() throws Exception {
        long recent = orderIn("DELIVERED", 2, 0);
        long almost = orderIn("DELIVERED", 7, 3600); // delivered 6 days 23 hours ago

        service.completeDeliveredOrders(now);

        assertEquals("DELIVERED", statusOf(recent));
        assertEquals("DELIVERED", statusOf(almost));
    }

    @Test
    void anOrderDeliveredExactlySevenDaysAgoIsCompleted() throws Exception {
        long exact = orderIn("DELIVERED", 7, 0);

        service.completeDeliveredOrders(now);

        assertEquals("COMPLETED", statusOf(exact));
    }

    @Test
    void ordersInOtherStatusesAreNeverTouched() throws Exception {
        long shipping = orderIn("SHIPPING", 30, 0);
        long pending = orderIn("PENDING_CONFIRMATION", 30, 0);
        long cancelled = orderIn("CANCELLED", 30, 0);

        service.completeDeliveredOrders(now);

        assertEquals("SHIPPING", statusOf(shipping));
        assertEquals("PENDING_CONFIRMATION", statusOf(pending));
        assertEquals("CANCELLED", statusOf(cancelled));
    }

    @Test
    void runningTwiceDoesNotCompleteTheSameOrderAgain() throws Exception {
        long old = orderIn("DELIVERED", 9, 0);
        service.completeDeliveredOrders(now);
        int historyAfterFirst = jdbc.queryForObject("select count(*) from order_status_history where order_id = ?", Integer.class, old);

        service.completeDeliveredOrders(now);

        assertEquals("COMPLETED", statusOf(old));
        assertEquals(historyAfterFirst, jdbc.queryForObject(
                "select count(*) from order_status_history where order_id = ?", Integer.class, old));
    }
}
