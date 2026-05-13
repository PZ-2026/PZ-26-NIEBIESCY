package com.vectorpeaks.edulink.data.model.chat

/**
 * Condensed participant data returned by the backend for a chat thread.
 *
 * @property id       unique identifier of the user
 * @property fullName full name displayed in the UI
 * @property roleId   role identifier (1=Admin, 2=Tutor, 3=Student)
 */
data class ParticipantInfo(
    val id: Int,
    val fullName: String,
    val roleId: Int
)

/**
 * A single message returned by the backend.
 *
 * @property id         unique identifier of the message
 * @property senderId   ID of the user who sent the message
 * @property senderName full name of the sender displayed in the UI
 * @property content    text content of the message
 * @property sentAt     ISO-8601 timestamp of when the message was sent
 */
data class MessageResponse(
    val id: Int,
    val senderId: Int,
    val senderName: String,
    val content: String,
    val sentAt: String
)

/**
 * Chat thread data returned by the backend, used in the conversation list view.
 *
 * @property id           unique identifier of the chat thread
 * @property createdAt    ISO-8601 timestamp of when the chat was created
 * @property participants list of users participating in this chat
 * @property lastMessage  most recent message preview, or null if no messages yet
 */
data class ChatResponse(
    val id: Int,
    val createdAt: String,
    val participants: List<ParticipantInfo>,
    val lastMessage: MessageResponse?
)

/**
 * Request body for creating a new chat between two users.
 *
 * @property userId1 ID of the first participant (e.g. the logged-in student)
 * @property userId2 ID of the second participant (e.g. the tutor from the offer)
 */
data class CreateChatRequest(
    val userId1: Int,
    val userId2: Int
)

/**
 * Request body for sending a message within an existing chat thread.
 *
 * @property senderId ID of the logged-in user sending the message
 * @property content  text content of the message
 */
data class SendMessageRequest(
    val senderId: Int,
    val content: String
)
