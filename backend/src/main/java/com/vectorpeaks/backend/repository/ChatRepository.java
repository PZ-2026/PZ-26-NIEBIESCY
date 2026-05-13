/*
 * ChatRepository.java
 *
 * Version: 1.0
 * Date: 2026-05-09
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for performing CRUD operations on {@link Chat} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard database
 * access methods. Includes a derived query for participant lookup and a custom
 * query for direct chat deduplication.
 *
 * @version 1.0
 * @author EduLink Team
 */
public interface ChatRepository extends JpaRepository<Chat, Integer> {

    /**
     * Returns all chats in which the given user is a participant,
     * ordered by creation date descending (most recent first).
     *
     * @param id the ID of the user whose chats are to be retrieved
     * @return list of chats the user participates in
     */
    List<Chat> findByParticipants_IdOrderByCreatedAtDesc(Integer id);

    /**
     * Checks whether a direct (two-participant) chat already exists between
     * two specific users. Used when opening a new conversation to avoid
     * creating duplicate chat threads.
     * A custom {@code @Query} is required here because the logic depends on
     * both participant IDs and an exact participant count of two.
     *
     * @param userId1 the ID of the first user
     * @param userId2 the ID of the second user
     * @return an {@link Optional} containing the existing chat, or empty if none found
     */
    @Query("""
        SELECT c FROM Chat c
        JOIN c.participants p1
        JOIN c.participants p2
        WHERE p1.id = :userId1 AND p2.id = :userId2
        AND SIZE(c.participants) = 2
        """)
    Optional<Chat> findDirectChat(
            @Param("userId1") Integer userId1,
            @Param("userId2") Integer userId2);
}