package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.chat.ChatResponse
import com.vectorpeaks.edulink.data.model.chat.MessageResponse
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModel
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModelFactory
import com.vectorpeaks.edulink.utils.DateUtils


/**
 * Tutor chat screen showing the list of conversations with students.
 *
 * Handles:
 * - Displaying all active chats for the logged-in tutor
 * - Loading state and error handling
 * - Navigation to chat detail view
 *
 * @param user the currently logged-in tutor user
 * @param modifier layout modifier for the composable
 * @param onChatOpen callback when a chat conversation is opened/closed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatScreen(
    user: User,
    modifier: Modifier = Modifier,
    onChatOpen: (Boolean) -> Unit = {}
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(RetrofitClient.apiService)
    )

    val chatsState by viewModel.chatsState.collectAsState()
    val chats by viewModel.chats.collectAsState()
    var selectedChat by remember { mutableStateOf<ChatResponse?>(null) }

    // Load chats when the screen is first composed
    LaunchedEffect(user.id) {
        viewModel.fetchChats(user.id)
    }

    if (selectedChat != null) {
        LaunchedEffect(selectedChat) {
            onChatOpen(true)
        }
        TutorChatDetailView(
            chat = selectedChat!!,
            currentUserId = user.id,
            viewModel = viewModel,
            onBack = {
                selectedChat = null
                onChatOpen(false)
                viewModel.fetchChats(user.id)
            }
        )
    } else {
        Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Rozmowy",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(16.dp))

            when (chatsState) {
                is ChatViewModel.ChatListState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is ChatViewModel.ChatListState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Błąd: ${(chatsState as ChatViewModel.ChatListState.Error).message}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                is ChatViewModel.ChatListState.Success -> {
                    if (chats.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Brak rozmów.\nOczekuj na pierwszą wiadomość od ucznia.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(chats) { chat ->
                                TutorConversationItem(
                                    chat = chat,
                                    currentUserId = user.id,
                                    onClick = { selectedChat = chat }
                                )
                            }
                        }
                    }
                }
                else -> {} // Idle state
            }
        }
    }
}

/**
 * Displays a single conversation item in the tutor's chat list.
 *
 * Shows:
 * - Avatar of the student
 * - Name of the student
 * - Preview of the last message
 * - Timestamp of the last message
 *
 * @param chat the ChatResponse containing conversation details
 * @param currentUserId the ID of the logged-in tutor
 * @param onClick callback when the conversation is tapped
 */
@Composable
private fun TutorConversationItem(
    chat: ChatResponse,
    currentUserId: Int,
    onClick: () -> Unit
) {
    // Find the student participant (not the tutor)
    val studentParticipant = chat.participants.find { it.id != currentUserId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = studentParticipant?.fullName ?: "Unknown",
                size = 48
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = studentParticipant?.fullName ?: "Unknown Student",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = DateUtils.formatChatTimestamp(chat.lastMessage?.sentAt ?: chat.createdAt),
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chat.lastMessage?.content ?: "Brak wiadomości",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Detailed tutor chat view showing the message conversation history.
 *
 * Handles:
 * - Loading message history
 * - Displaying all messages with timestamps
 * - Input field for composing new messages
 * - Sending messages and adding them to the local list
 * - Error states and retry logic
 *
 * @param chat the ChatResponse for this conversation
 * @param currentUserId the ID of the logged-in tutor
 * @param viewModel the ChatViewModel for managing state
 * @param onBack callback to close the detail view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorChatDetailView(
    chat: ChatResponse,
    currentUserId: Int,
    viewModel: ChatViewModel,
    onBack: () -> Unit
) {
    val listState = rememberLazyListState()
    val messagesState by viewModel.messagesState.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val sendMessageState by viewModel.sendMessageState.collectAsState()
    var newMessage by remember { mutableStateOf("") }

    // Load message history when entering the detail view
    LaunchedEffect(chat.id) {
        viewModel.fetchMessages(chat.id)
    }

    BackHandler {
        onBack()
    }

    LaunchedEffect(messagesState) {
        if (messagesState is ChatViewModel.MessageListState.Success
            && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Clear text after successful send
    LaunchedEffect(sendMessageState) {
        if (sendMessageState is ChatViewModel.SendMessageState.Success) {
            newMessage = ""
            viewModel.resetSendMessageState()
        }
    }

    val studentParticipant = chat.participants.find { it.id != currentUserId }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            name = studentParticipant?.fullName ?: "Unknown",
                            size = 32
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(studentParticipant?.fullName ?: "Unknown Student")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            Surface(color = Surface, shadowElevation = 4.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        placeholder = { Text("Napisz wiadomość...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.weight(1f),
                        enabled = sendMessageState !is ChatViewModel.SendMessageState.Loading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = {
                            if (newMessage.isNotBlank()) {
                                viewModel.sendMessage(chat.id, currentUserId, newMessage)
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary),
                        enabled = newMessage.isNotBlank()
                                && sendMessageState !is ChatViewModel.SendMessageState.Loading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Wyślij")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (messagesState) {
                is ChatViewModel.MessageListState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ChatViewModel.MessageListState.Error -> {
                    Text(
                        text = "Błąd: ${(messagesState as ChatViewModel.MessageListState.Error).message}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ChatViewModel.MessageListState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        reverseLayout = false  // Messages flow from top to bottom
                    ) {
                        items(messages) { message ->
                            TutorMessageBubble(
                                message = message,
                                isCurrentUser = message.senderId == currentUserId
                            )
                        }
                    }
                }
                else -> {} // Idle state
            }

            // Show error for send message state
            if (sendMessageState is ChatViewModel.SendMessageState.Error) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.error
                ) {
                    Text(
                        text = (sendMessageState as ChatViewModel.SendMessageState.Error).message,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
    }
}

/**
 * A single message bubble displayed in the tutor's conversation view.
 *
 * Styling:
 * - Tutor's own messages align right with primary color
 * - Student's messages align left with variant surface color
 * - Includes sender name, message content, and timestamp
 *
 * @param message the MessageResponse to display
 * @param isCurrentUser true if the message was sent by the tutor
 */
@Composable
private fun TutorMessageBubble(
    message: MessageResponse,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isCurrentUser) 4.dp else 16.dp
            ),
            color = if (isCurrentUser) PrimaryContainer else SurfaceVariant,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                if (!isCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCurrentUser) OnPrimaryContainer else OnBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = DateUtils.formatMessageTimestamp(message.sentAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
