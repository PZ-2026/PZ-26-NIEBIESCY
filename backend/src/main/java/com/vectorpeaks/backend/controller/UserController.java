/*
 * UserController.java
 *
 * Version: 1.2
 * Date: 2026-05-17
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.RegisterRequest;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.Map;

/**
 * Manages user-related operations.
 * Provides endpoints to retrieve and add users, update profile data,
 * anonymize accounts (GDPR deletion), and register new users.
 *
 * @version 1.2
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

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

    /**
     * Anonymizes a user account (GDPR‑style deletion).
     * All personal data are overwritten, the account becomes unusable.
     *
     * @param id the user ID
     * @return ResponseEntity with success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = userOpt.get();

        String anonymizedEmail = "deleted_" + user.getId() + "@deleted.local";
        user.setFirstName("Deleted");
        user.setLastName("User");
        user.setEmail(anonymizedEmail);
        user.setPassword("");
        user.setPhoneNumber(null);
        user.setAddress(null);
        user.setAccountStatusId(9);

        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates a user's account status (e.g., block/unblock).
     *
     * @param id          the user ID
     * @param body        map containing "accountStatusId" key
     * @return updated user or error if user not found
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Integer id,
                                              @RequestBody Map<String, Integer> body) {
        Integer newStatusId = body.get("accountStatusId");
        if (newStatusId == null) {
            return ResponseEntity.badRequest().body("accountStatusId is required");
        }
        return userRepository.findById(id).map(user -> {
            user.setAccountStatusId(newStatusId);
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Registers a new user account with validation and password hashing.
     * All fields are required, email must be valid and unique, role must be 2 or 3.
     *
     * @param request the registration data
     * @return ResponseEntity with success or error message
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String email = request.getEmail();
        logger.info("Registration attempt for email: {}", email);

        if (request.getFirstName() == null || request.getFirstName().isBlank() ||
                request.getLastName() == null || request.getLastName().isBlank() ||
                email == null || email.isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            logger.error("Registration failed – missing required fields for email: {}", email);
            return ResponseEntity.badRequest().body("Wszystkie pola są wymagane");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            logger.error("Registration failed – invalid email format: {}", email);
            return ResponseEntity.badRequest().body("Nieprawidłowy adres e-mail");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            logger.error("Registration failed – email already in use: {}", email);
            return ResponseEntity.badRequest().body("Użytkownik z takim adresem email już istnieje");
        }

        if (request.getRoleId() == null || (request.getRoleId() != 2 && request.getRoleId() != 3)) {
            logger.error("Registration failed – invalid roleId: {} for email: {}",
                    request.getRoleId(), email);
            return ResponseEntity.badRequest().body("Nieprawidłowa rola");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(request.getPassword());

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRoleId(request.getRoleId());
        user.setAccountStatusId(1);
        user.setAddress(request.getCity());
        user.setPhoneNumber(request.getPhoneNumber());

        userRepository.save(user);
        logger.info("Registration successful for email: {}, roleId: {}", email, request.getRoleId());
        return ResponseEntity.ok().build();
    }

    /**
     * Updates the FCM registration token for the specified user.
     * Called by the mobile app after login to enable push notifications.
     *
     * @param id   the ID of the user whose token is being updated
     * @param body request body containing the {@code fcmToken} field
     * @return {@code 200 OK} if updated, {@code 404 Not Found} if user does not exist
     */
    @PutMapping("/{id}/fcm-token")
    public ResponseEntity<?> updateFcmToken(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        return userRepository.findById(id).map(user -> {
            user.setFcmToken(body.get("fcmToken"));
            userRepository.save(user);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}