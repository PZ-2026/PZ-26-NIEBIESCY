/*
 * RoleID.java
 *
 * Version: 1.0
 * Date: 2026-04-13
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.entity;

/**
 * Enumeration of user roles within the EduLink system.
 * Defines the three possible privilege levels.
 *
 * @version 1.0
 * @author EduLink Team
 */
public enum RoleID {
    /** Student role – can browse offers and book lessons. */
    STUDENT,

    /** Tutor role – can create offers and manage reservations. */
    TUTOR,

    /** Administrator role – has full system access. */
    ADMIN;
}