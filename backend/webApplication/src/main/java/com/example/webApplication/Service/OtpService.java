package com.example.webApplication.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Configuration.HashConfiguration;
import com.example.webApplication.Dto.OtpCodeDto;
import com.example.webApplication.Dto.ResendVerificationRequest;
import com.example.webApplication.Dto.VerifyEmailRequest;
import com.example.webApplication.Entity.OtpCode;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.EmailDeliveryException;
import com.example.webApplication.Exception.OtpException;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.OtpRepo;
import com.example.webApplication.Repository.UserRepo;

@Service 
public class OtpService {

    private final OtpRepo otpRepo;
    private final UserRepo userRepo;
    private final EmailService emailService;
    
    @Value("${otp.length}")
    private int otpLength;

    @Value("${otp.expiration-min}")
    private int otpExpiration;

    @Value("${otp.secret}")
    private String otpSecret;

    @Value("${otp.resend-cooldown-min}")
    private int otpResend;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    public OtpService(OtpRepo otpRepo, UserRepo userRepo, EmailService emailService) {
        this.otpRepo = otpRepo;
        this.userRepo = userRepo;
        this.emailService = emailService;
    }

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Creates a new OTP for the user, emails it, then deletes every older code for
     * that email. Registration and resend both go through this.
     */
    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public void issueAndSendOtp(User user) {
        if (user == null || user.getEmail() == null) {
            throw new OtpException("Enter a user with an email");
        }

        String normalizedEmail = normalizeEmail(user.getEmail());

        String rawCode = generateRawCode();
        OtpCode savedOtp = createOtp(user, normalizedEmail, rawCode);

        try {
            emailService.sendVerificationEmail(normalizedEmail, rawCode, otpExpiration);
        } catch (EmailDeliveryException ex) {
            // the code never reached the user, make sure it cannot be used
            savedOtp.setUsed(true);
            otpRepo.save(savedOtp);
            throw ex;
        }

        // the new code is live and delivered, so every earlier code for this
        // email is both invalid and removed from the database
        otpRepo.deleteOtherCodesForEmail(normalizedEmail, savedOtp.getId());
    }

    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public OtpCodeDto generateVerificationCode(Long userId) {
        if (userId == null) {
            throw new ResourceNotFoundException("Enter user id");
        }

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        issueAndSendOtp(user);

        OtpCode savedOtp = otpRepo.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(normalizeEmail(user.getEmail()))
            .orElseThrow(() -> new OtpException("Otp could not be generated"));

        // never expose the code (or its hash) to the caller
        return new OtpCodeDto(
            savedOtp.getId(),
            null,
            savedOtp.getExpiresAt()
        );
    }

    @Transactional(noRollbackFor = OtpException.class)
    public void verifyEmail(VerifyEmailRequest request) {
        if (request == null || request.email() == null || request.email().trim().isEmpty()) {
            throw new OtpException("Provide an email");
        }
        if (request.code() == null || request.code().trim().isEmpty()) {
            throw new OtpException("Provide an otp code");
        }

        String normalizedEmail = normalizeEmail(request.email());
        String submittedCode = normalizeCode(request.code());

        User user = userRepo.findByEmail(normalizedEmail)
            .orElseThrow(() -> new OtpException("Invalid or expired otp code"));

        if (user.isEmailVerified()) {
            throw new OtpException("Email is already verified");
        }

        OtpCode activeOtp = otpRepo.findTopByEmailAndUsedFalseOrderByCreatedAtDesc(normalizedEmail)
            .orElseThrow(() -> new OtpException("Invalid or expired otp code"));

        if (activeOtp.getExpiresAt().compareTo(Instant.now()) < 0) {
            activeOtp.setUsed(true);
            otpRepo.save(activeOtp);
            throw new OtpException("Otp expired, create a new one");
        }

        if (activeOtp.getAttempts() >= maxAttempts) {
            activeOtp.setUsed(true);
            otpRepo.save(activeOtp);
            throw new OtpException("Too many failed attempts, create a new one");
        }

        String submittedHash = HashConfiguration.hash(submittedCode);
        if (!submittedHash.equals(activeOtp.getCode())) {
            int nextAttempts = activeOtp.getAttempts() + 1;
            activeOtp.setAttempts(nextAttempts);
            if (nextAttempts >= maxAttempts) {
                activeOtp.setUsed(true);
            }
            otpRepo.save(activeOtp);
            throw new OtpException("Provide a valid otp code");
        }

        activeOtp.setUsed(true);
        otpRepo.save(activeOtp);

        user.setEmailVerified(true);
        userRepo.save(user);
    }

    @Transactional(noRollbackFor = EmailDeliveryException.class)
    public void resendOtp(ResendVerificationRequest request) {
        if (request == null || request.email() == null || request.email().trim().isEmpty()) {
            throw new OtpException("Provide an email");
        }

        String normalizedEmail = normalizeEmail(request.email());
        User user = userRepo.findByEmail(normalizedEmail).orElse(null);

        // do not leak whether the account exists or is already verified
        if (user == null || user.isEmailVerified()) {
            return;
        }

        // newest row regardless of state, so burning a code on failed attempts
        // cannot be used to skip the cooldown
        otpRepo.findTopByEmailOrderByCreatedAtDesc(normalizedEmail).ifPresent(latest -> {
            Instant availableAt = latest.getCreatedAt().plus(otpResend, ChronoUnit.MINUTES);
            if (availableAt.isAfter(Instant.now())) {
                throw new OtpException("Please wait " + otpResend + " minute(s) before requesting a new otp code");
            }
        });

        issueAndSendOtp(user);
    }

    public void verifyOtp(String rawOtp) {
        if (rawOtp == null || rawOtp.trim().isEmpty()) {
            throw new OtpException("Provide a otp code");
        }

        String otpHash = HashConfiguration.hash(normalizeCode(rawOtp));
        OtpCode oldOtp = otpRepo.findByCode(otpHash)
            .orElseThrow(() -> new OtpException("Otp not found or expired"));

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

    private OtpCode createOtp(User user, String normalizedEmail, String rawCode) {
        OtpCode otpCode = new OtpCode();

        otpCode.setUser(user);
        otpCode.setEmail(normalizedEmail);

        //store the hashed otp code into the database
        otpCode.setCode(HashConfiguration.hash(rawCode));

        //set the expiration time starting when it was created
        otpCode.setExpiresAt(Instant.now().plus(otpExpiration, ChronoUnit.MINUTES));

        return otpRepo.save(otpCode);
    }

    private String generateRawCode() {
        StringBuilder code = new StringBuilder(otpLength);

        for (int i = 0; i < otpLength; i++) {
            int randomIndex = secureRandom.nextInt(otpSecret.length());

            code.append(otpSecret.charAt(randomIndex));
        }

        return code.toString();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
