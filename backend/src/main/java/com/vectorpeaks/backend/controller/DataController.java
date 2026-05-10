/*
 * DataController.java
 *
 * Version: 1.1
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.SubjectDto;
import com.vectorpeaks.backend.repository.SubjectRepository;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides endpoints for retrieving reference data used in the frontend,
 * such as distinct subject names and cities.
 *
 * @version 1.1
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/data")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class DataController {

    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;

    /**
     * Constructs a new DataController with required repositories.
     *
     * @param subjectRepository repository for subjects
     * @param userRepository    repository for users
     */
    public DataController(SubjectRepository subjectRepository, UserRepository userRepository) {
        this.subjectRepository = subjectRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves a list of all distinct subject names available in the system.
     *
     * @return list of subject names
     */
    @GetMapping("/subjects")
    public List<String> getSubjects() {
        return subjectRepository.findAllDistinctNames();
    }

    /**
     * Retrieves a list of all distinct cities from tutor addresses.
     *
     * @return list of city names
     */
    @GetMapping("/cities")
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
    public List<SubjectDto> getSubjectsWithId() {
        return subjectRepository.findAll().stream()
                .map(s -> new SubjectDto(s.getId(), s.getName()))
                .collect(Collectors.toList());
    }
}