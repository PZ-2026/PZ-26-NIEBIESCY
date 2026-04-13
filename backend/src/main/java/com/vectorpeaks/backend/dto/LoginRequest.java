/*
 * LoginRequest.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object (DTO) for login requests.
 * Contains the user's email and password submitted during authentication.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class LoginRequest {
    /** The user's email address. */
    private String email;

    /** The user's plain-text password. */
    private String password;

}