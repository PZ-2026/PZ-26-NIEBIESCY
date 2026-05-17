/*
 * SubjectDto.java
 *
 * Version: 1.0
 * Date: 2026-04-28
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */
package com.vectorpeaks.backend.dto;

/**
 * Data Transfer Object (DTO) for a subject.
 * Contains the subject's unique identifier and its display name.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class SubjectDto {

    /** Unique identifier of the subject. */
    private Integer id;

    /** Name of the subject (e.g., "Mathematics", "English"). */
    private String name;

    /**
     * Constructs a new SubjectDto with the given ID and name.
     *
     * @param id   the subject identifier
     * @param name the subject name
     */
    public SubjectDto(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
