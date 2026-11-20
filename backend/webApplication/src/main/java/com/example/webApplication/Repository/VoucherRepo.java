package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webApplication.Entity.Voucher;

/**
 * Normal queries skip soft-deleted vouchers (the entity has @SQLRestriction("deleted_at IS NULL")).
 * The trash queries below are native on purpose so they can see the deleted rows.
 */
public interface VoucherRepo extends JpaRepository<Voucher, Long>, JpaSpecificationExecutor<Voucher> {

    Optional<Voucher> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = "select * from vouchers where deleted_at is not null order by deleted_at desc",
            countQuery = "select count(*) from vouchers where deleted_at is not null",
            nativeQuery = true)
    Page<Voucher> findDeleted(Pageable pageable);

    @Query(value = "select * from vouchers where id = :id and deleted_at is not null", nativeQuery = true)
    Optional<Voucher> findDeletedById(@Param("id") long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update vouchers set deleted_at = null, deleted_by = null, code = :code "
            + "where id = :id and deleted_at is not null", nativeQuery = true)
    int restore(@Param("id") long id, @Param("code") String code);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from vouchers where id = :id and deleted_at is not null", nativeQuery = true)
    int purge(@Param("id") long id);

    /**
     * Atomically takes one use of the voucher; the WHERE clause stops it at the usage limit.
     *
     * @return 1 on success, 0 when the voucher has no uses left (or does not exist)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Voucher v set v.usedCount = v.usedCount + 1 where v.id = :id and v.usedCount < v.usageLimit")
    int useOnce(@Param("id") long id);

    /** Gives one use back (when an order that used the voucher is cancelled). */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "update vouchers set used_count = used_count - 1 where id = :id and used_count > 0", nativeQuery = true)
    int releaseOnce(@Param("id") long id);
}
