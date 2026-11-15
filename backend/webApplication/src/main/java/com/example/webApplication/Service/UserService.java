package com.example.webApplication.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.webApplication.Dto.RegisterRequest;
import com.example.webApplication.Dto.UserDto;
import com.example.webApplication.Entity.User;
import com.example.webApplication.Repository.UserRepo;

public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    //hash the password 7 times before saving it inside the db
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(7);

    public UserDto registerUser(RegisterRequest request) throws Exception {
        User user = new User();

        String email = request.email();
        String password = request.password();

        if (email == null || email.trim().isEmpty()) {
            throw new Exception("error: Provide an Email");
        }

        email = email.trim();
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new Exception("error: Invalid Email format");
        }

        if (userRepo.existsByEmail(email)) {
            throw new Exception("error: Email in used");
        }

        user.setEmail(email);

        if (password == null || password.trim().isEmpty() || password.length() <= 8) {
            throw new Exception("error: Provide a secure email");
        }

        user.setPasswordHash(encoder.encode(password));

        User savedUser = userRepo.save(user);

        return new UserDto(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole(),
            savedUser.getAccountStatus(),
            savedUser.getCreatedAt()
        ); 
    }
}
