/*
 * RegisterRequest.java
 *
 * Version: 1.0
 * Date: 2026-05-02
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for user registration requests.
 * Contains all necessary information to create a new user account.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class RegisterRequest {
    /** First name of the user. */
    private String firstName;
    /** Last name of the user. */
    private String lastName;
    /** Email address (used as login). */
    private String email;
    /** Plain-text password (will be hashed before storage). */
    private String password;
    /** Role ID: 2 = Student, 3 = Tutor. */
    private Integer roleId;
    /** City of residence. */
    private String city;
    /** Phone number (9 digits). */
    private String phoneNumber;
}