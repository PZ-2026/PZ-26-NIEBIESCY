/*
 * Message.java
 *
 * Version: 1.1
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a single message sent within a chat thread.
 * Mapped to the {@code messages} table in the database.
 *
 * @version 1.1
 * @author EduLink Team
 */
@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    /**
     * The chat thread this message belongs to.
     * Foreign key referencing the {@code chats} table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    /** The text content of the message. */
    @Column(name = "content", nullable = false)
    private String content;

    /** Timestamp indicating when this message was sent. */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * The user who sent this message.
     * Foreign key referencing the {@code users} table.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User sender;

    /** Indicator whether the message has been read by the recipient. */
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    /** Sets {@code sentAt} to the current timestamp before the first persist if not already set. */
    @PrePersist
    protected void onSend() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    // Getters and setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Chat getChat() { return chat; }
    public void setChat(Chat chat) { this.chat = chat; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { this.isRead = read; }
}