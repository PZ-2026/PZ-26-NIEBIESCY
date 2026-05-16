/*
 * CustomUserDetailsService.java
 *
 * Version: 1.0
 * Date: 2026-05-15
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.security;

import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

/**
 * Custom UserDetailsService implementation that loads user details from the database.
 *
 * <p>Spring Security uses this service to authenticate users based on email
 * instead of the default username. This replaces the default in-memory user
 * configuration.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads user details from the database using email as the username.
     *
     * <p>Spring Security calls this method during authentication to fetch user
     * credentials and permissions.
     *
     * @param email the user's email address (used as username)
     * @return UserDetails object containing user credentials and authorities
     * @throws UsernameNotFoundException if user is not found in the database
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        User user = userOpt.get();

        // Convert role ID to authority
        String role = user.getRoleName();
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(role);

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountLocked(user.getAccountStatusId() == 2)  // 2 = BLOCKED/LOCKED
                .disabled(user.getAccountStatusId() != 1)       // 1 = ACTIVE
                .build();
    }
}
