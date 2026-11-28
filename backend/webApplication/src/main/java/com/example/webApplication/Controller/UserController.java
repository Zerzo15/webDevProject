package com.example.webApplication.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import com.example.webApplication.Dto.LoginRequest;
import com.example.webApplication.Dto.RefreshTokenRequest;
import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.ResendVerificationRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Dto.VerifyEmailRequest;
import com.example.webApplication.Service.OtpService;
import com.example.webApplication.Service.RefreshTokenService;
import com.example.webApplication.Service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;
    private final RefreshTokenService refreshService;
    private final OtpService otpService;

    @Value("${security.refresh-cookie-secure:true}")
    private boolean refreshCookieSecure;

    public UserController(UserService userService, RefreshTokenService refreshService, OtpService otpService) {
        this.userService = userService;
        this.refreshService = refreshService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        UserDto savedUser = userService.registerUser(request);
        return ResponseEntity.ok(savedUser); 
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        otpService.verifyEmail(request);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully. You can now log in."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(@RequestBody ResendVerificationRequest request) {
        otpService.resendOtp(request);
        return ResponseEntity.ok(Map.of("message", "If an unverified account exists for this email, a verification code has been sent."));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenRespond> loginUser(@RequestBody LoginRequest loginRequest) {
        TokenRespond successMessage = userService.loginUser(loginRequest);
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie(successMessage.refreshToken()).toString())
            .body(successMessage);
    }  

    @PostMapping("/refresh-token")       
    public ResponseEntity<TokenRespond> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String rawToken) {
        TokenRespond success = refreshService.refreshToken(new RefreshTokenRequest(rawToken));
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, refreshCookie(success.refreshToken()).toString())
            .body(success);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String rawToken) {
        if (rawToken != null && !rawToken.isBlank()) {
            refreshService.logout(rawToken);
        }

        ResponseCookie clearCookie = ResponseCookie.from("refresh_token", "")
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .sameSite("Strict")
            .path("/api/user")
            .maxAge(0)
            .build();
        return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .build();
    }

    private ResponseCookie refreshCookie(String token) {
        return ResponseCookie.from("refresh_token", token)
            .httpOnly(true)
            .secure(refreshCookieSecure)
            .sameSite("Strict")
            .path("/api/user")
            .build();
    }
}
