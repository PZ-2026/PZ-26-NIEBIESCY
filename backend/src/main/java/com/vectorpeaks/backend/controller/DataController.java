/*
 * DataController.java
 *
 * Version: 1.2
 * Date: 2026-05-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.SubjectDto;
import com.vectorpeaks.backend.repository.SubjectRepository;
import com.vectorpeaks.backend.repository.UserRepository;
import com.vectorpeaks.backend.repository.GlobalLimitRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides endpoints for retrieving reference data used in the frontend,
 * such as distinct subject names and cities.
 *
 * @version 1.2
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class DataController {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final GlobalLimitRepository globalLimitRepository;

    /**
     * Constructs a new DataController with required repositories.
     *
     * @param subjectRepository     repository for subjects
     * @param userRepository        repository for users
     * @param globalLimitRepository repository for global limits
     */
    public DataController(SubjectRepository subjectRepository,
                          UserRepository userRepository,
                          GlobalLimitRepository globalLimitRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
        this.globalLimitRepository = globalLimitRepository;
    }

    /**
     * Retrieves a list of all distinct subject names available in the system.
     *
     * @return list of subject names
     */
    @GetMapping("/subjects")
    @PreAuthorize("isAuthenticated()")
    public List<String> getSubjects() {
        return subjectRepository.findAllDistinctNames();
    }

    /**
     * Retrieves a list of all distinct cities from tutor addresses.
     *
     * @return list of city names
     */
    @GetMapping("/cities")
    @PreAuthorize("isAuthenticated()")
    public List<String> getCities() {
        return userRepository.findAllDistinctCities();
    }

    /**
     * Retrieves a list of all subjects with their IDs and names.
     * Used for creating offers where the subject ID is required.
     *
     * @return list of SubjectDto objects
     */
    @GetMapping("/subjects-with-id")
    @PreAuthorize("isAuthenticated()")
    public List<SubjectDto> getSubjectsWithId() {
        return subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName()))
                .collect(Collectors.toList());
    }

    /**
     * Retrieves the global maximum hourly price limit set by the administrator.
     * Used by tutors to validate offer prices before submission.
     *
     * @return the maximum allowed price per hour, or 200.0 if not configured
     */

    @GetMapping("/price-limit")
    public ResponseEntity<Double> getPriceLimit() {
        return globalLimitRepository.findById(1)
                .map(l -> ResponseEntity.ok(l.getHourlyPriceLimit().doubleValue()))
                .orElse(ResponseEntity.ok(200.0));
    }
}