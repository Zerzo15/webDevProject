package com.example.webApplication.Service;

import java.time.Instant;
import java.util.Locale;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.webApplication.Dto.LoginRequest;
import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.TokenRespond;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.InvalidRegistrationException;
import com.example.webApplication.Exception.UserAlreadyExistsException;
import com.example.webApplication.Repository.UserRepo;

@Service 
public class UserService {
    private final UserRepo userRepo;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    
    public UserService(UserRepo userRepo, JWTService jwtService, AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    //hash the password 12 times before saving it inside the db
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    public UserDto registerUser(RegisterRequest request) {
        User user = new User();

        String email = request.email();
        String phone = request.phone();
        String password = request.password();

        if (email == null || email.trim().isEmpty()) {
            throw new InvalidRegistrationException("Provide an email");
        }

        email = email.trim().toLowerCase(Locale.ROOT);
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidRegistrationException("Invalid email format");
        }

        if (userRepo.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email is already in use");
        }

        user.setEmail(email);

        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidRegistrationException("Provide a phone number");
        }
        String cleanPhone = phone.replaceAll("[\\s\\-]", "");

        if (!cleanPhone.matches("^\\+?[0-9]{9,15}$")) {
            throw new InvalidRegistrationException("Invalid phone number format");
        }

        if (userRepo.existsByPhone(cleanPhone)) {
            throw new UserAlreadyExistsException("Phone number is already in use");
        }

        user.setPhone(cleanPhone);

        if (password == null || password.trim().isEmpty() || password.length() <= 8) {
            throw new InvalidRegistrationException("Password must be longer than 8 characters");
        }

        user.setPasswordHash(encoder.encode(password));

        user.setCreatedAt(Instant.now());

        User savedUser = userRepo.save(user);

        return new UserDto(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getPhone(),
            savedUser.getRole(),
            savedUser.getAccountStatus(),
            savedUser.getCreatedAt()
        ); 
    }

    public TokenRespond loginUser(LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.loginId(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String username = userDetails.getUsername();

        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

        String token = jwtService.generateToken(username, role);
        
        return new TokenRespond(token);
    }
}
