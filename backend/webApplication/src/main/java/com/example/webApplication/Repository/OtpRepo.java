package com.example.webApplication.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.webApplication.Entity.OtpCode;

public interface OtpRepo extends JpaRepository<OtpCode, Long>{
    
    boolean existsByCode(String code);

    Optional<OtpCode> findByCode(String code);
}
