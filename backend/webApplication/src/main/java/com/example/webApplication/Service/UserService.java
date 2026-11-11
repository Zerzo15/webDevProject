package com.example.webApplication.Service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.example.webApplication.Repository.UserRepo;

public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    //hash the password 7 times before saving it inside the db
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(7);
}
