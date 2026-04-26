/*
 * UserRepository.java
 *
 * Version: 1.1
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link User} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard database
 * access methods. Includes custom queries for finding users by email and
 * retrieving distinct cities.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the found user, or empty if none exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Retrieves a list of all distinct non-empty cities from user addresses.
     *
     * @return list of unique city names
     */
    @Query("SELECT DISTINCT u.address FROM User u WHERE u.address IS NOT NULL AND u.address != ''")
    List<String> findAllDistinctCities();
}