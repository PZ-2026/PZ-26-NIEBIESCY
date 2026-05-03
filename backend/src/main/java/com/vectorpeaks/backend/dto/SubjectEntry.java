/*
 * SubjectEntry.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object representing a subject with its review count.
 * Used in admin reports for popular subjects ranking.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class SubjectEntry {
    private String name;
    private int reviewCount;
}
