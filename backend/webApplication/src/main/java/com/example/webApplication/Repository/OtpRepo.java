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

    Optional<OtpCode> findTopByEmailOrderByCreatedAtDesc(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpCode> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.email = :email AND o.id <> :keepId")
    int deleteOtherCodesForEmail(@Param("email") String email, @Param("keepId") Long keepId);

    void deleteByExpiresAtBefore(Instant now);
}
