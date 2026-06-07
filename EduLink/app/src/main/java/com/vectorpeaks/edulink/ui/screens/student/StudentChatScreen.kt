package com.vectorpeaks.edulink.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.chat.ChatResponse
import com.vectorpeaks.edulink.data.model.chat.MessageResponse
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.components.EduSearchBar
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModel
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModelFactory
import com.vectorpeaks.edulink.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentChatScreen(
    user: User,
    modifier: Modifier = Modifier,
    onChatOpen: (Boolean) -> Unit = {},
    pendingChatTutorId: Int? = null,
    onPendingChatConsumed: () -> Unit = {}
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(RetrofitClient.apiService)
    )
    // HistoryViewModel to load accepted bookings for new chat selection
    val historyViewModel: HistoryViewModel = viewModel()

    val chatsState by viewModel.chatsState.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val createChatState by viewModel.createChatState.collectAsState()
    val bookings by historyViewModel.bookings.collectAsState()

    var selectedChat by remember { mutableStateOf<ChatResponse?>(null) }
    var showNewChatSheet by remember { mutableStateOf(false) }

    // Search query for filtering the chat list
    var chatSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(user.id) {
        viewModel.fetchChats(user.id)
        historyViewModel.loadBookings(user.id)
    }

    // When pendingChatTutorId arrives from history screen,
    // call the API to create or get existing chat with that tutor
    LaunchedEffect(pendingChatTutorId) {
        if (pendingChatTutorId != null) {
            viewModel.createOrGetChat(user.id, pendingChatTutorId)
        }
    }

    // Once chat is ready, open it and clear the pending request
    LaunchedEffect(createChatState) {
        if (createChatState is ChatViewModel.CreateChatState.Success) {
            selectedChat = (createChatState as ChatViewModel.CreateChatState.Success).chat
            viewModel.resetCreateChatState()
            onPendingChatConsumed()
            showNewChatSheet = false
        }
    }

    // Accepted bookings that don't yet have a chat — shown in new chat sheet
    val acceptedBookingsForChat = remember(bookings, chats) {
        bookings.filter { booking ->
            booking.status == "ACCEPTED" &&
                    // Only show if there's no existing chat with this tutor
                    chats.none { chat ->
                        chat.participants.any { it.id == booking.tutorId }
                    }
        }
            // Deduplicate by tutorId — one entry per tutor
            .distinctBy { it.tutorId }
    }

    // Filter chats by search query (tutor name or last message content)
    val filteredChats = remember(chats, chatSearchQuery) {
        if (chatSearchQuery.isBlank()) chats
        else chats.filter { chat ->
            val otherName = chat.participants
                .find { it.id != user.id }?.fullName ?: ""
            val lastMsg = chat.lastMessage?.content ?: ""
            otherName.contains(chatSearchQuery, ignoreCase = true) ||
                    lastMsg.contains(chatSearchQuery, ignoreCase = true)
        }
    }

    if (selectedChat != null) {
        LaunchedEffect(selectedChat) { onChatOpen(true) }
        StudentChatDetailView(
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
        Box(modifier = modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Rozmowy",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Search bar with clear button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = chatSearchQuery,
                        onValueChange = { chatSearchQuery = it },
                        placeholder = { Text("Szukaj rozmowy...") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            // X button — only visible when something is typed
                            if (chatSearchQuery.isNotBlank()) {
                                IconButton(onClick = { chatSearchQuery = "" }) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Wyczyść",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                when (chatsState) {
                    is ChatViewModel.ChatListState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is ChatViewModel.ChatListState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Błąd: ${(chatsState as ChatViewModel.ChatListState.Error).message}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is ChatViewModel.ChatListState.Success -> {
                        if (filteredChats.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (chatSearchQuery.isNotBlank())
                                        "Brak rozmów pasujących do wyszukiwania."
                                    else
                                        "Brak rozmów.\nZarezerwuj lekcję, aby rozpocząć czat.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = OnSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredChats) { chat ->
                                    StudentConversationItem(
                                        chat = chat,
                                        currentUserId = user.id,
                                        onClick = { selectedChat = chat }
                                    )
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

            // FAB — opens new chat sheet
            FloatingActionButton(
                onClick = { showNewChatSheet = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                containerColor = Primary,
                contentColor = Surface
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nowa rozmowa")
            }
        }

        // New chat bottom sheet — shows accepted bookings without a chat
        if (showNewChatSheet) {
            NewChatSheet(
                currentUserId = user.id,
                acceptedBookings = acceptedBookingsForChat,
                onDismiss = { showNewChatSheet = false },
                onSelectTutor = { tutorId ->
                    viewModel.createOrGetChat(user.id, tutorId)
                }
            )
        }
    }
}

/**
 * Bottom sheet for starting a new chat.
 * Shows a searchable list of accepted bookings (one per tutor) that don't yet have a chat.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewChatSheet(
    currentUserId: Int,
    acceptedBookings: List<BookingResponse>,
    onDismiss: () -> Unit,
    onSelectTutor: (tutorId: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(acceptedBookings, searchQuery) {
        if (searchQuery.isBlank()) acceptedBookings
        else acceptedBookings.filter { booking ->
            booking.tutorName.contains(searchQuery, ignoreCase = true) ||
                    booking.subject.contains(searchQuery, ignoreCase = true)
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Nowa rozmowa",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Search field with X button
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj korepetytora lub przedmiotu...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Wyczyść",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank())
                            "Brak wyników dla \"$searchQuery\"."
                        else
                            "Brak zaakceptowanych rezerwacji bez aktywnego czatu.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered, key = { it.tutorId }) { booking ->
                        NewChatBookingItem(
                            booking = booking,
                            onClick = { onSelectTutor(booking.tutorId) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single item in the new chat sheet — shows tutor name and subject.
 */
@Composable
private fun NewChatBookingItem(
    booking: BookingResponse,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = booking.tutorName, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = booking.tutorName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OnBackground
                )
                Text(
                    text = booking.subject,
                    style = MaterialTheme.typography.bodySmall,
                    color = Primary
                )
            }
        }
    }
}

/**
 * Displays a single conversation item in the chat list.
 */
@Composable
private fun StudentConversationItem(
    chat: ChatResponse,
    currentUserId: Int,
    onClick: () -> Unit
) {
    val otherParticipant = chat.participants.find { it.id != currentUserId }
    val hasUnread = chat.unreadCount > 0

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(name = otherParticipant?.fullName ?: "Unknown", size = 48)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = otherParticipant?.fullName ?: "Unknown User",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (hasUnread) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (hasUnread) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = DateUtils.formatChatTimestamp(
                                chat.lastMessage?.sentAt ?: chat.createdAt
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasUnread) MaterialTheme.colorScheme.primary else OnSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chat.lastMessage?.content ?: "Brak wiadomości",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (hasUnread) MaterialTheme.colorScheme.onSurface else OnSurfaceVariant,
                    fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Detailed chat view showing the full message conversation.
 * pendingChatTutorId and createChatState logic is handled in the parent [StudentChatScreen],
 * so this composable only receives the already-resolved chat object.
 *
 * @param chat the resolved ChatResponse to display
 * @param currentUserId the logged-in student's id
 * @param viewModel shared ChatViewModel
 * @param onBack callback to close the detail view
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentChatDetailView(
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

    // Load message history and mark as read when entering the detail view
    LaunchedEffect(chat.id) {
        viewModel.fetchMessages(chat.id)
        viewModel.markChatAsRead(chat.id, currentUserId)
    }

    BackHandler { onBack() }

    // Scroll to bottom when messages load or a new one arrives
    LaunchedEffect(messagesState) {
        if (messagesState is ChatViewModel.MessageListState.Success && messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    // Clear input field after successful send
    LaunchedEffect(sendMessageState) {
        if (sendMessageState is ChatViewModel.SendMessageState.Success) {
            newMessage = ""
            viewModel.resetSendMessageState()
        }
    }

    val otherParticipant = chat.participants.find { it.id != currentUserId }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = otherParticipant?.fullName ?: "Unknown", size = 32)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(otherParticipant?.fullName ?: "Unknown User")
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
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (messagesState) {
                is ChatViewModel.MessageListState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                    if (messages.isEmpty()) {
                        // Empty state — shown before any message is sent
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nie masz z tym użytkownikiem\njeszcze żadnej korespondencji.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                StudentMessageBubble(
                                    message = message,
                                    isCurrentUser = message.senderId == currentUserId
                                )
                            }
                        }
                    }
                }
                else -> {}

            }  // koniec when(messagesState)

            if (sendMessageState is ChatViewModel.SendMessageState.Error) {
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
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
 * A single message bubble in the conversation.
 */
@Composable
private fun StudentMessageBubble(
    message: MessageResponse,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
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