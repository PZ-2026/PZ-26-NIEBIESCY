package com.vectorpeaks.edulink.data.model

data class ChatConversation(
    val id: Int,
    val otherUserId: Int,
    val otherUserName: String,
    val lastMessage: String,
    val lastMessageTime: String,
    val unreadCount: Int = 0
)

data class Message(
    val id: Int,
    val conversationId: Int,
    val senderId: Int,
    val text: String,
    val timestamp: String,
    val isRead: Boolean = true
)
