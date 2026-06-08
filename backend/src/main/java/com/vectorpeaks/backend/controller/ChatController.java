/*
 * ChatController.java
 *
 * Version: 1.2
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.ChatDtos.*;
import com.vectorpeaks.backend.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller handling chat-related HTTP requests in the EduLink system.
 *
 * <p>Exposes the following endpoints:
 * <ul>
 * <li>{@code POST /api/chats} – create or retrieve an existing chat thread</li>
 * <li>{@code GET  /api/chats/user/{userId}} – list all chats for a user</li>
 * <li>{@code GET  /api/chats/{chatId}/messages} – fetch message history</li>
 * <li>{@code POST /api/chats/{chatId}/messages} – send a new message</li>
 * </ul>
 * * <p>Includes built-in BOLA (Broken Object Level Authorization) protection and security event logging.
 *
 * @version 1.2
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/chats")
@PreAuthorize("isAuthenticated()")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private ChatService chatService;

    // -----------------------------------------------------------------------
    // Chat creation
    // -----------------------------------------------------------------------

    /**
     * Creates a new chat between two users or returns the existing one.
     * Validates if the authenticated user is one of the participants.
     *
     * @param request        request body containing the IDs of both participants
     * @param authentication the security context containing the logged-in user's details
     * @return {@code 200 OK} with the {@link ChatResponse}, or {@code 403 Forbidden} / {@code 400 Bad Request}
     */
    @PostMapping
    public ResponseEntity<?> createOrGetChat(@RequestBody CreateChatRequest request,
                                             Authentication authentication) {
        Integer loggedInUserId = (Integer) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin && !loggedInUserId.equals(request.getUserId1()) && !loggedInUserId.equals(request.getUserId2())) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to create a chat for third-party users {} and {}",
                    loggedInUserId, request.getUserId1(), request.getUserId2());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nie możesz utworzyć czatu dla osób trzecich.");
        }

        try {
            ChatResponse chat = chatService.getOrCreateChat(request);
            return ResponseEntity.ok(chat);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // User chat list
    // -----------------------------------------------------------------------

    /**
     * Returns all chat threads (conversations) for the given user.
     * Handled by method-level security to ensure users only access their own chats.
     *
     * @param userId the ID of the user whose chats are to be retrieved
     * @return {@code 200 OK} with a list of {@link ChatResponse} objects
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("#userId == authentication.principal or hasRole('ADMIN')")
    public ResponseEntity<List<ChatResponse>> getUserChats(@PathVariable Integer userId) {
        return ResponseEntity.ok(chatService.getChatsForUser(userId));
    }

    // -----------------------------------------------------------------------
    // Message history
    // -----------------------------------------------------------------------

    /**
     * Returns the full message history of the given chat thread.
     * The service layer verifies if the user is an active participant in this chat.
     *
     * @param chatId         the ID of the chat thread
     * @param authentication the security context containing the logged-in user's details
     * @return {@code 200 OK} with message list, {@code 403} if access denied, or {@code 404} if chat not found
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Integer chatId,
                                         Authentication authentication) {
        Integer loggedInUserId = (Integer) authentication.getPrincipal();

        try {
            return ResponseEntity.ok(chatService.getMessages(chatId, loggedInUserId));
        } catch (SecurityException e) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to unauthorizedly read Chat ID {}", loggedInUserId, chatId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Sending messages
    // -----------------------------------------------------------------------

    /**
     * Persists a new message sent by a user in the specified chat thread.
     * Verifies sender identity and participant status before saving.
     *
     * @param chatId         the ID of the target chat thread
     * @param request        request body containing the sender's ID and message content
     * @param authentication the security context containing the logged-in user's details
     * @return {@code 201 Created} with the saved message, or error status
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Integer chatId,
            @RequestBody SendMessageRequest request,
            Authentication authentication) {

        Integer loggedInUserId = (Integer) authentication.getPrincipal();

        if (!loggedInUserId.equals(request.getSenderId())) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to send a message acting as User ID {} on Chat ID {}",
                    loggedInUserId, request.getSenderId(), chatId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Nie możesz wysłać wiadomości jako inny użytkownik.");
        }

        try {
            MessageResponse saved = chatService.sendMessage(chatId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Marks all unread messages in the specified chat thread as read for the logged-in user.
     *
     * @param chatId         the ID of the chat thread to mark as read
     * @param authentication the security context containing the logged-in user's details
     * @return {@code 200 OK} on success, or error status
     */
    @PostMapping("/{chatId}/read")
    public ResponseEntity<?> markChatAsRead(
            @PathVariable Integer chatId,
            Authentication authentication) {

        Integer loggedInUserId = (Integer) authentication.getPrincipal();

        try {
            chatService.markChatAsRead(chatId, loggedInUserId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Blocks or unblocks a chat thread.
     * Only participants can perform this action.
     *
     * @param chatId         the ID of the chat to block/unblock
     * @param body           map containing "blocked" boolean
     * @param authentication the security context
     * @return updated ChatResponse or error
     */
    @PutMapping("/{chatId}/block")
    public ResponseEntity<?> toggleBlock(
            @PathVariable Integer chatId,
            @RequestBody Map<String, Boolean> body,
            Authentication authentication) {

        Boolean blocked = body.get("blocked");
        if (blocked == null) {
            return ResponseEntity.badRequest().body("Field 'blocked' is required.");
        }

        Integer loggedInUserId = (Integer) authentication.getPrincipal();

        try {
            ChatResponse response = chatService.toggleBlock(chatId, loggedInUserId, blocked);
            return ResponseEntity.ok(response);
        } catch (SecurityException e) {
            logger.warn("SECURITY ALERT (BOLA): User ID {} attempted to block/unblock Chat ID {} without access",
                    loggedInUserId, chatId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}