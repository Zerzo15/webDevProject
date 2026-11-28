package com.example.webApplication.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.webApplication.Configuration.HashConfiguration;
import com.example.webApplication.Dto.OtpCodeDto;
import com.example.webApplication.Entity.OtpCode;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.OtpException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.OtpRepo;
import com.example.webApplication.Repository.UserRepo;

import jakarta.persistence.criteria.CriteriaBuilder.In;

@Service 
public class OtpService {

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    
    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.expiration-min}")
    private int otpExpiration;

    @Value("${otp.resend-cooldown-min}")
    private int otpResend;

    @Value("${otp.secret}")
    private String otpSecret;

    public OtpService(OtpRepo otpRepo, UserRepo userRepo) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    public OtpCodeDto generateVerificationCode(Long userId) {
        if (userId == null) {
            throw new ResourceNotFoundException("Enter user id");
        }

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            
        OtpCode otpCode = new OtpCode();   
        
        otpCode.setUser(user);
        otpCode.setEmail(user.getEmail());

        StringBuilder code = new StringBuilder(otpLength);

        for (int i = 0; i < otpLength; i++) {
            int randomIndex = secureRandom.nextInt(otpSecret.length());

            code.append(otpSecret.charAt(randomIndex));
        }       

        //store the hashed otp code into the database
        otpCode.setCode(HashConfiguration.hash(code.toString()));

        //set the expiration time to be 15 minute starting when it was created
        otpCode.setExpiresAt(Instant.now().plus(otpExpiration, ChronoUnit.MINUTES));

        OtpCode savedOtp = otpRepo.save(otpCode);

        return new OtpCodeDto(
            savedOtp.getId(),
            savedOtp.getCode(),
            savedOtp.getExpiresAt()
        );
    }

    public void verifyOtp(String rawOtp) {
        if (rawOtp == null || rawOtp.trim().isEmpty()) {
            throw new OtpException("Provide a otp code");
        }

        String otpHash = HashConfiguration.hash(rawOtp);
        OtpCode oldOtp = otpRepo.findByCode(otpHash)
            .orElseThrow(() -> new OtpException("Otp not found or expired"));

        if (!otpRepo.existsByCode(rawOtp)) {
            throw new OtpException("Provide a valid otp code");
        }
        if (oldOtp.isUsed()) {
            throw new OtpException("Otp is used, create a new one");
        }
        if (oldOtp.getExpiresAt().compareTo(Instant.now()) < 0) {
            otpRepo.delete(oldOtp);
            throw new OtpException("Otp expired, create a new one");
        }

        oldOtp.setUsed(true);
        otpRepo.save(oldOtp);
    }
}
