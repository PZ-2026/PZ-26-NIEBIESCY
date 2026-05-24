/*
 * ChatService.java
 *
 * Version: 1.2
 * Date: 2026-05-24
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
 * </ul>
 *
 * @version 1.2
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
        if (existing.isPresent()) {
            return toResponse(existing.get());
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

        return toResponse(chat);
    }

    // -----------------------------------------------------------------------
    // Retrieving chats
    // -----------------------------------------------------------------------

    /**
     * Returns all chat threads in which the specified user participates,
     * ordered by most recent activity.
     *
     * @param userId the ID of the user
     * @return list of {@link ChatResponse} objects
     */
    @Transactional(readOnly = true)
    public List<ChatResponse> getChatsForUser(Integer userId) {
        return chatRepository.findByParticipants_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
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
    // Entity → DTO mapping
    // -----------------------------------------------------------------------

    /**
     * Converts a {@link Chat} entity to a {@link ChatResponse} DTO,
     * including participant info and the most recent message preview.
     *
     * @param chat the chat entity to convert
     * @return the corresponding {@link ChatResponse}
     */
    private ChatResponse toResponse(Chat chat) {
        ChatResponse dto = new ChatResponse();
        dto.setId(chat.getId());
        dto.setCreatedAt(chat.getCreatedAt());

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
        return dto;
    }
}