/*
 * UserController.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Manages user-related operations.
 * Provides endpoints to retrieve and add users.
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class UserController {

    private final UserRepository repo;

    /**
     * Constructs a new UserController with the given UserRepository.
     *
     * @param repo the user repository used for database operations
     */

    public UserController(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns a list of all users.
     *
     * @return list of all users in the system
     */
    @GetMapping
    public List<User> getAllUsers() {
        return repo.findAll();
    }

    /**
     * Adds a new user to the database.
     *
     * @param user the user to be added
     * @return the saved user (including generated ID)
     */
    @PostMapping
    public User addUser(@RequestBody User user) {
        return repo.save(user);
    }
}