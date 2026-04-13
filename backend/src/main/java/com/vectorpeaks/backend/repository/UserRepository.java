/*
 * UserRepository.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link User} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard database
 * access methods.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the found user, or empty if none
     *         exists with the given email
     */
    Optional<User> findByEmail(String email);
}