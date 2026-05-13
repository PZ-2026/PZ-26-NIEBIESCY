/*
 * ChatViewModel.kt
 *
 * Version: 1.2
 * Date: 2026-05-10
 *
 */

package com.vectorpeaks.edulink.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.chat.ChatResponse
import com.vectorpeaks.edulink.data.model.chat.CreateChatRequest
import com.vectorpeaks.edulink.data.model.chat.MessageResponse
import com.vectorpeaks.edulink.data.model.chat.SendMessageRequest
import com.vectorpeaks.edulink.network.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * ViewModel responsible for managing chat-related state and business logic.
 *
 * Handles:
 * - Fetching all chats for the logged-in user
 * - Creating or retrieving existing chats with other users
 * - Loading message history for a specific chat
 * - Sending new messages
 * - Error handling and loading states
 *
 * @param apiService the Retrofit service for API calls
 */
class ChatViewModel(private val apiService: ApiService) : ViewModel() {

    // -----------------------------------------------------------------------
    // State: Chat list
    // -----------------------------------------------------------------------

    private val _chatsState = MutableStateFlow<ChatListState>(ChatListState.Idle)
    val chatsState: StateFlow<ChatListState> = _chatsState.asStateFlow()

    private val _chats = MutableStateFlow<List<ChatResponse>>(emptyList())
    val chats: StateFlow<List<ChatResponse>> = _chats.asStateFlow()

    // -----------------------------------------------------------------------
    // State: Chat detail (messages)
    // -----------------------------------------------------------------------

    private val _messagesState = MutableStateFlow<MessageListState>(MessageListState.Idle)
    val messagesState: StateFlow<MessageListState> = _messagesState.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageResponse>>(emptyList())
    val messages: StateFlow<List<MessageResponse>> = _messages.asStateFlow()

    // -----------------------------------------------------------------------
    // State: Send message
    // -----------------------------------------------------------------------

    private val _sendMessageState = MutableStateFlow<SendMessageState>(SendMessageState.Idle)
    val sendMessageState: StateFlow<SendMessageState> = _sendMessageState.asStateFlow()

    // -----------------------------------------------------------------------
    // State: Create/get chat
    // -----------------------------------------------------------------------

    private val _createChatState = MutableStateFlow<CreateChatState>(CreateChatState.Idle)
    val createChatState: StateFlow<CreateChatState> = _createChatState.asStateFlow()

    // -----------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------

    /**
     * Fetches all chat threads for the given user from the backend.
     * Updates [chatsState] to reflect loading/success/error states.
     *
     * @param userId the ID of the logged-in user
     */
    fun fetchChats(userId: Int) {
        viewModelScope.launch {
            _chatsState.value = ChatListState.Loading
            try {
                val chatList = apiService.getChatsForUser(userId)
                _chats.value = chatList.sortedByDescending { chat ->
                    chat.lastMessage?.sentAt ?: chat.createdAt
                }
                _chatsState.value = ChatListState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch chats for user $userId")
                _chatsState.value = ChatListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Creates a new chat between two users or retrieves the existing one.
     * Typically called when a student initiates a chat with a tutor.
     *
     * @param userId1 ID of the first participant (e.g., the student)
     * @param userId2 ID of the second participant (e.g., the tutor)
     */
    fun createOrGetChat(userId1: Int, userId2: Int) {
        viewModelScope.launch {
            _createChatState.value = CreateChatState.Loading
            try {
                val request = CreateChatRequest(userId1, userId2)
                val chatResponse = apiService.createOrGetChat(request)
                _createChatState.value = CreateChatState.Success(chatResponse)
            } catch (e: Exception) {
                Timber.e(e, "Failed to create or get chat between $userId1 and $userId2")
                _createChatState.value = CreateChatState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Fetches the full message history for a specific chat thread.
     * Updates [messagesState] to reflect loading/success/error states.
     *
     * @param chatId the ID of the chat thread
     */
    fun fetchMessages(chatId: Int) {
        viewModelScope.launch {
            _messagesState.value = MessageListState.Loading
            try {
                val messageList = apiService.getMessages(chatId)
                _messages.value = messageList
                _messagesState.value = MessageListState.Success
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch messages for chat $chatId")
                _messagesState.value = MessageListState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Sends a new message in the given chat thread.
     * After successful send, the message is added to the local [_messages] list.
     * Any push notification to the recipient is handled server-side.
     *
     * @param chatId the ID of the target chat thread
     * @param senderId the ID of the sender (logged-in user)
     * @param content the text content of the message
     */
    fun sendMessage(chatId: Int, senderId: Int, content: String) {
        viewModelScope.launch {
            _sendMessageState.value = SendMessageState.Loading
            try {
                val request = SendMessageRequest(senderId, content)
                val response = apiService.sendMessage(chatId, request)

                if (response.isSuccessful) {
                    val messageResponse: MessageResponse? = response.body()

                    if (messageResponse != null) {
                        // FIX: Use proper list concatenation to preserve type
                        val updatedMessages: List<MessageResponse> = _messages.value + messageResponse
                        _messages.value = updatedMessages
                        _sendMessageState.value = SendMessageState.Success
                    } else {
                        _sendMessageState.value = SendMessageState.Error("Empty response body")
                    }
                } else {
                    _sendMessageState.value = SendMessageState.Error(
                        "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send message in chat $chatId")
                _sendMessageState.value = SendMessageState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Resets the send message state to [SendMessageState.Idle].
     * Call this after successfully sending a message to clear the UI feedback.
     */
    fun resetSendMessageState() {
        _sendMessageState.value = SendMessageState.Idle
    }

    /**
     * Resets the create chat state to [CreateChatState.Idle].
     * Call this after successfully creating/retrieving a chat.
     */
    fun resetCreateChatState() {
        _createChatState.value = CreateChatState.Idle
    }

    // -----------------------------------------------------------------------
    // State sealed classes
    // -----------------------------------------------------------------------

    /**
     * Represents the state of fetching all chats for a user.
     */
    sealed class ChatListState {
        object Idle : ChatListState()
        object Loading : ChatListState()
        object Success : ChatListState()
        data class Error(val message: String) : ChatListState()
    }

    /**
     * Represents the state of fetching messages for a specific chat.
     */
    sealed class MessageListState {
        object Idle : MessageListState()
        object Loading : MessageListState()
        object Success : MessageListState()
        data class Error(val message: String) : MessageListState()
    }

    /**
     * Represents the state of sending a new message.
     */
    sealed class SendMessageState {
        object Idle : SendMessageState()
        object Loading : SendMessageState()
        object Success : SendMessageState()
        data class Error(val message: String) : SendMessageState()
    }

    /**
     * Represents the state of creating or retrieving a chat thread.
     */
    sealed class CreateChatState {
        object Idle : CreateChatState()
        object Loading : CreateChatState()
        data class Success(val chat: ChatResponse) : CreateChatState()
        data class Error(val message: String) : CreateChatState()
    }
}