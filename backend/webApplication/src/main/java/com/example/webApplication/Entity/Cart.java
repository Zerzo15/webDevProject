package com.example.webApplication.Entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import jakarta.persistence.*;

@Entity
@Table(name = "carts")
public class Cart extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}