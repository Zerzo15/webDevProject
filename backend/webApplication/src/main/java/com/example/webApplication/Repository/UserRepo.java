package com.example.webApplication.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.User;

public interface UserRepo extends JpaRepository<User, Long>{
    
}
