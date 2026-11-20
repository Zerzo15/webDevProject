package com.example.webApplication.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Exception.OutOfStockException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.ProductVariantRepo;

/**
 * The only place that changes the stock of a product variant (proposal 6.2).
 * Every change is one atomic UPDATE, so concurrent orders can never oversell.
 * These methods join the caller's transaction, so a rollback of the order also rolls the stock back.
 */
@Service
public class InventoryService {

    private final ProductVariantRepo variantRepo;

    public InventoryService(ProductVariantRepo variantRepo) {
        this.variantRepo = variantRepo;
    }

    /**
     * Takes qty from the stock of the variant.
     *
     * @throws OutOfStockException       when the stock is lower than qty
     * @throws ResourceNotFoundException when the variant does not exist
     * @throws IllegalArgumentException  when qty is not positive
     */
    @Transactional
    public void decrease(long variantId, int qty) {
        requirePositive(qty);
        if (variantRepo.decrease(variantId, qty) == 0) {
            if (!variantRepo.existsById(variantId)) {
                throw notFound(variantId);
            }
            throw new OutOfStockException(variantId, qty);
        }
    }

    /**
     * Gives qty back to the stock (for example when an order is cancelled).
     *
     * @throws ResourceNotFoundException when the variant does not exist
     * @throws IllegalArgumentException  when qty is not positive
     */
    @Transactional
    public void increase(long variantId, int qty) {
        requirePositive(qty);
        if (variantRepo.increase(variantId, qty) == 0) {
            throw notFound(variantId);
        }
    }

    /**
     * Sets the stock of a variant: the initial stock of a new variant, or a manual
     * adjustment by staff/admin.
     *
     * @throws ResourceNotFoundException when the variant does not exist
     * @throws IllegalArgumentException  when qty is negative
     */
    @Transactional
    public void initStock(long variantId, int qty) {
        if (qty < 0) {
            throw new IllegalArgumentException("Stock must not be negative");
        }
        if (variantRepo.setStock(variantId, qty) == 0) {
            throw notFound(variantId);
        }
    }

    private static void requirePositive(int qty) {
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private static ResourceNotFoundException notFound(long variantId) {
        return new ResourceNotFoundException("Variant " + variantId + " does not exist");
    }
}
