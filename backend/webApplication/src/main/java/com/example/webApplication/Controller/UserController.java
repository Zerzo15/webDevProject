package com.example.webApplication.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.webApplication.Dto.LoginRequest;
import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Service.UserService;

@Controller 
@RequestMapping("/api/user")
public class UserController {
    
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            UserDto savedUser = userService.registerUser(request);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            TokenRespond successMessage = userService.loginUser(loginRequest);
            return ResponseEntity.ok(successMessage);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body("Error: Invalid Credentials");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: Something Went Wrong");
        }
    }  
}
