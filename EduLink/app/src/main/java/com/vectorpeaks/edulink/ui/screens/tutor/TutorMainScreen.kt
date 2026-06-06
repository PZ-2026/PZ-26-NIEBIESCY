package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.theme.*
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModel
import com.vectorpeaks.edulink.ui.viewmodel.ChatViewModelFactory
import androidx.compose.ui.text.style.TextOverflow

data class TutorTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun TutorMainScreen(
    user: User,
    onLogout: () -> Unit,
    onNavigateToReviews: (tutorId: Int, tutorName: String) -> Unit,
    startTab: Int = 0
) {

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
        TutorTab("Pulpit",     Icons.Filled.Dashboard,    Icons.Outlined.Dashboard),
        TutorTab("Oferty",     Icons.Filled.LocalOffer,   Icons.Outlined.LocalOffer),
        TutorTab("Rezerwacje", Icons.Filled.EventNote,    Icons.Outlined.EventNote),
        TutorTab("Rozmowy",    Icons.Filled.ChatBubble,   Icons.Outlined.ChatBubbleOutline),
        TutorTab("Profil",     Icons.Filled.Person,       Icons.Outlined.Person)
    )
    var selectedTab by remember { mutableIntStateOf(startTab) }
    var isChatDetailOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (!isChatDetailOpen) {
                NavigationBar(containerColor = Surface) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
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
                                    fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis           
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Primary,
                                selectedTextColor = Primary,
                                indicatorColor = PrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> TutorDashboardScreen(user = user, modifier = Modifier.padding(innerPadding))
            1 -> TutorOffersScreen(
                user = user,
                modifier = Modifier.padding(innerPadding),
                onNavigateToReviews = onNavigateToReviews
            )
            2 -> TutorReservationsScreen(tutorId = user.id, modifier = Modifier.padding(innerPadding))
            3 -> TutorChatScreen(
                user = user,
                modifier = Modifier.padding(innerPadding),
                onChatOpen = { isOpen -> isChatDetailOpen = isOpen }
            )
            4 -> TutorProfileScreen(userId = user.id, onLogout = onLogout, modifier = Modifier.padding(innerPadding))
        }
    }
}