/*
 * LoginResponse.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */
package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for login responses.
 * Contains user information returned after successful authentication.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class LoginResponse {
    /** The unique identifier of the user. */
    private Integer id;

    /** The user's first name. */
    private String firstName;

    /** The user's last name. */
    private String lastName;

    /** The user's email address. */
    private String email;

    /** The role of the user (e.g., "STUDENT", "TUTOR", "ADMIN"). */
    private String role;

    /** The user's token used to authenticate the user's requests. */
    private String token;
}