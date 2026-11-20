package com.example.webApplication.Service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.webApplication.Entity.Product;
import com.example.webApplication.Entity.ProductVariant;
import com.example.webApplication.Exception.OutOfStockException;
import com.example.webApplication.Repository.ProductRepo;
import com.example.webApplication.Repository.ProductVariantRepo;

/**
 * Runs against a real MySQL (the same one the application uses), so it is skipped
 * when SPRING_DATASOURCE_URL is not set in the environment.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "SPRING_DATASOURCE_URL", matches = ".+")
class InventoryServiceConcurrencyTest {

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ProductRepo productRepo;
    @Autowired
    private ProductVariantRepo variantRepo;

    private Product product;
    private long variantId;

    @BeforeEach
    void createVariantWithStock() {
        product = new Product();
        product.setName("Inventory test product");
        product.setSlug("inventory-test-" + System.nanoTime());
        product.setBasePrice(new BigDecimal("100000.00"));
        product = productRepo.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSize("M");
        variant.setColor("Black");
        variant.setStockQuantity(0);
        variantId = variantRepo.save(variant).getId();
    }

    @AfterEach
    void cleanUp() {
        // variants are removed by ON DELETE CASCADE
        productRepo.deleteById(product.getId());
    }

    private int stock() {
        return variantRepo.findById(variantId).orElseThrow().getStockQuantity();
    }

    @Test
    void fiftyThreadsBuyingTheLastFiveItemsOnlyFiveSucceed() throws Exception {
        inventoryService.initStock(variantId, 5);

        int threads = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch go = new CountDownLatch(1);
        AtomicInteger succeeded = new AtomicInteger();
        AtomicInteger outOfStock = new AtomicInteger();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
            Callable<Void> buyOne = () -> {
                ready.countDown();
                go.await();
                try {
                    inventoryService.decrease(variantId, 1);
                    succeeded.incrementAndGet();
                } catch (OutOfStockException e) {
                    outOfStock.incrementAndGet();
                }
                return null;
            };
            futures.add(pool.submit(buyOne));
        }
        ready.await();
        go.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdown();

        assertEquals(5, succeeded.get());
        assertEquals(45, outOfStock.get());
        assertEquals(0, stock());
    }

    @Test
    void decreaseThenIncreaseRestoresTheStock() {
        inventoryService.initStock(variantId, 10);

        inventoryService.decrease(variantId, 4);
        assertEquals(6, stock());

        inventoryService.increase(variantId, 4);
        assertEquals(10, stock());
    }

    @Test
    void stockNeverGoesNegative() {
        inventoryService.initStock(variantId, 2);

        assertThrows(OutOfStockException.class, () -> inventoryService.decrease(variantId, 3));

        assertEquals(2, stock());
    }
}
