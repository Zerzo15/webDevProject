package com.example.webApplication.Service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Configuration.HashConfiguration;
import com.example.webApplication.Dto.RefreshTokenRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Entity.AccountStatus;
import com.example.webApplication.Entity.RefreshToken;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.RefreshTokenException;
import com.example.webApplication.Repository.RefreshTokenRepo;
import com.example.webApplication.Repository.UserRepo;

@Service 
public class RefreshTokenService {
    
    private RefreshTokenRepo refreshRepo;
    private UserRepo userRepo;
    private final JWTService jwtService;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshExpired;

    public RefreshTokenService(RefreshTokenRepo refreshRepo, UserRepo userRepo, JWTService jwtService) {
        this.refreshRepo = refreshRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }
    
    public String createToken(String email) {
        //original token
        String token = UUID.randomUUID().toString();

        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new RefreshTokenException("User not found"));
        RefreshToken refreshToken = new RefreshToken(
            user,
            HashConfiguration.hash(token), // hashed using SHA-256
            Instant.now().plusMillis(refreshExpired) // 7 days
        );

        refreshRepo.save(refreshToken);

        return token;
    }

    public RefreshToken verifyToken(RefreshToken token) {
        if (token.isRevoked()) {
            throw new RefreshTokenException("Token revoked");
        }
        if (token.getExpiresAt().compareTo(Instant.now()) < 0) {
            refreshRepo.delete(token);
            throw new RefreshTokenException("Token expired");
        }
        return token;
    }

    @Transactional(noRollbackFor = RefreshTokenException.class)
    public TokenRespond refreshToken(RefreshTokenRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new RefreshTokenException("Refresh token not found");
        }

        String requestTokenHash = HashConfiguration.hash(request.token());
        RefreshToken oldToken = refreshRepo.findByTokenHash(requestTokenHash)
            .orElseThrow(() -> new RefreshTokenException("Refresh token not found or expired"));

        if (oldToken.isRevoked()) {
            revokeAllByUser(oldToken.getUser().getId());
            throw new RefreshTokenException("Token revoked");
        }
        verifyToken(oldToken);

        User user = oldToken.getUser();
        if (user.getAccountStatus() == AccountStatus.LOCKED
                || user.getDeletedAt() != null) {
            throw new RefreshTokenException("User is not eligible to refresh tokens");
        }
        oldToken.setRevoked(true);
        refreshRepo.save(oldToken);

        String newRefreshToken = createToken(user.getEmail());
        String accessToken = jwtService.generateToken(
            user.getEmail(),
            user.getRole().name()
        );

        return new TokenRespond(accessToken, newRefreshToken);
    }

    @Transactional(noRollbackFor = RefreshTokenException.class)
    public void logout(String rawToken) {
        // Logout should be safe to repeat. The controller will clear the
        // browser cookie even when the token is already absent or expired.
        if (rawToken == null || rawToken.isBlank()) {
            return;
        }

        String requestTokenHash = HashConfiguration.hash(rawToken);
        refreshRepo.findByTokenHash(requestTokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                refreshRepo.save(token);
            }
        });
    }

    @Transactional
    public void revokeAllByUser(Long userId) {
        refreshRepo.revokeAllByUserId(userId);
    }

}
