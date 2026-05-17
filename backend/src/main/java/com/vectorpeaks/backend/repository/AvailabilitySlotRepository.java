    /*
     * AvailabilitySlotRepository.java
     *
     * Version: 1.0
     * Date: 2026-04-26
     *
     * Copyright (c) 2026 EduLink Team. All rights reserved.
     *
     * This software is the confidential and proprietary information of EduLink.
     */

    package com.vectorpeaks.backend.repository;

    import com.vectorpeaks.backend.entity.AvailabilitySlot;
    import org.springframework.data.jpa.repository.JpaRepository;

    /**
     * Repository interface for performing CRUD operations on {@link AvailabilitySlot} entities.
     * Extends Spring Data JPA's {@link JpaRepository} to provide standard database access methods.
     *
     * @version 1.0
     * @author EduLink Team
     */
    public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Integer> {
    }