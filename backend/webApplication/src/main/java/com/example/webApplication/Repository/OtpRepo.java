package com.example.webApplication.Repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.webApplication.Entity.OtpCode;

import jakarta.persistence.LockModeType;

public interface OtpRepo extends JpaRepository<OtpCode, Long>{
    
    boolean existsByCode(String code);

    Optional<OtpCode> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpCode> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("UPDATE OtpCode o SET o.used = true WHERE o.email = :email AND o.used = false")
    int invalidateAllUnusedByEmail(@Param("email") String email);

    void deleteByExpiresAtBefore(Instant now);
}
