/*
 * ChatDtos.java
 *
 * Version: 1.0
 * Date: 2026-05-09
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Objects used by {@code ChatController}.
 * Grouped in a single file for readability; each DTO is a {@code public static} class.
 *
 * @version 1.0
 * @author EduLink Team
 */
public class ChatDtos {

    // -----------------------------------------------------------------------
    // Request DTOs
    // -----------------------------------------------------------------------

    /**
     * Request body for creating a new chat between two users.
     */
    public static class CreateChatRequest {

        /** The ID of the first participant (e.g. the student initiating the conversation). */
        private Integer userId1;

        /** The ID of the second participant (e.g. the tutor being contacted). */
        private Integer userId2;

        /** The initial number of unread messages in the chat, defaults to 0. */
        private Integer unreadCount = 0;

        public Integer getUserId1() { return userId1; }
        public void setUserId1(Integer userId1) { this.userId1 = userId1; }

        public Integer getUserId2() { return userId2; }
        public void setUserId2(Integer userId2) { this.userId2 = userId2; }

        public Integer getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
    }

    /**
     * Request body for sending a new message within an existing chat thread.
     */
    public static class SendMessageRequest {

        /** The ID of the user sending the message. */
        private Integer senderId;

        /** The text content of the message. */
        private String content;

        public Integer getSenderId() { return senderId; }
        public void setSenderId(Integer senderId) { this.senderId = senderId; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // -----------------------------------------------------------------------
    // Response DTOs
    // -----------------------------------------------------------------------

    /**
     * Response body containing the data of a single message returned to the client.
     */
    public static class MessageResponse {

        /** Unique identifier of the message. */
        private Integer id;

        /** ID of the user who sent the message. */
        private Integer senderId;

        /** Full name of the sender displayed in the UI. */
        private String senderName;

        /** Text content of the message. */
        private String content;

        /** Timestamp indicating when the message was sent. */
        private LocalDateTime sentAt;

        /** Indicator whether this specific message has been read by the recipient. */
        private Boolean isRead = false;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getSenderId() { return senderId; }
        public void setSenderId(Integer senderId) { this.senderId = senderId; }

        public String getSenderName() { return senderName; }
        public void setSenderName(String senderName) { this.senderName = senderName; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public LocalDateTime getSentAt() { return sentAt; }
        public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }

        public Boolean getIsRead() { return isRead; }
        public void setIsRead(Boolean isRead) { this.isRead = isRead; }
    }

    /**
     * Response body containing chat thread data displayed in the conversation list.
     * Includes a summarised participant list and the most recent message preview.
     */
    public static class ChatResponse {

        /** Unique identifier of the chat thread. */
        private Integer id;

        /** Timestamp indicating when the chat was created. */
        private LocalDateTime createdAt;

        /** List of participants in this chat thread. */
        private List<ParticipantInfo> participants;

        /** The most recent message in this chat, used as a preview in the list view. */
        private MessageResponse lastMessage;

        /** The number of unread messages in this chat thread for the current user. */
        private Integer unreadCount = 0;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public List<ParticipantInfo> getParticipants() { return participants; }
        public void setParticipants(List<ParticipantInfo> participants) { this.participants = participants; }

        public MessageResponse getLastMessage() { return lastMessage; }
        public void setLastMessage(MessageResponse lastMessage) { this.lastMessage = lastMessage; }

        public Integer getUnreadCount() { return unreadCount; }
        public void setUnreadCount(Integer unreadCount) { this.unreadCount = unreadCount; }
    }

    /**
     * Condensed participant data used within {@link ChatResponse}.
     */
    public static class ParticipantInfo {

        /** Unique identifier of the user. */
        private Integer id;

        /** Full name of the user displayed in the UI. */
        private String fullName;

        /** Role ID of the user within the system (e.g. 1=Admin, 2=Tutor, 3=Student). */
        private Integer roleId;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public Integer getRoleId() { return roleId; }
        public void setRoleId(Integer roleId) { this.roleId = roleId; }
    }
}