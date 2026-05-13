/*
 * ChatController.java
 *
 * Version: 1.0
 * Date: 2026-05-09
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.vectorpeaks.backend.dto.ChatDtos.*;
import com.vectorpeaks.backend.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller handling chat-related HTTP requests in the EduLink system.
 *
 * <p>Exposes the following endpoints:
 * <ul>
 *   <li>{@code POST /api/chats} – create or retrieve an existing chat thread,</li>
 *   <li>{@code GET  /api/chats/user/{userId}} – list all chats for a user,</li>
 *   <li>{@code GET  /api/chats/{chatId}/messages} – fetch message history,</li>
 *   <li>{@code POST /api/chats/{chatId}/messages} – send a new message.</li>
 * </ul>
 *
 * @version 1.0
 * @author EduLink Team
 */
@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = "*") // For development only – restrict in production
public class ChatController {

    @Autowired
    private ChatService chatService;

    // -----------------------------------------------------------------------
    // Chat creation
    // -----------------------------------------------------------------------

    /**
     * Creates a new chat between two users or returns the existing one.
     * Typically called when a student taps "Message tutor" on the offer screen.
     *
     * @param request request body containing the IDs of both participants
     * @return {@code 200 OK} with the {@link ChatResponse}, or
     *         {@code 400 Bad Request} if either user does not exist
     */
    @PostMapping
    public ResponseEntity<?> createOrGetChat(@RequestBody CreateChatRequest request) {
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
     * Returns all chat threads (conversations) for the given user,
     * ordered by most recent activity.
     *
     * @param userId the ID of the user whose chats are to be retrieved
     * @return {@code 200 OK} with a list of {@link ChatResponse} objects
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ChatResponse>> getUserChats(@PathVariable Integer userId) {
        return ResponseEntity.ok(chatService.getChatsForUser(userId));
    }

    // -----------------------------------------------------------------------
    // Message history
    // -----------------------------------------------------------------------

    /**
     * Returns the full message history of the given chat thread in
     * chronological order. Called when a user opens a specific conversation.
     *
     * @param chatId the ID of the chat thread
     * @return {@code 200 OK} with a list of {@link MessageResponse} objects, or
     *         {@code 404 Not Found} if the chat does not exist
     */
    @GetMapping("/{chatId}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Integer chatId) {
        try {
            return ResponseEntity.ok(chatService.getMessages(chatId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Sending messages
    // -----------------------------------------------------------------------

    /**
     * Persists a new message sent by a user in the specified chat thread.
     * May trigger an FCM push notification to the recipient after saving.
     *
     * @param chatId  the ID of the target chat thread
     * @param request request body containing the sender's ID and message content
     * @return {@code 201 Created} with the saved {@link MessageResponse}, or
     *         {@code 400 Bad Request} if the chat or sender does not exist
     */
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<?> sendMessage(
            @PathVariable Integer chatId,
            @RequestBody SendMessageRequest request) {
        try {
            MessageResponse saved = chatService.sendMessage(chatId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}