package com.example.webApplication.Controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
 */
@SpringBootTest
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class CartApiTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVariantRepo variantRepo;
    @Autowired
    private InventoryService inventoryService;

    private User alice;
    private User bob;
    private Product product;
    private long variantId;

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
        product.setName("Cart test shirt");
        product.setSlug("cart-test-" + System.nanoTime());
        product.setBasePrice(new BigDecimal("250000.00"));
        product = productRepo.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize("M");
        variant.setColor("Black");
        variant.setStockQuantity(5);
        variantId = variantRepo.save(variant).getId();
    }

    @AfterEach
    void cleanUp() {
        // carts and cart items are removed by ON DELETE CASCADE
        userRepo.deleteById(alice.getId());
        userRepo.deleteById(bob.getId());
        productRepo.deleteById(product.getId());
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor asCustomer(User u) {
        return user(u.getEmail()).roles("CUSTOMER");
    }

    private MvcResult addItem(User who, int quantity) throws Exception {
        return mvc.perform(post("/api/v1/cart/items").with(asCustomer(who))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":" + quantity + "}"))
                .andReturn();
    }

    private long firstItemId(User who) throws Exception {
        String json = mvc.perform(get("/api/v1/cart").with(asCustomer(who))).andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(json, "$.items[0].id")).longValue();
    }

    @Test
    void emptyCartIsReturnedForANewUser() throws Exception {
        mvc.perform(get("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.subtotal").value(0.0));
    }

    @Test
    void addingAnItemShowsPriceAndSubtotal() throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].unitPrice").value(250000.0))
                .andExpect(jsonPath("$.items[0].lineTotal").value(500000.0))
                .andExpect(jsonPath("$.subtotal").value(500000.0))
                .andExpect(jsonPath("$.hasStockProblem").value(false));
    }

    @Test
    void addingTheSameVariantTwiceAddsTheQuantities() throws Exception {
        addItem(alice, 2);

        mvc.perform(post("/api/v1/cart/items").with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    void addingMoreThanTheStockIsRejectedWithConflict() throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":6}"))
                .andExpect(status().isConflict());

        mvc.perform(get("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void quantityBelowOneIsRejectedWithBadRequest() throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownVariantIsNotFound() throws Exception {
        mvc.perform(post("/api/v1/cart/items").with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":999999999,\"quantity\":1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatingAndRemovingALine() throws Exception {
        addItem(alice, 1);
        long itemId = firstItemId(alice);

        mvc.perform(put("/api/v1/cart/items/" + itemId).with(asCustomer(alice))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(4));

        mvc.perform(delete("/api/v1/cart/items/" + itemId).with(asCustomer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void clearingTheCartRemovesEveryLine() throws Exception {
        addItem(alice, 2);

        mvc.perform(delete("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        mvc.perform(get("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(jsonPath("$.items.length()").value(0));
    }

    @Test
    void aCartShowsAStockProblemWhenStockDropsBelowTheQuantity() throws Exception {
        addItem(alice, 4);
        inventoryService.initStock(variantId, 2);

        mvc.perform(get("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(jsonPath("$.items[0].inStock").value(false))
                .andExpect(jsonPath("$.items[0].availableStock").value(2))
                .andExpect(jsonPath("$.hasStockProblem").value(true));
    }

    @Test
    void usersCannotSeeOrChangeEachOthersCarts() throws Exception {
        addItem(alice, 2);
        long alicesItem = firstItemId(alice);

        mvc.perform(get("/api/v1/cart").with(asCustomer(bob)))
                .andExpect(jsonPath("$.items.length()").value(0));

        mvc.perform(put("/api/v1/cart/items/" + alicesItem).with(asCustomer(bob))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1}"))
                .andExpect(status().isNotFound());

        mvc.perform(delete("/api/v1/cart/items/" + alicesItem).with(asCustomer(bob)))
                .andExpect(status().isNotFound());

        mvc.perform(get("/api/v1/cart").with(asCustomer(alice)))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
    }

    @Test
    void staffAndAdminHaveNoCart() throws Exception {
        mvc.perform(get("/api/v1/cart").with(user("staff@test.local").roles("STAFF")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/cart").with(user("admin@test.local").roles("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void anonymousUsersAreRejected() throws Exception {
        mvc.perform(get("/api/v1/cart")).andExpect(status().is4xxClientError());
    }
}
