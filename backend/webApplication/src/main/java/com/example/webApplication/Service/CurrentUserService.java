package com.example.webApplication.Service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.webApplication.Entity.User;
import com.example.webApplication.Exception.ResourceNotFoundException;
import com.example.webApplication.Repository.UserRepo;

/**
 * Finds the logged-in user from the authentication set by the JWT filter.
 * The user is always taken from the token, never from the request data, so a client
 * cannot read or change another user's cart or orders.
 */
@Service
public class CurrentUserService {

    private final UserRepo userRepo;

    public CurrentUserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User require(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("User not found");
        }
        String login = authentication.getName();
        return userRepo.findByEmailOrPhone(login, login)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
