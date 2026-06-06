package com.vectorpeaks.backend.repository;

import com.vectorpeaks.backend.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repository interface for performing CRUD operations on {@link Message} entities.
 * Extends Spring Data JPA's {@link JpaRepository} to provide standard database
 * access methods.
 *
 * @version 1.1
 * @author EduLink Team
 */
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Retrieves all messages belonging to a specific chat thread,
     * ordered chronologically by their creation time in ascending order (oldest first).
     *
     * @param chatId the ID of the chat thread
     * @return a list of messages ordered by sent time
     */
    List<Message> findByChat_IdOrderBySentAtAsc(Integer chatId);

    /**
     * Counts the number of unread messages in a specific chat thread
     * that were sent by a user other than the specified sender.
     *
     * @param chatId   the ID of the chat thread
     * @param senderId the ID of the current user (to exclude their own messages)
     * @return the total count of unread messages from other users
     */
    long countByChat_IdAndSender_IdNotAndIsReadFalse(Integer chatId, Integer senderId);

    /**
     * Retrieves a list of unread messages in a specific chat thread
     * that were sent by a user other than the specified sender.
     * This is typically used to fetch messages for batch-updating their read status in Java.
     *
     * @param chatId   the ID of the chat thread
     * @param senderId the ID of the current user (to exclude their own messages)
     * @return a list of unread messages from other users
     */
    List<Message> findByChat_IdAndSender_IdNotAndIsReadFalse(Integer chatId, Integer senderId);
}