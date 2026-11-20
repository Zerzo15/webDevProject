package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.VoucherUsage;

public interface VoucherUsageRepo extends JpaRepository<VoucherUsage, Long> {

    long deleteByOrderId(long orderId);
}
