package com.example.webApplication.Controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

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
import org.springframework.test.web.servlet.ResultActions;
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
 * Orders are placed through the customer API; staff and admin then handle them.
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class OrderAdminApiTest {

    private static final String API = "/api/v1/admin/orders";

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

    private final String prefix = "A" + (System.nanoTime() % 1_000_000_000L) + "-";
    private final RequestPostProcessor staff = user("staff@test.local").roles("STAFF");
    private final RequestPostProcessor admin = user("admin@test.local").roles("ADMIN");
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
        product.setName("Admin test coat");
        product.setSlug("admin-test-" + System.nanoTime());
        product.setBasePrice(new BigDecimal("100000.00"));
        product = productRepo.save(product);
        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize("S");
        v.setColor("Red");
        v.setStockQuantity(0);
        variantId = variantRepo.save(v).getId();
        inventory.initStock(variantId, 10);
    }

    @AfterEach
    void cleanUp() {
        userRepo.deleteById(alice.getId());
        userRepo.deleteById(bob.getId());
        jdbc.update("delete from vouchers where code like ?", prefix + "%");
        productRepo.deleteById(product.getId());
    }

    // ---------------------------------------------------------------- helpers

    private static RequestPostProcessor asCustomer(User u) {
        return user(u.getEmail()).roles("CUSTOMER");
    }

    private long placeOrder(User who, int quantity, String voucher) throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(who)).contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":" + quantity + "}")).andExpect(status().isOk());
        String body = "{\"shippingAddress\":\"1 Test Street\""
                + (voucher == null ? "" : ",\"voucherCode\":\"" + voucher + "\"") + "}";
        String json = mvc.perform(post("/api/v1/orders").with(asCustomer(who)).header("Idempotency-Key", prefix + (++keyCounter))
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.id")).longValue();
    }

    private ResultActions transition(RequestPostProcessor who, long orderId, String target, String reason) throws Exception {
        return mvc.perform(post(API + "/" + orderId + "/transitions").with(who).contentType(MediaType.APPLICATION_JSON)
                .content("{\"target\":\"" + target + "\"" + (reason == null ? "" : ",\"reason\":\"" + reason + "\"") + "}"));
    }

    private int stock() {
        return jdbc.queryForObject("select stock_quantity from product_variants where id = ?", Integer.class, variantId);
    }

    private long createVoucher(String suffix) {
        jdbc.update("insert into vouchers (created_at, code, type, discount_value, min_order_amount, start_date, end_date, "
                + "usage_limit, used_count, is_active) values (now(6), ?, 'FIXED_AMOUNT', 10000, 0, '2020-01-01', '2099-01-01', 5, 0, b'1')",
                prefix + suffix);
        return jdbc.queryForObject("select id from vouchers where code = ?", Long.class, prefix + suffix);
    }

    // ------------------------------------------------------------ list filters

    @Test
    void theFiveFiltersNarrowTheOrderList() throws Exception {
        long o1 = placeOrder(alice, 1, null);
        long o2 = placeOrder(alice, 1, null);
        long o3 = placeOrder(alice, 1, null);
        jdbc.update("update orders set created_at = '2026-01-10 00:00:00', total_amount = 100000 where id = ?", o1);
        jdbc.update("update orders set created_at = '2026-02-10 00:00:00', total_amount = 500000, status = 'AWAITING_PICKUP' where id = ?", o2);
        jdbc.update("update orders set created_at = '2026-03-10 00:00:00', total_amount = 900000, payment_method = 'BANK' where id = ?", o3);
        String mine = alice.getEmail();

        // keyword: customer email
        mvc.perform(get(API).with(staff).param("keyword", mine)).andExpect(jsonPath("$.totalElements").value(3));
        mvc.perform(get(API).with(staff).param("keyword", bob.getEmail())).andExpect(jsonPath("$.totalElements").value(0));
        // keyword: order number
        mvc.perform(get(API).with(staff).param("keyword", String.valueOf(o2)).param("size", "100"))
                .andExpect(jsonPath("$.content[?(@.id==" + o2 + ")]").isNotEmpty());
        // status
        mvc.perform(get(API).with(staff).param("keyword", mine).param("status", "AWAITING_PICKUP"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(o2));
        // created date range
        mvc.perform(get(API).with(staff).param("keyword", mine)
                .param("createdFrom", "2026-02-01T00:00:00Z").param("createdTo", "2026-02-28T00:00:00Z"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(o2));
        // total amount range
        mvc.perform(get(API).with(staff).param("keyword", mine).param("totalFrom", "400000").param("totalTo", "1000000"))
                .andExpect(jsonPath("$.totalElements").value(2));
        // payment method
        mvc.perform(get(API).with(staff).param("keyword", mine).param("paymentMethod", "bank"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(o3));
        // all together
        mvc.perform(get(API).with(staff).param("keyword", mine).param("status", "PENDING_CONFIRMATION")
                .param("totalFrom", "50000").param("totalTo", "200000"))
                .andExpect(jsonPath("$.totalElements").value(1)).andExpect(jsonPath("$.content[0].id").value(o1));
    }

    @Test
    void theListIsPagedAndSortedByTwoColumns() throws Exception {
        long o1 = placeOrder(alice, 1, null);
        long o2 = placeOrder(alice, 1, null);
        long o3 = placeOrder(bob, 1, null);
        jdbc.update("update orders set total_amount = 200000 where id in (?, ?)", o1, o2);
        jdbc.update("update orders set total_amount = 100000 where id = ?", o3);

        // keyword "@test.local" matches all test users; narrow to the three orders by id range below
        mvc.perform(get(API).with(admin).param("keyword", alice.getEmail()).param("sort", "totalAmount,desc").param("sort", "id,asc"))
                .andExpect(jsonPath("$.content[0].id").value(o1))
                .andExpect(jsonPath("$.content[1].id").value(o2))
                .andExpect(jsonPath("$.from").value(1))
                .andExpect(jsonPath("$.to").value(2))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get(API).with(admin).param("keyword", alice.getEmail()).param("sort", "id,desc"))
                .andExpect(jsonPath("$.content[0].id").value(o2));
        mvc.perform(get(API).with(admin).param("size", "15")).andExpect(status().isBadRequest());
        mvc.perform(get(API).with(admin).param("sort", "user,asc")).andExpect(status().isBadRequest());
    }

    @Test
    void detailShowsTheCustomerAndTheHistory() throws Exception {
        long id = placeOrder(alice, 2, null);

        mvc.perform(get(API + "/" + id).with(staff))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerEmail").value(alice.getEmail()))
                .andExpect(jsonPath("$.lines[0].quantity").value(2))
                .andExpect(jsonPath("$.history.length()").value(1));
        mvc.perform(get(API + "/999999999").with(staff)).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------ transitions

    @Test
    void staffMovesAnOrderThroughTheFulfilmentSteps() throws Exception {
        long id = placeOrder(alice, 1, null);

        transition(staff, id, "AWAITING_PICKUP", null)
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("AWAITING_PICKUP"));
        transition(staff, id, "SHIPPING", null).andExpect(jsonPath("$.status").value("SHIPPING"));
        transition(admin, id, "DELIVERED", "handed to the courier")
                .andExpect(jsonPath("$.status").value("DELIVERED"))
                .andExpect(jsonPath("$.history.length()").value(4))
                .andExpect(jsonPath("$.history[1].changedBy").value("staff@test.local"))
                .andExpect(jsonPath("$.history[3].changedBy").value("admin@test.local"))
                .andExpect(jsonPath("$.history[3].reason").value("handed to the courier"))
                .andExpect(jsonPath("$.history[3].fromStatus").value("SHIPPING"));

        // staff and admin cannot complete the order, only the customer can
        transition(staff, id, "COMPLETED", null).andExpect(status().isConflict());
        transition(admin, id, "COMPLETED", null).andExpect(status().isConflict());
        mvc.perform(post("/api/v1/orders/" + id + "/received").with(asCustomer(alice)))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void stepsCannotBeSkippedOrGoBackwards() throws Exception {
        long id = placeOrder(alice, 1, null);

        transition(staff, id, "SHIPPING", null).andExpect(status().isConflict());
        transition(staff, id, "DELIVERED", null).andExpect(status().isConflict());
        transition(staff, id, "PENDING_CONFIRMATION", null).andExpect(status().isConflict());
        transition(staff, id, "AWAITING_PICKUP", null).andExpect(status().isOk());
        transition(staff, id, "PENDING_CONFIRMATION", null).andExpect(status().isConflict());
    }

    @Test
    void cancellingBeforeShippingGivesBackStockAndVoucherUse() throws Exception {
        long voucher = createVoucher("V1");
        long id = placeOrder(alice, 3, prefix + "V1");
        transition(staff, id, "AWAITING_PICKUP", null).andExpect(status().isOk());
        assertEquals(7, stock());
        assertEquals(1, jdbc.queryForObject("select used_count from vouchers where id = ?", Integer.class, voucher));

        transition(staff, id, "CANCELLED", "out of stock at the warehouse")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.history[2].reason").value("out of stock at the warehouse"));

        assertEquals(10, stock());
        assertEquals(0, jdbc.queryForObject("select used_count from vouchers where id = ?", Integer.class, voucher));
    }

    @Test
    void anOrderThatIsShippingCannotBeCancelled() throws Exception {
        long id = placeOrder(alice, 2, null);
        transition(staff, id, "AWAITING_PICKUP", null).andExpect(status().isOk());
        transition(staff, id, "SHIPPING", null).andExpect(status().isOk());

        transition(staff, id, "CANCELLED", null).andExpect(status().isConflict());
        transition(admin, id, "CANCELLED", null).andExpect(status().isConflict());

        assertEquals(8, stock());
    }

    @Test
    void aFinalOrderCannotChangeAnyMore() throws Exception {
        long id = placeOrder(alice, 1, null);
        transition(admin, id, "CANCELLED", null).andExpect(status().isOk());

        transition(admin, id, "AWAITING_PICKUP", null).andExpect(status().isConflict());
        transition(admin, id, "CANCELLED", null).andExpect(status().isConflict());
        assertEquals(10, stock()); // the stock was given back only once
    }

    @Test
    void aMissingTargetAndAnUnknownOrderAreRejected() throws Exception {
        long id = placeOrder(alice, 1, null);

        mvc.perform(post(API + "/" + id + "/transitions").with(staff).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        transition(staff, 999999999L, "AWAITING_PICKUP", null).andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------- stock / roles

    @Test
    void staffAndAdminCorrectTheStock() throws Exception {
        String url = "/api/v1/admin/inventory/variants/" + variantId + "/stock";

        mvc.perform(put(url).with(staff).contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":25}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.variantId").value(variantId))
                .andExpect(jsonPath("$.stockQuantity").value(25));
        assertEquals(25, stock());

        mvc.perform(put(url).with(admin).contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":0}"))
                .andExpect(status().isOk());
        assertEquals(0, stock());
    }

    @Test
    void invalidStockCorrectionsAreRejected() throws Exception {
        String url = "/api/v1/admin/inventory/variants/" + variantId + "/stock";

        mvc.perform(put(url).with(staff).contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put(url).with(staff).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(put("/api/v1/admin/inventory/variants/999999999/stock").with(staff)
                .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":5}"))
                .andExpect(status().isNotFound());
        assertEquals(10, stock());
    }

    @Test
    void customersAndAnonymousUsersCannotUseTheStaffApis() throws Exception {
        RequestPostProcessor customer = asCustomer(alice);
        mvc.perform(get(API).with(customer)).andExpect(status().isForbidden());
        mvc.perform(post(API + "/1/transitions").with(customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"target\":\"CANCELLED\"}")).andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/admin/inventory/variants/" + variantId + "/stock").with(customer)
                .contentType(MediaType.APPLICATION_JSON).content("{\"stockQuantity\":99}")).andExpect(status().isForbidden());
        mvc.perform(get(API)).andExpect(status().is4xxClientError());
        assertEquals(10, stock());
    }
}
