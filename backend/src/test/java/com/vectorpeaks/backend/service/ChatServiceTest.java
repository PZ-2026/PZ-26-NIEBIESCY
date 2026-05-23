/*
 * ChatServiceTest.java
 *
 * Version: 1.0
 * Date: 2026-05-18
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChatService}.
 *
 * <p>Verifies business logic, repository interactions, and push notification
 * triggers using pure Mockito unit tests.
 *
 * @version 1.0
 * @author EduLink Team
 * @see ChatService
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    /** Mock of the chat database layer. */
    @Mock
    private ChatRepository chatRepository;

    /** Mock of the message database layer. */
    @Mock
    private MessageRepository messageRepository;

    /** Mock of the user database layer. */
    @Mock
    private UserRepository userRepository;

    /** Mock of the Firebase Cloud Messaging notification sender service. */
    @Mock
    private FcmService fcmService;

    /** Mock of the user device tokens management service. */
    @Mock
    private FcmTokenService fcmTokenService;

    /** The instance under test with injected mocks. */
    @InjectMocks
    private ChatService chatService;

    /** Reusable test participant instance representing a student. */
    private User user1;

    /** Reusable test participant instance representing a tutor. */
    private User user2;

    /** Reusable chat entity stub configured with user1 and user2. */
    private Chat sampleChat;

    /**
     * Prepares test fixtures and common domain entity stubs before each test method execution.
     */
    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setId(1);
        user1.setFirstName("John");
        user1.setLastName("Student");
        user1.setRoleId(3);

        user2 = new User();
        user2.setId(2);
        user2.setFirstName("Anna");
        user2.setLastName("Tutor");
        user2.setRoleId(2);

        sampleChat = new Chat();
        sampleChat.setId(100);
        sampleChat.setCreatedAt(LocalDateTime.now());
        sampleChat.setParticipants(Set.of(user1, user2));
    }

    // -----------------------------------------------------------------------
    // getOrCreateChat() tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@link ChatService#getOrCreateChat} skips creation logic
     * and returns the active mapping immediately if a conversation thread
     * between the requested users already exists.
     */
    @Test
    void getOrCreateChat_chatAlreadyExists_returnsExistingChat() {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(2);

        when(chatRepository.findDirectChat(1, 2)).thenReturn(Optional.of(sampleChat));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(100)).thenReturn(Collections.emptyList());

        ChatResponse result = chatService.getOrCreateChat(request);

        assertNotNull(result);
        assertEquals(100, result.getId());
        verify(chatRepository, never()).save(any(Chat.class));
    }

    /**
     * Verifies that {@link ChatService#getOrCreateChat} successfully triggers database
     * persistence for a new chat thread when no prior history is discovered.
     */
    @Test
    void getOrCreateChat_chatDoesNotExist_createsAndReturnsNewChat() {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(2);

        when(chatRepository.findDirectChat(1, 2)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(2)).thenReturn(Optional.of(user2));

        ChatResponse result = chatService.getOrCreateChat(request);

        assertNotNull(result);
        verify(chatRepository, times(1)).save(any(Chat.class));
    }

    /**
     * Verifies that {@link ChatService#getOrCreateChat} terminates with an
     * {@link IllegalArgumentException} when attempting to construct a thread
     * containing a non-existent participant identifier.
     */
    @Test
    void getOrCreateChat_userNotFound_throwsIllegalArgumentException() {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(999);

        when(chatRepository.findDirectChat(1, 999)).thenReturn(Optional.empty());
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(999)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> chatService.getOrCreateChat(request));
    }

    // -----------------------------------------------------------------------
    // getChatsForUser() tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@link ChatService#getChatsForUser} aggregates, maps,
     * and forwards data collections successfully for a legitimate user reference.
     */
    @Test
    void getChatsForUser_returnsMappedChatResponses() {
        Integer userId = 1;
        when(chatRepository.findByParticipants_IdOrderByCreatedAtDesc(userId)).thenReturn(List.of(sampleChat));
        when(messageRepository.findByChat_IdOrderBySentAtAsc(100)).thenReturn(Collections.emptyList());

        List<ChatResponse> result = chatService.getChatsForUser(userId);

        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getId());
    }

    // -----------------------------------------------------------------------
    // getMessages() tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@link ChatService#getMessages} delivers the entire structural
     * text ledger mapped sequentially according to chronological guidelines.
     */
    @Test
    void getMessages_chatExists_returnsMessagesList() {
        Integer chatId = 100;
        Message msg = new Message();
        msg.setId(501);
        msg.setContent("Test message");
        msg.setSender(user1);
        msg.setSentAt(LocalDateTime.now());

        when(chatRepository.existsById(chatId)).thenReturn(true);
        when(messageRepository.findByChat_IdOrderBySentAtAsc(chatId)).thenReturn(List.of(msg));

        List<MessageResponse> result = chatService.getMessages(chatId);

        assertEquals(1, result.size());
        assertEquals("Test message", result.get(0).getContent());
        assertEquals(1, result.get(0).getSenderId());
    }

    /**
     * Verifies that {@link ChatService#getMessages} throws an {@link IllegalArgumentException}
     * if the targeted chat configuration identity is absent from the datastore.
     */
    @Test
    void getMessages_chatDoesNotExist_throwsIllegalArgumentException() {
        Integer chatId = 999;
        when(chatRepository.existsById(chatId)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> chatService.getMessages(chatId));
    }

    // -----------------------------------------------------------------------
    // sendMessage() tests
    // -----------------------------------------------------------------------

    /**
     * Verifies that {@link ChatService#sendMessage} commits data entries properly
     * and broadcasts individual push payload operations targeting every distinct device
     * registration owned by the corresponding recipient.
     */
    @Test
    void sendMessage_success_savesMessageAndSendsFcmNotification() {
        Integer chatId = 100;
        SendMessageRequest request = new SendMessageRequest();
        request.setSenderId(1);
        request.setContent("Hello Anna!");

        when(chatRepository.findById(chatId)).thenReturn(Optional.of(sampleChat));
        when(userRepository.findById(1)).thenReturn(Optional.of(user1));
        when(fcmTokenService.getTokensForUser(2)).thenReturn(List.of("token-device-1", "token-device-2"));

        MessageResponse result = chatService.sendMessage(chatId, request);

        assertNotNull(result);
        assertEquals("Hello Anna!", result.getContent());
        assertEquals(1, result.getSenderId());

        verify(messageRepository, times(1)).save(any(Message.class));
        verify(fcmService, times(1)).sendNotification("token-device-1", "John Student", "Hello Anna!");
        verify(fcmService, times(1)).sendNotification("token-device-2", "John Student", "Hello Anna!");
    }

    /**
     * Verifies that {@link ChatService#sendMessage} breaks routine operation and drops execution
     * via an {@link IllegalArgumentException} when referencing a non-existent thread instance.
     */
    @Test
    void sendMessage_chatNotFound_throwsIllegalArgumentException() {
        Integer chatId = 999;
        SendMessageRequest request = new SendMessageRequest();

        when(chatRepository.findById(chatId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> chatService.sendMessage(chatId, request));
    }
}