/*
 * SubjectRepository.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Subject} entities.
 * Provides a custom query to retrieve all distinct subject names.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface SubjectRepository extends JpaRepository<Subject, Integer> {

    /**
     * Retrieves a list of all distinct subject names from the subjects table.
     *
     * @return list of unique subject names
     */
    @Query("SELECT DISTINCT s.name FROM Subject s")
    List<String> findAllDistinctNames();
}