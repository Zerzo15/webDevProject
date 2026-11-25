package com.example.webApplication.Controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.LoginRequest;
import com.example.webApplication.Dto.RefreshTokenRequest;
import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Service.RefreshTokenService;
import com.example.webApplication.Service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;
    private final RefreshTokenService refreshService;

    public UserController(UserService userService, RefreshTokenService refreshService) {
        this.userService = userService;
        this.refreshService = refreshService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        UserDto savedUser = userService.registerUser(request);
        return ResponseEntity.ok(savedUser); 
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
            .secure(true)
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
            .secure(true)
            .sameSite("Strict")
            .path("/api/user")
            .build();
    }
}
