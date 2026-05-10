/*
 * GlobalLimitRepository.java
 *
 * Version: 1.0
 * Date: 2026-05-03
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.GlobalLimit;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository interface for performing CRUD operations on {@link GlobalLimit} entities.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface GlobalLimitRepository extends JpaRepository<GlobalLimit, Integer> {
}
