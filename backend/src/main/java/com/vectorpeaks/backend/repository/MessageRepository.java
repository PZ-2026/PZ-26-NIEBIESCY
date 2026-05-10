/*
 * MessageRepository.java
 *
 * Version: 1.0
 * Date: 2026-05-09
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Message} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard database
 * access methods.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Returns all messages belonging to the given chat thread,
     * ordered chronologically by send time ascending (oldest first).
     *
     * @param chatId the ID of the chat thread
     * @return list of messages in chronological order
     */
    List<Message> findByChat_IdOrderBySentAtAsc(Integer chatId);
}