/*
 * GlobalMessageDto.java
 *
 * Version: 1.0
 * Date: 2026-06-08
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object for the global admin message displayed to all users.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class GlobalMessageDto {
    private String message;
}
