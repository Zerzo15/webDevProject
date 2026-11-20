package com.example.webApplication.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.webApplication.Entity.Product;
import com.example.webApplication.Entity.ProductVariant;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Entity.UserRoleStatus;
import com.example.webApplication.Repository.ProductRepo;
import com.example.webApplication.Repository.ProductVariantRepo;
import com.example.webApplication.Repository.UserRepo;
import com.example.webApplication.Service.InventoryService;

/**
 * Runs against a real MySQL, so it is skipped when SPRING_DATASOURCE_URL is not set.
 * The shipping fee comes from order.shipping-fee (30000 in application.properties).
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class OrderApiTest {

    private static final String ADDRESS = "12 Nguyen Hue, District 1, Ho Chi Minh City";

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

    private final String prefix = "O" + (System.nanoTime() % 1_000_000_000L) + "-";
    private User alice;
    private User bob;
    private Product product;
    private long variantId;
    private int keyCounter;

    private User createUser(String name) {
        long n = System.nanoTime();
        User u = new User();
        u.setEmail(name + n + "@test.local");
        u.setPhone("09" + (n % 100000000L));
        u.setFullName(name);
        u.setPasswordHash("not-a-real-hash");
        u.setRole(UserRoleStatus.CUSTOMER);
        u.setEmailVerified(true);
        return userRepo.save(u);
    }

    @BeforeEach
    void createData() {
        alice = createUser("alice");
        bob = createUser("bob");
        product = new Product();
        product.setName("Order test jacket");
        product.setSlug("order-test-" + System.nanoTime());
        product.setBasePrice(new BigDecimal("100000.00"));
        product = productRepo.save(product);
        variantId = newVariant(5);
    }

    private long newVariant(int stock) {
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize("L");
        v.setColor("Blue");
        v.setStockQuantity(0);
        long id = variantRepo.save(v).getId();
        inventory.initStock(id, stock);
        return id;
    }

    @AfterEach
    void cleanUp() {
        // orders, their lines, history and voucher usages go with the users (ON DELETE CASCADE)
        userRepo.deleteById(alice.getId());
        userRepo.deleteById(bob.getId());
        jdbc.update("delete from vouchers where code like ?", prefix + "%");
        productRepo.deleteById(product.getId());
    }

    // ---------------------------------------------------------------- helpers

    private static RequestPostProcessor asCustomer(User u) {
        return user(u.getEmail()).roles("CUSTOMER");
    }

    private void addToCart(User who, long variant, int quantity) throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(who)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variant + ",\"quantity\":" + quantity + "}"))
                .andExpect(status().isOk());
    }

    private String key() {
        return prefix + "key" + (++keyCounter);
    }

    private org.springframework.test.web.servlet.ResultActions place(User who, String key, String voucher) throws Exception {
        String body = "{\"shippingAddress\":\"" + ADDRESS + "\""
                + (voucher == null ? "" : ",\"voucherCode\":\"" + voucher + "\"") + "}";
        var request = post("/api/v1/orders").with(asCustomer(who)).contentType(MediaType.APPLICATION_JSON).content(body);
        if (key != null) {
            request.header("Idempotency-Key", key);
        }
        return mvc.perform(request);
    }

    private long orderId(org.springframework.test.web.servlet.ResultActions result) throws Exception {
        String json = result.andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    }

    private long createVoucher(String suffix, String type, String value, String max, String min, int limit) {
        jdbc.update("insert into vouchers (created_at, code, type, discount_value, max_discount_amount, min_order_amount, "
                + "start_date, end_date, usage_limit, used_count, is_active) values (now(6), ?, ?, ?, ?, ?, "
                + "'2020-01-01', '2099-01-01', ?, 0, b'1')", prefix + suffix, type, value, max, min, limit);
        return jdbc.queryForObject("select id from vouchers where code = ?", Long.class, prefix + suffix);
    }

    private int stock(long variant) {
        return jdbc.queryForObject("select stock_quantity from product_variants where id = ?", Integer.class, variant);
    }

    private int usedCount(long voucherId) {
        return jdbc.queryForObject("select used_count from vouchers where id = ?", Integer.class, voucherId);
    }

    private int count(String sql, Object... args) {
        return jdbc.queryForObject(sql, Integer.class, args);
    }

    // ------------------------------------------------------------ placing

    @Test
    void placingAnOrderTakesStockEmptiesTheCartAndWritesHistory() throws Exception {
        addToCart(alice, variantId, 2);

        var result = place(alice, key(), null)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING_CONFIRMATION"))
                .andExpect(jsonPath("$.paymentMethod").value("COD"))
                .andExpect(jsonPath("$.shippingAddress").value(ADDRESS))
                .andExpect(jsonPath("$.subtotal").value(200000.0))
                .andExpect(jsonPath("$.shippingFee").value(30000.0))
                .andExpect(jsonPath("$.totalAmount").value(230000.0))
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(100000.0))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.history.length()").value(1))
                .andExpect(jsonPath("$.history[0].toStatus").value("PENDING_CONFIRMATION"))
                .andExpect(jsonPath("$.history[0].fromStatus").doesNotExist());

        assertEquals(3, stock(variantId));
        mvc.perform(get("/api/v1/cart").with(asCustomer(alice))).andExpect(jsonPath("$.items.length()").value(0));
        mvc.perform(get("/api/v1/orders/" + orderId(result)).with(asCustomer(alice))).andExpect(status().isOk());
    }

    @Test
    void thePriceAndAddressAreFixedWhenTheOrderIsPlaced() throws Exception {
        addToCart(alice, variantId, 1);
        long id = orderId(place(alice, key(), null).andExpect(status().isCreated()));

        jdbc.update("update products set base_price = 999999 where id = ?", product.getId());

        mvc.perform(get("/api/v1/orders/" + id).with(asCustomer(alice)))
                .andExpect(jsonPath("$.lines[0].unitPrice").value(100000.0))
                .andExpect(jsonPath("$.totalAmount").value(130000.0));
    }

    @Test
    void aPercentVoucherLowersTheTotalAndCountsOneUse() throws Exception {
        long voucher = createVoucher("P10", "PERCENT", "10", "50000", "100000", 5);
        addToCart(alice, variantId, 2);

        place(alice, key(), prefix + "P10")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.voucherCode").value(prefix + "P10"))
                .andExpect(jsonPath("$.discountAmount").value(20000.0))
                .andExpect(jsonPath("$.totalAmount").value(210000.0));

        assertEquals(1, usedCount(voucher));
        assertEquals(1, count("select count(*) from voucher_usages where voucher_id = ?", voucher));
    }

    @Test
    void aFreeshipVoucherRemovesTheShippingFee() throws Exception {
        createVoucher("FS", "FREESHIP", "0", null, "0", 5);
        addToCart(alice, variantId, 1);

        place(alice, key(), prefix + "FS")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shippingDiscount").value(30000.0))
                .andExpect(jsonPath("$.totalAmount").value(100000.0));
    }

    @Test
    void previewShowsTheSameTotalAsThePlacedOrder() throws Exception {
        createVoucher("PV", "FIXED_AMOUNT", "40000", null, "0", 5);
        addToCart(alice, variantId, 2);

        mvc.perform(post("/api/v1/checkout/preview").with(asCustomer(alice)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"voucherCode\":\"" + prefix + "PV\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(200000.0))
                .andExpect(jsonPath("$.discount").value(40000.0))
                .andExpect(jsonPath("$.shippingFee").value(30000.0))
                .andExpect(jsonPath("$.total").value(190000.0));

        place(alice, key(), prefix + "PV").andExpect(jsonPath("$.totalAmount").value(190000.0));
    }

    @Test
    void previewWithoutVoucherAndWithAnUnknownVoucher() throws Exception {
        addToCart(alice, variantId, 1);

        mvc.perform(post("/api/v1/checkout/preview").with(asCustomer(alice)).contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(jsonPath("$.total").value(130000.0));
        mvc.perform(post("/api/v1/checkout/preview").with(asCustomer(alice)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"voucherCode\":\"" + prefix + "NOPE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aVoucherBelowItsMinimumOrderIsRejectedAndNothingChanges() throws Exception {
        createVoucher("MIN", "PERCENT", "10", null, "500000", 5);
        addToCart(alice, variantId, 1);

        place(alice, key(), prefix + "MIN").andExpect(status().isBadRequest());

        assertEquals(5, stock(variantId));
        mvc.perform(get("/api/v1/cart").with(asCustomer(alice))).andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void aVoucherWithOneUseLeftWorksForOnlyOneOrder() throws Exception {
        long voucher = createVoucher("ONE", "FIXED_AMOUNT", "10000", null, "0", 1);
        addToCart(alice, variantId, 1);
        addToCart(bob, variantId, 1);

        place(alice, key(), prefix + "ONE").andExpect(status().isCreated());
        place(bob, key(), prefix + "ONE").andExpect(status().isBadRequest());

        assertEquals(1, usedCount(voucher));
        assertEquals(4, stock(variantId));
    }

    // -------------------------------------------------------- idempotency

    @Test
    void theSameKeyReturnsTheSameOrderAndTakesStockOnce() throws Exception {
        addToCart(alice, variantId, 2);
        String key = key();

        long first = orderId(place(alice, key, null).andExpect(status().isCreated()));
        long second = orderId(place(alice, key, null).andExpect(status().isCreated()));

        assertEquals(first, second);
        assertEquals(3, stock(variantId));
        assertEquals(1, count("select count(*) from orders where idempotency_key = ?", key));
    }

    @Test
    void aKeyUsedByAnotherCustomerIsRejected() throws Exception {
        addToCart(alice, variantId, 1);
        addToCart(bob, variantId, 1);
        String key = key();
        place(alice, key, null).andExpect(status().isCreated());

        place(bob, key, null).andExpect(status().isConflict());
    }

    // ---------------------------------------------------------- validation

    @Test
    void theKeyHeaderTheAddressAndANonEmptyCartAreRequired() throws Exception {
        addToCart(alice, variantId, 1);

        place(alice, null, null).andExpect(status().isBadRequest());
        mvc.perform(post("/api/v1/orders").with(asCustomer(alice)).header("Idempotency-Key", key())
                .contentType(MediaType.APPLICATION_JSON).content("{\"shippingAddress\":\"  \"}"))
                .andExpect(status().isBadRequest());
        place(bob, key(), null).andExpect(status().isBadRequest()); // bob's cart is empty
    }

    @Test
    void notEnoughStockFailsTheWholeOrderWithoutChangingAnything() throws Exception {
        long other = newVariant(10);
        addToCart(alice, other, 3);
        addToCart(alice, variantId, 4);
        inventory.initStock(variantId, 2); // stock dropped after the items were added

        place(alice, key(), null).andExpect(status().isConflict());

        assertEquals(10, stock(other)); // the first line's stock was given back
        assertEquals(2, stock(variantId));
        assertEquals(0, count("select count(*) from orders where user_id = ?", alice.getId()));
        mvc.perform(get("/api/v1/cart").with(asCustomer(alice))).andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void twoCustomersBuyingTheLastItemOnlyOneSucceeds() throws Exception {
        long last = newVariant(1);
        addToCart(alice, last, 1);
        addToCart(bob, last, 1);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Integer>> results = new ArrayList<>();
        for (User who : List.of(alice, bob)) {
            String key = key();
            results.add(pool.submit(() -> {
                go.await();
                return place(who, key, null).andReturn().getResponse().getStatus();
            }));
        }
        go.countDown();
        int created = 0;
        int conflicts = 0;
        for (Future<Integer> r : results) {
            int code = r.get();
            if (code == 201) {
                created++;
            } else if (code == 409) {
                conflicts++;
            }
        }
        pool.shutdown();

        assertEquals(1, created);
        assertEquals(1, conflicts);
        assertEquals(0, stock(last));
    }

    // ------------------------------------------------------ cancel / received

    @Test
    void cancellingAPendingOrderGivesBackStockAndVoucherUse() throws Exception {
        long voucher = createVoucher("CAN", "FIXED_AMOUNT", "10000", null, "0", 5);
        addToCart(alice, variantId, 2);
        long id = orderId(place(alice, key(), prefix + "CAN").andExpect(status().isCreated()));
        assertEquals(3, stock(variantId));
        assertEquals(1, usedCount(voucher));

        mvc.perform(post("/api/v1/orders/" + id + "/cancel").with(asCustomer(alice)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"changed my mind\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.history.length()").value(2))
                .andExpect(jsonPath("$.history[1].fromStatus").value("PENDING_CONFIRMATION"))
                .andExpect(jsonPath("$.history[1].toStatus").value("CANCELLED"))
                .andExpect(jsonPath("$.history[1].reason").value("changed my mind"))
                .andExpect(jsonPath("$.history[1].changedBy").value(alice.getEmail()));

        assertEquals(5, stock(variantId));
        assertEquals(0, usedCount(voucher));
        assertEquals(0, count("select count(*) from voucher_usages where voucher_id = ?", voucher));
    }

    @Test
    void aCustomerCannotCancelOnceTheShopConfirmedTheOrder() throws Exception {
        addToCart(alice, variantId, 1);
        long id = orderId(place(alice, key(), null).andExpect(status().isCreated()));
        jdbc.update("update orders set status = 'AWAITING_PICKUP' where id = ?", id);

        mvc.perform(post("/api/v1/orders/" + id + "/cancel").with(asCustomer(alice))).andExpect(status().isConflict());

        assertEquals(4, stock(variantId));
    }

    @Test
    void theCustomerConfirmsReceiptOnlyAfterDelivery() throws Exception {
        addToCart(alice, variantId, 1);
        long id = orderId(place(alice, key(), null).andExpect(status().isCreated()));

        mvc.perform(post("/api/v1/orders/" + id + "/received").with(asCustomer(alice))).andExpect(status().isConflict());

        jdbc.update("update orders set status = 'DELIVERED' where id = ?", id);
        mvc.perform(post("/api/v1/orders/" + id + "/received").with(asCustomer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.history[1].toStatus").value("COMPLETED"));
    }

    // ------------------------------------------------------ list / security

    @Test
    void myOrdersCanBeFilteredByStatusAndAreOnlyMine() throws Exception {
        addToCart(alice, variantId, 1);
        long first = orderId(place(alice, key(), null).andExpect(status().isCreated()));
        addToCart(alice, variantId, 1);
        place(alice, key(), null).andExpect(status().isCreated());
        addToCart(bob, variantId, 1);
        place(bob, key(), null).andExpect(status().isCreated());
        mvc.perform(post("/api/v1/orders/" + first + "/cancel").with(asCustomer(alice))).andExpect(status().isOk());

        mvc.perform(get("/api/v1/orders").with(asCustomer(alice)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.from").value(1))
                .andExpect(jsonPath("$.to").value(2));
        mvc.perform(get("/api/v1/orders").with(asCustomer(alice)).param("status", "CANCELLED"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(first));
        mvc.perform(get("/api/v1/orders").with(asCustomer(alice)).param("size", "15")).andExpect(status().isBadRequest());
    }

    @Test
    void anotherCustomersOrderIsNotFound() throws Exception {
        addToCart(alice, variantId, 1);
        long id = orderId(place(alice, key(), null).andExpect(status().isCreated()));

        mvc.perform(get("/api/v1/orders/" + id).with(asCustomer(bob))).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/orders/" + id + "/cancel").with(asCustomer(bob))).andExpect(status().isNotFound());
        mvc.perform(post("/api/v1/orders/" + id + "/received").with(asCustomer(bob))).andExpect(status().isNotFound());
    }

    @Test
    void onlyCustomersUseTheCustomerOrderApi() throws Exception {
        mvc.perform(get("/api/v1/orders").with(user("s@test.local").roles("STAFF"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/orders").with(user("a@test.local").roles("ADMIN"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/orders")).andExpect(status().is4xxClientError());
    }
}
