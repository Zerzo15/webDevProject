package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.example.webApplication.Entity.RefreshToken;

import jakarta.persistence.LockModeType;


public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long>{
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);
} 
