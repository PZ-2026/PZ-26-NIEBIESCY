/*
 * ChatControllerTest.java
 *
 * Version: 1.3
 * Date: 2026-05-24
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.ChatDtos.*;
import com.vectorpeaks.backend.service.ChatService;
import com.vectorpeaks.backend.service.MaintenanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ChatController}.
 *
 * <p>Verifies the behaviour of the chat endpoints using {@code @WebMvcTest}.
 * Includes tests for BOLA security verifications by injecting a mock Principal
 * via Spring Security test support.
 *
 * @version 1.3
 * @author EduLink Team
 * @see ChatController
 */
@WebMvcTest(
        controllers = ChatController.class,
        excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
class ChatControllerTest extends BaseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ChatService chatService;

    /**
     * Helper method to generate a Mock Authentication token for security context injection.
     */
    private UsernamePasswordAuthenticationToken getMockAuth(Integer userId, String roleName) {
        return new UsernamePasswordAuthenticationToken(
                userId, null, List.of(new SimpleGrantedAuthority(roleName))
        );
    }

    // -----------------------------------------------------------------------
    // POST /api/chats
    // -----------------------------------------------------------------------

    @Test
    void createOrGetChat_success_returns200AndChat() throws Exception {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(2);

        ParticipantInfo p1 = new ParticipantInfo();
        p1.setId(1);
        p1.setFullName("John Student");
        p1.setRoleId(3);

        ChatResponse response = new ChatResponse();
        response.setId(100);
        response.setParticipants(List.of(p1));

        when(chatService.getOrCreateChat(any(CreateChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chats")
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))) // Poprawione bindowanie Authentication
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100));
    }

    @Test
    void createOrGetChat_unauthorizedThirdParty_returns403Forbidden() throws Exception {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(2);

        // User 999 attempts to create a chat between User 1 and User 2
        mockMvc.perform(post("/api/chats")
                        .with(authentication(getMockAuth(999, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Nie możesz utworzyć czatu dla osób trzecich."));
    }

    // -----------------------------------------------------------------------
    // GET /api/chats/user/{userId}
    // -----------------------------------------------------------------------

    @Test
    void getUserChats_success_returns200AndList() throws Exception {
        Integer userId = 1;
        ChatResponse chat1 = new ChatResponse(); chat1.setId(101);
        ChatResponse chat2 = new ChatResponse(); chat2.setId(102);

        when(chatService.getChatsForUser(userId)).thenReturn(List.of(chat1, chat2));

        mockMvc.perform(get("/api/chats/user/{userId}", userId)
                        .with(authentication(getMockAuth(userId, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // -----------------------------------------------------------------------
    // GET /api/chats/{chatId}/messages
    // -----------------------------------------------------------------------

    @Test
    void getMessages_chatExistsAndAuthorized_returns200AndList() throws Exception {
        Integer chatId = 100;
        MessageResponse m1 = new MessageResponse(); m1.setId(1001); m1.setContent("Hello");
        MessageResponse m2 = new MessageResponse(); m2.setId(1002); m2.setContent("Hi there!");

        when(chatService.getMessages(chatId, 1)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/chats/{chatId}/messages", chatId)
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getMessages_userNotParticipant_returns403Forbidden() throws Exception {
        Integer chatId = 100;
        when(chatService.getMessages(chatId, 999))
                .thenThrow(new SecurityException("Brak dostępu: nie jesteś uczestnikiem tej konwersacji."));

        mockMvc.perform(get("/api/chats/{chatId}/messages", chatId)
                        .with(authentication(getMockAuth(999, "ROLE_STUDENT"))))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Brak dostępu: nie jesteś uczestnikiem tej konwersacji."));
    }

    // -----------------------------------------------------------------------
    // POST /api/chats/{chatId}/messages
    // -----------------------------------------------------------------------

    @Test
    void sendMessage_success_returns201CreatedAndMessage() throws Exception {
        Integer chatId = 100;
        SendMessageRequest request = new SendMessageRequest();
        request.setSenderId(1);
        request.setContent("New message text");

        MessageResponse response = new MessageResponse();
        response.setId(5001);

        when(chatService.sendMessage(eq(chatId), any(SendMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5001));
    }

    @Test
    void sendMessage_senderIdMismatch_returns403Forbidden() throws Exception {
        Integer chatId = 100;
        SendMessageRequest request = new SendMessageRequest();
        request.setSenderId(2); // Request claims to be from User 2
        request.setContent("Spoofed message");

        // But the logged-in user is User 1 (BOLA attack attempt)
        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .with(authentication(getMockAuth(1, "ROLE_STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Nie możesz wysłać wiadomości jako inny użytkownik."));
    }
}