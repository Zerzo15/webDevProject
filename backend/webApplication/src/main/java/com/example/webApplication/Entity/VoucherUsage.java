package com.example.webApplication.Entity;

import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.*;

@Entity
@Table(name = "voucher_usages", uniqueConstraints = {
    @UniqueConstraint(name = "uk_voucher_user_order", columnNames = {"voucher_id", "user_id", "order_id"})
})

public class VoucherUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "voucher_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Voucher voucher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    @Column(name = "used_at", nullable = false, updatable = false)
    private Instant usedAt = Instant.now();

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
}