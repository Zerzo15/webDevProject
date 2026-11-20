package com.example.webApplication.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.webApplication.Dto.LoginRequest;
import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Service.UserService;

@RestController
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        UserDto savedUser = userService.registerUser(request);
        return ResponseEntity.ok(savedUser); 
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        TokenRespond successMessage = userService.loginUser(loginRequest);
        return ResponseEntity.ok(successMessage);
    }  
}
