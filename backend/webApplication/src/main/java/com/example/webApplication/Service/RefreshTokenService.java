package com.example.webApplication.Service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.webApplication.Configuration.HashConfiguration;
import com.example.webApplication.Dto.RefreshTokenRequest;
import com.example.webApplication.Dto.TokenRespond;
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

    public RefreshTokenService(RefreshTokenRepo refreshRepo, UserRepo userRepo, JWTService jwtService) {
        this.refreshRepo = refreshRepo;
        this.userRepo = userRepo;
        this.jwtService = jwtService;
    }
    
    public String createToken(String email) {
        //original token
        String token = UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken(
            userRepo.findByEmail(email).get(), 
            HashConfiguration.hash(token), //hashed using SHA-256
            Instant.now().plusMillis(600000)
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

    @Transactional
    public TokenRespond refreshToken(RefreshTokenRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            throw new RefreshTokenException("Refresh token not found");
        }

        String requestTokenHash = HashConfiguration.hash(request.token());
        RefreshToken oldToken = refreshRepo.findByTokenHash(requestTokenHash)
            .map(this::verifyToken)
            .orElseThrow(() -> new RefreshTokenException("Refresh token not found or expired"));

        User user = oldToken.getUser();
        oldToken.setRevoked(true);
        refreshRepo.save(oldToken);

        String newRefreshToken = createToken(user.getEmail());
        String accessToken = jwtService.generateToken(
            user.getEmail(),
            user.getRole().name()
        );

        return new TokenRespond(accessToken, newRefreshToken);
    }
}
