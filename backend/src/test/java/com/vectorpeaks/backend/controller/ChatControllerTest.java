/*
 * ChatControllerTest.java
 *
 * Version: 1.1
 * Date: 2026-05-18
 *
 * Copyright (c) 2026 EduLink Team. All rights reserved.
 *
 * This software is the confidential and proprietary information of EduLink.
 */

package com.vectorpeaks.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vectorpeaks.backend.dto.ChatDtos.*;
import com.vectorpeaks.backend.service.ChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for {@link ChatController}.
 *
 * <p>Verifies the behaviour of the chat endpoints using {@code @WebMvcTest}.
 * Extends {@link BaseControllerTest} to handle security filters bypass.
 *
 * @version 1.1
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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.participants[0].id").value(1))
                .andExpect(jsonPath("$.participants[0].fullName").value("John Student"));
    }

    @Test
    void createOrGetChat_userNotFound_returns400BadRequest() throws Exception {
        CreateChatRequest request = new CreateChatRequest();
        request.setUserId1(1);
        request.setUserId2(999);

        when(chatService.getOrCreateChat(any(CreateChatRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found"));

        mockMvc.perform(post("/api/chats")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User not found"));
    }

    // -----------------------------------------------------------------------
    // GET /api/chats/user/{userId}
    // -----------------------------------------------------------------------

    @Test
    void getUserChats_success_returns200AndList() throws Exception {
        Integer userId = 1;

        ChatResponse chat1 = new ChatResponse();
        chat1.setId(101);

        ChatResponse chat2 = new ChatResponse();
        chat2.setId(102);

        when(chatService.getChatsForUser(userId)).thenReturn(List.of(chat1, chat2));

        mockMvc.perform(get("/api/chats/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(101))
                .andExpect(jsonPath("$[1].id").value(102));
    }

    // -----------------------------------------------------------------------
    // GET /api/chats/{chatId}/messages
    // -----------------------------------------------------------------------

    @Test
    void getMessages_chatExists_returns200AndList() throws Exception {
        Integer chatId = 100;
        MessageResponse m1 = new MessageResponse();
        m1.setId(1001);
        m1.setSenderId(1);
        m1.setContent("Hello");

        MessageResponse m2 = new MessageResponse();
        m2.setId(1002);
        m2.setSenderId(2);
        m2.setContent("Hi there!");

        when(chatService.getMessages(chatId)).thenReturn(List.of(m1, m2));

        mockMvc.perform(get("/api/chats/{chatId}/messages", chatId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1001))
                .andExpect(jsonPath("$[0].content").value("Hello"))
                .andExpect(jsonPath("$[1].id").value(1002))
                .andExpect(jsonPath("$[1].content").value("Hi there!"));
    }

    @Test
    void getMessages_chatNotFound_returns404NotFound() throws Exception {
        Integer chatId = 999;
        when(chatService.getMessages(chatId))
                .thenThrow(new IllegalArgumentException("Chat thread not found"));

        mockMvc.perform(get("/api/chats/{chatId}/messages", chatId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Chat thread not found"));
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
        response.setSenderId(1);
        response.setContent("New message text");

        when(chatService.sendMessage(eq(chatId), any(SendMessageRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5001))
                .andExpect(jsonPath("$.content").value("New message text"))
                .andExpect(jsonPath("$.senderId").value(1));
    }

    @Test
    void sendMessage_invalidChatOrSender_returns400BadRequest() throws Exception {
        Integer chatId = 100;
        SendMessageRequest request = new SendMessageRequest();
        request.setSenderId(999);
        request.setContent("Hello");

        when(chatService.sendMessage(eq(chatId), any(SendMessageRequest.class)))
                .thenThrow(new IllegalArgumentException("Chat or sender not found"));

        mockMvc.perform(post("/api/chats/{chatId}/messages", chatId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Chat or sender not found"));
    }
}