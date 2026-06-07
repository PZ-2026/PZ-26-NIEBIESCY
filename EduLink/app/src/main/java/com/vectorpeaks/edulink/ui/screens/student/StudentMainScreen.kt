package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.OffersViewModel
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.theme.*
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModel
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModelFactory

data class StudentTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigateToOfferDetail: (Int) -> Unit,
    onNavigateToReviews: (tutorId: Int, tutorName: String) -> Unit
) {
    val offersViewModel: OffersViewModel = viewModel()

    val chatViewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(RetrofitClient.apiService)
    )
    val chats by chatViewModel.chats.collectAsState()

    LaunchedEffect(user.id) {
        chatViewModel.fetchChats(user.id)
    }

    val totalUnreadCount = chats.sumOf { chat -> chat.unreadCount ?: 0 }
    val hasUnreadMessages = totalUnreadCount > 0

    val tabs = listOf(
        StudentTab("Szukaj",   Icons.Filled.Search,      Icons.Outlined.Search),
        StudentTab("Historia", Icons.Filled.History,     Icons.Outlined.History),
        StudentTab("Rozmowy",  Icons.Filled.ChatBubble,  Icons.Outlined.ChatBubbleOutline),
        StudentTab("Profil",   Icons.Filled.Person,      Icons.Outlined.Person)
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatDetailOpen by remember { mutableStateOf(false) }

    // When student taps "Napisz" on a booking, we store the tutorId here,
    // switch to the chat tab, and StudentChatScreen picks it up to open/create the chat.
    var pendingChatTutorId by remember { mutableStateOf<Int?>(null) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (!isChatDetailOpen) {
                NavigationBar(containerColor = Surface) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            icon = {
                                if (tab.title == "Rozmowy" && hasUnreadMessages) {
                                    BadgedBox(
                                        badge = {
                                            Badge {
                                                Text(if (totalUnreadCount > 99) "99+" else totalUnreadCount.toString())
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = tab.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor    = PrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> StudentSearchScreen(
                studentId           = user.id,
                modifier            = Modifier.padding(innerPadding),
                onNavigateToOfferDetail = onNavigateToOfferDetail,
                onNavigateToReviews = onNavigateToReviews,
                offersViewModel     = offersViewModel
            )
            1 -> StudentHistoryScreen(
                studentId = user.id,
                modifier  = Modifier.padding(innerPadding),
                // Switch to chat tab and pass tutorId to open/create the chat
                onOpenChat = { tutorId ->
                    pendingChatTutorId = tutorId
                    selectedTab = 2
                }
            )
            2 -> StudentChatScreen(
                user               = user,
                modifier           = Modifier.padding(innerPadding),
                onChatOpen         = { isOpen -> isChatDetailOpen = isOpen },
                pendingChatTutorId = pendingChatTutorId,
                onPendingChatConsumed = { pendingChatTutorId = null }
            )
            3 -> StudentProfileScreen(
                userId   = user.id,
                onLogout = onLogout,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}