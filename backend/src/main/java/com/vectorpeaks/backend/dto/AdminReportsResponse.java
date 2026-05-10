/*
 * AdminReportsResponse.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;
import java.util.List;

/**
 * Data Transfer Object for admin reports and statistics.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class AdminReportsResponse {
    private long totalBookings;
    private long totalOffers;
    private List<SubjectEntry> popularSubjects;
}
