/*
 * ChatService.java
 *
 * Version: 1.3
 * Date: 2026-05-29
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.service;

import com.vectorpeaks.backend.dto.ChatDtos.*;
import com.vectorpeaks.backend.entity.Chat;
import com.vectorpeaks.backend.entity.Message;
import com.vectorpeaks.backend.entity.User;
import com.vectorpeaks.backend.repository.ChatRepository;
import com.vectorpeaks.backend.repository.MessageRepository;
import com.vectorpeaks.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service class responsible for the business logic of the chat feature.
 *
 * <p>Handles:
 * <ul>
 * <li>creating new chat threads (with deduplication),</li>
 * <li>retrieving the list of chats for a given user,</li>
 * <li>fetching message history (with participant verification),</li>
 * <li>persisting and broadcasting new messages via FCM.</li>
 * <li>marking messages as read.</li>
 * </ul>
 *
 * @version 1.3
 * @author EduLink Team
 */
@Service
public class ChatService {

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FcmService fcmService;

    @Autowired
    private FcmTokenService fcmTokenService;

    // -----------------------------------------------------------------------
    // Chat creation
    // -----------------------------------------------------------------------

    /**
     * Creates a new direct chat between two users, or returns the existing one
     * if a conversation between them already exists (prevents duplicate threads).
     *
     * @param request request containing the IDs of both participants
     * @return the created or existing {@link ChatResponse}
     * @throws IllegalArgumentException if either user does not exist
     */
    @Transactional
    public ChatResponse getOrCreateChat(CreateChatRequest request) {
        // Return the existing chat if one already exists between these two users
        Optional<Chat> existing = chatRepository.findDirectChat(
                request.getUserId1(), request.getUserId2());

        // Since we are creating/initiating, we can pass either user ID for the initial unread mapping,
        // usually 0 unread on creation. We'll pass the initiator's ID.
        if (existing.isPresent()) {
            return toResponse(existing.get(), request.getUserId1());
        }

        User user1 = userRepository.findById(request.getUserId1())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + request.getUserId1()));
        User user2 = userRepository.findById(request.getUserId2())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found with id: " + request.getUserId2()));

        Chat chat = new Chat();
        chat.setParticipants(Set.of(user1, user2));
        chatRepository.save(chat);

        return toResponse(chat, request.getUserId1());
    }

    // -----------------------------------------------------------------------
    // Retrieving chats
    // -----------------------------------------------------------------------

    /**
     * Returns all chat threads in which the specified user participates,
     * ordered by most recent activity. Calculates unread message counts.
     *
     * @param userId the ID of the user requesting their chats
     * @return list of {@link ChatResponse} objects
     */
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForUser(Integer userId) {
        return chatRepository.findByParticipants_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(chat -> toResponse(chat, userId))
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Message history
    // -----------------------------------------------------------------------

    /**
     * Returns the full message history of a chat thread in chronological order.
     * Validates if the requesting user is an active participant of the chat.
     *
     * @param chatId         the ID of the chat thread
     * @param loggedInUserId the ID of the user requesting the history
     * @return list of {@link MessageResponse} objects ordered oldest-first
     * @throws IllegalArgumentException if no chat exists with the given ID
     * @throws SecurityException        if the user is not a participant of the chat
     */
    @Transactional(readOnly = true)
    public List<MessageResponse> getMessages(Integer chatId, Integer loggedInUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id: " + chatId));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(loggedInUserId));

        if (!isParticipant) {
            throw new SecurityException("Access denied: You are not a participant in this conversation.");
        }

        return messageRepository.findByChat_IdOrderBySentAtAsc(chatId)
                .stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Sending messages
    // -----------------------------------------------------------------------

    /**
     * Persists a new message sent by a user in the given chat thread.
     * Validates if the sender belongs to the chat and triggers FCM push notifications
     * to alert the other participant in real time.
     *
     * @param chatId  the ID of the target chat thread
     * @param request request containing the sender's ID and message content
     * @return the persisted message as a {@link MessageResponse}
     * @throws IllegalArgumentException if the chat, sender does not exist, or sender is not a participant
     */
    @Transactional
    public MessageResponse sendMessage(Integer chatId, SendMessageRequest request) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found with id: " + chatId));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(request.getSenderId()));

        if (!isParticipant) {
            throw new IllegalArgumentException("You cannot send messages to a chat you do not belong to.");
        }

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + request.getSenderId()));

        Message message = new Message();
        message.setChat(chat);
        message.setSender(sender);
        message.setContent(request.getContent());
        // isRead is false by default from the entity definition
        messageRepository.save(message);

        String senderName = sender.getFirstName() + " " + sender.getLastName();
        chat.getParticipants().stream()
                .filter(p -> !p.getId().equals(sender.getId()))
                .forEach(recipient -> {
                    fcmTokenService.getTokensForUser(recipient.getId())
                            .forEach(token -> fcmService.sendNotification(
                                    token,
                                    senderName,
                                    message.getContent()
                            ));
                });

        return toMessageResponse(message);
    }

    // -----------------------------------------------------------------------
    // Mark as Read
    // -----------------------------------------------------------------------

    /**
     * Marks all unread messages in a given chat as read, targeting only the messages
     * that were sent by the OTHER participant.
     *
     * @param chatId         the ID of the chat thread
     * @param loggedInUserId the ID of the user reading the messages
     */
    @Transactional
    public void markChatAsRead(Integer chatId, Integer loggedInUserId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found"));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(loggedInUserId));

        if (!isParticipant) {
            throw new IllegalArgumentException("You are not a participant of this chat.");
        }

        // Fetch messages where this user is NOT the sender and isRead is false
        List<Message> unreadMessages = messageRepository
                .findByChat_IdAndSender_IdNotAndIsReadFalse(chatId, loggedInUserId);

        // Safe, JPA-managed update
        for (Message message : unreadMessages) {
            message.setRead(true);
        }

        // Hibernate will auto-update these entities upon transaction commit
    }

    // -----------------------------------------------------------------------
    // Entity → DTO mapping
    // -----------------------------------------------------------------------

    /**
     * Converts a {@link Chat} entity to a {@link ChatResponse} DTO,
     * including participant info, the most recent message preview, and unread counts.
     *
     * @param chat   the chat entity to convert
     * @param userId the ID of the user retrieving the data (used for calculating unread messages)
     * @return the corresponding {@link ChatResponse}
     */
    private ChatResponse toResponse(Chat chat, Integer userId) {
        ChatResponse dto = new ChatResponse();
        dto.setId(chat.getId());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setIsBlocked(chat.isBlocked());
        dto.setBlockedBy(chat.getBlockedBy());

        List<ParticipantInfo> participants = chat.getParticipants()
                .stream()
                .map(u -> {
                    ParticipantInfo info = new ParticipantInfo();
                    info.setId(u.getId());
                    info.setFullName(u.getFirstName() + " " + u.getLastName());
                    info.setRoleId(u.getRoleId());
                    return info;
                })
                .collect(Collectors.toList());
        dto.setParticipants(participants);

        // Calculate unread messages
        long unreadCount = messageRepository.countByChat_IdAndSender_IdNotAndIsReadFalse(chat.getId(), userId);
        dto.setUnreadCount((int) unreadCount);

        // Attach the most recent message as a preview for the conversation list
        messageRepository.findByChat_IdOrderBySentAtAsc(chat.getId())
                .stream()
                .max(Comparator.comparing(Message::getSentAt))
                .ifPresent(last -> dto.setLastMessage(toMessageResponse(last)));

        return dto;
    }

    /**
     * Converts a {@link Message} entity to a {@link MessageResponse} DTO.
     *
     * @param message the message entity to convert
     * @return the corresponding {@link MessageResponse}
     */
    private MessageResponse toMessageResponse(Message message) {
        MessageResponse dto = new MessageResponse();
        dto.setId(message.getId());
        dto.setContent(message.getContent());
        dto.setSentAt(message.getSentAt());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getFirstName()
                + " " + message.getSender().getLastName());
        dto.setIsRead(message.isRead());
        return dto;
    }

    /**
     * Toggles the blocked state of a chat thread.
     * Only a participant of the chat can block/unblock it.
     *
     * @param chatId         the ID of the chat to block/unblock
     * @param loggedInUserId the ID of the user performing the action
     * @param block          true to block, false to unblock
     * @throws IllegalArgumentException if chat not found or user is not a participant
     */
    @Transactional
    public ChatResponse toggleBlock(Integer chatId, Integer loggedInUserId, boolean block) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Chat not found: " + chatId));

        boolean isParticipant = chat.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(loggedInUserId));

        if (!isParticipant) {
            throw new SecurityException("Access denied: You are not a participant in this conversation.");
        }

        chat.setBlocked(block);
        chat.setBlockedBy(block ? loggedInUserId : null);
        chatRepository.save(chat);

        return toResponse(chat, loggedInUserId);
    }
}