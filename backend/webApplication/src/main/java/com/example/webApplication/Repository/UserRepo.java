package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailOrPhone(String email, String phone);
}
