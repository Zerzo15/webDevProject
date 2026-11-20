package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webApplication.Entity.ProductVariant;

public interface ProductVariantRepo extends JpaRepository<ProductVariant, Long> {

    /**
     * Atomically takes qty from the stock in a single UPDATE (no read-then-write).
     * The WHERE clause guarantees the stock never goes below zero.
     * Note: a bulk UPDATE does not touch the @Version column.
     *
     * @return number of rows updated: 1 on success, 0 when the variant does not exist
     *         or does not have enough stock.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ProductVariant v set v.stockQuantity = v.stockQuantity - :qty "
            + "where v.id = :id and v.stockQuantity >= :qty")
    int decrease(@Param("id") long id, @Param("qty") int qty);

    /**
     * Atomically gives qty back to the stock.
     *
     * @return number of rows updated: 1 on success, 0 when the variant does not exist.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ProductVariant v set v.stockQuantity = v.stockQuantity + :qty where v.id = :id")
    int increase(@Param("id") long id, @Param("qty") int qty);

    /**
     * Sets the stock of a variant (initial stock or a manual adjustment by staff/admin).
     *
     * @return number of rows updated: 1 on success, 0 when the variant does not exist.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ProductVariant v set v.stockQuantity = :qty where v.id = :id")
    int setStock(@Param("id") long id, @Param("qty") int qty);
}
