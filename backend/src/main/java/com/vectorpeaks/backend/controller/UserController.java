/*
 * UserController.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Manages user-related operations.
 * Provides endpoints to retrieve and add users, and to update profile data.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class UserController {

    private final UserRepository userRepository;

    /**
     * Constructs a new UserController with the given UserRepository.
     *
     * @param userRepository the user repository used for database operations
     */
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Returns a list of all users.
     *
     * @return list of all users in the system
     */
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Adds a new user to the database.
     *
     * @param user the user to be added
     * @return the saved user (including generated ID)
     */
    @PostMapping
    public User addUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id the user ID
     * @return the user (without sensitive data like password)
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        return userOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user's profile (address and phone number).
     * Validation: phone number must be 9 digits, address at least 2 characters.
     *
     * @param id          the user ID
     * @param updatedUser object containing new address and phone number
     * @return updated user or error message if validation fails or user not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Integer id,
                                        @RequestBody User updatedUser) {
        String phone = updatedUser.getPhoneNumber();
        if (phone != null && !phone.isEmpty()) {
            if (!phone.matches("\\d{9}")) {
                return ResponseEntity.badRequest()
                        .body("Numer telefonu musi zawierać 9 cyfr");
            }
        }

        String address = updatedUser.getAddress();
        if (address != null && address.length() < 2) {
            return ResponseEntity.badRequest()
                    .body("Adres musi zawierać co najmniej 2 znaki");
        }

        return userRepository.findById(id).map(user -> {
            if (address != null) {
                user.setAddress(address);
            }
            if (phone != null) {
                user.setPhoneNumber(phone);
            }
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }
}