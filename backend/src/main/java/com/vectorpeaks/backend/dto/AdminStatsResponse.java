/*
 * AdminStatsResponse.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.dto;

import lombok.Data;

/**
 * Data Transfer Object for admin dashboard statistics.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Data
public class AdminStatsResponse {
    private long totalUsers;
    private long totalOffers;
    private long totalBookings;
    private long tutorsCount;
    private long studentsCount;
    private long pendingCount;
}
