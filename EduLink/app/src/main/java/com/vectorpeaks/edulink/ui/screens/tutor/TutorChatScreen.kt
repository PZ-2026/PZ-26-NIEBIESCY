package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.clickable
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import com.vectorpeaks.edulink.data.model.user.BookingResponse


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
    onChatOpen: (Boolean) -> Unit = {},
    pendingChatStudentId: Int? = null,
    onPendingChatConsumed: () -> Unit = {}
) {
    val viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(RetrofitClient.apiService)
    )

    val reservationsViewModel: TutorReservationsViewModel = viewModel()
    val chatsState by viewModel.chatsState.collectAsState()
    val chats by viewModel.chats.collectAsState()
    val createChatState by viewModel.createChatState.collectAsState()
    var showNewChatSheet by remember { mutableStateOf(false) }
    var chatSearchQuery by remember { mutableStateOf("") }

    var selectedChat by remember { mutableStateOf<ChatResponse?>(null) }
    val bookings by reservationsViewModel.bookings.collectAsState()

    LaunchedEffect(user.id) {
        viewModel.fetchChats(user.id)

        reservationsViewModel.loadBookings(user.id)
    }

    // When arriving from reservations screen, create or get chat with that student
    LaunchedEffect(pendingChatStudentId) {
        if (pendingChatStudentId != null) {
            viewModel.createOrGetChat(user.id, pendingChatStudentId)
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

    val acceptedBookingsForChat = remember(bookings, chats) {
        bookings.filter { booking ->
            booking.status == "ACCEPTED" &&
                    chats.none { chat ->
                        chat.participants.any { it.id == booking.studentId }
                    }
        }
            .distinctBy { it.studentId }
    }

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

                OutlinedTextField(
                    value = chatSearchQuery,
                    onValueChange = { chatSearchQuery = it },
                    placeholder = { Text("Szukaj rozmowy...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
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
                Spacer(modifier = Modifier.height(8.dp))

                val chatFilters = listOf("Odblokowane", "Zablokowane", "Wszystkie")
                val pagerState = rememberPagerState(pageCount = { chatFilters.size })
                val coroutineScope = rememberCoroutineScope()

                ScrollableTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Background,
                    contentColor = Primary,
                    edgePadding = 0.dp
                ) {
                    chatFilters.forEachIndexed { index, label ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = {
                                Text(
                                    text = label,
                                    fontWeight = if (pagerState.currentPage == index)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            selectedContentColor = Primary,
                            unselectedContentColor = OnSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (chatsState) {
                    is ChatViewModel.ChatListState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { CircularProgressIndicator() }
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
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            val pageChats = when (page) {
                                0 -> filteredChats.filter { !it.isBlocked }
                                1 -> filteredChats.filter { it.isBlocked }
                                else -> filteredChats
                            }

                            if (pageChats.isEmpty()) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when {
                                            chatSearchQuery.isNotBlank() ->
                                                "Brak rozmów pasujących do wyszukiwania."
                                            page == 0 -> "Brak aktywnych rozmów.\nOczekuj na pierwszą wiadomość od ucznia."
                                            page == 1 -> "Brak zablokowanych rozmów."
                                            else -> "Brak rozmów."
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = OnSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(bottom = 88.dp, top = 4.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(pageChats) { chat ->
                                        TutorConversationItem(
                                            chat = chat,
                                            currentUserId = user.id,
                                            onClick = { selectedChat = chat }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else -> {}
                }
            }

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

        if (showNewChatSheet) {
            TutorNewChatSheet(
                currentUserId = user.id,
                acceptedBookings = acceptedBookingsForChat,
                onDismiss = { showNewChatSheet = false },
                onSelectStudent = { studentId ->
                    viewModel.createOrGetChat(user.id, studentId)
                }
            )
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
 * - Unread badge (if applicable)
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
    val studentParticipant = chat.participants.find { it.id != currentUserId }
    val hasUnread = chat.unreadCount > 0

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
                            text = DateUtils.formatChatTimestamp(chat.lastMessage?.sentAt ?: chat.createdAt),
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
    val chats by viewModel.chats.collectAsState()
    var newMessage by remember { mutableStateOf("") }

    val currentChat = chats.find { it.id == chat.id } ?: chat

    var showBlockDialog by remember { mutableStateOf(false) }

    val canToggleBlock = !currentChat.isBlocked || currentChat.blockedBy == currentUserId

    if (showBlockDialog && canToggleBlock) {
        AlertDialog(
            onDismissRequest = { showBlockDialog = false },
            title = {
                Text(if (currentChat.isBlocked) "Odblokuj czat" else "Zablokuj czat")
            },
            text = {
                Text(
                    if (currentChat.isBlocked)
                        "Czy na pewno chcesz odblokować tę rozmowę?"
                    else
                        "Czy na pewno chcesz zablokować tę rozmowę? Żadna ze stron nie będzie mogła wysyłać wiadomości."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleBlock(currentChat.id, !currentChat.isBlocked)
                        showBlockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentChat.isBlocked) Primary else Error
                    )
                ) {
                    Text(if (currentChat.isBlocked) "Odblokuj" else "Zablokuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBlockDialog = false }) { Text("Anuluj") }
            }
        )
    }

    LaunchedEffect(chat.id) {
        viewModel.fetchMessages(chat.id)
        viewModel.markChatAsRead(chat.id, currentUserId)
    }

    BackHandler { onBack() }

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
    LaunchedEffect(sendMessageState) {
        if (sendMessageState is ChatViewModel.SendMessageState.Success) {
            newMessage = ""
            viewModel.resetSendMessageState()
        }
    }

    val studentParticipant = currentChat.participants.find { it.id != currentUserId }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(name = studentParticipant?.fullName ?: "Unknown", size = 32)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(studentParticipant?.fullName ?: "Unknown Student")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                actions = {
                    if (canToggleBlock) {
                        IconButton(onClick = { showBlockDialog = true }) {
                            Icon(
                                imageVector = if (currentChat.isBlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = if (currentChat.isBlocked) "Odblokuj czat" else "Zablokuj czat",
                                tint = if (currentChat.isBlocked) Error else OnSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            Surface(color = Surface, shadowElevation = 4.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    if (currentChat.isBlocked) {
                        Surface(color = ErrorContainer) {
                            Text(
                                text = "Ten czat jest zablokowany.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Error,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                    viewModel.sendMessage(currentChat.id, currentUserId, newMessage)
                                }
                            },
                            colors = IconButtonDefaults.filledIconButtonColors(containerColor = Primary),
                            enabled = newMessage.isNotBlank()
                                    && sendMessageState !is ChatViewModel.SendMessageState.Loading
                                    && !currentChat.isBlocked
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Wyślij")
                        }
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
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages) { message ->
                                TutorMessageBubble(
                                    message = message,
                                    isCurrentUser = message.senderId == currentUserId
                                )
                            }
                        }
                    }
                }
                else -> {}
            }

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorNewChatSheet(
    currentUserId: Int,
    acceptedBookings: List<BookingResponse>,
    onDismiss: () -> Unit,
    onSelectStudent: (studentId: Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(acceptedBookings, searchQuery) {
        if (searchQuery.isBlank()) acceptedBookings
        else acceptedBookings.filter { booking ->
            val name = booking.studentName ?: ""
            name.contains(searchQuery, ignoreCase = true) ||
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

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Szukaj studenta lub przedmiotu...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
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
                    items(filtered, key = { it.studentId }) { booking ->
                        TutorNewChatBookingItem(
                            booking = booking,
                            onClick = { onSelectStudent(booking.studentId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TutorNewChatBookingItem(
    booking: BookingResponse,
    onClick: () -> Unit
) {
    val studentName = booking.studentName ?: "Nieznany student"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = studentName, size = 44)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = studentName,
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