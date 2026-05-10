/*
 * GlobalLimitDto.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object for global platform settings.
 * Used for reading and updating global limits.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class GlobalLimitDto {
    private Double maxPricePerHour;
    private String globalMessage;
}
