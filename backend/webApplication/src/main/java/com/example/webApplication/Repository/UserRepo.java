package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    boolean existsByEmail(String email);
    
    Optional<User> findByUsernameOrEmailOrPhone(String username, String email, String phone);
}
