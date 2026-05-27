/*
 * Subject.java
 *
 * Version: 1.0
 * Date: 2026-04-26
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;

/**
 * Entity representing a subject that can be taught or requested.
 * Subjects are used to categorize offers and tutor specializations.
 *
 * @version 1.0
 * @author EduLink Team
 */
@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /** Name of the subject (e.g., "Mathematics", "English"). */
    private String name;

    /** Status identifier (e.g., active, inactive). */
    @Column(name = "status_id")
    private Integer statusId;



    // Getters and setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getStatusId() { return statusId; }
    public void setStatusId(Integer statusId) { this.statusId = statusId; }

}