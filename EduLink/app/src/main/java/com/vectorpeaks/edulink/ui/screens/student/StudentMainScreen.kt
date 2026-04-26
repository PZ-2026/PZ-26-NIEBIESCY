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
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.theme.*

data class StudentTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(
    user: User,
    onLogout: () -> Unit
) {
    val tabs = listOf(
        StudentTab("Szukaj", Icons.Filled.Search, Icons.Outlined.Search),
        StudentTab("Historia", Icons.Filled.History, Icons.Outlined.History),
        StudentTab("Rozmowy", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
        StudentTab("Profil", Icons.Filled.Person, Icons.Outlined.Person)
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    var isChatDetailOpen by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            if (!isChatDetailOpen) {
                NavigationBar(
                    containerColor = Surface
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = if (selectedTab == index) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title
                                )
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
                                indicatorColor = PrimaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        when (selectedTab) {
            0 -> StudentSearchScreen(modifier = Modifier.padding(innerPadding))
            1 -> StudentHistoryScreen(user = user, modifier = Modifier.padding(innerPadding))
            2 -> StudentChatScreen(
                user = user,
                modifier = Modifier.padding(innerPadding),
                onChatOpen = { isOpen -> isChatDetailOpen = isOpen }
            )
            3 -> StudentProfileScreen(userId = user.id, onLogout = onLogout, modifier = Modifier.padding(innerPadding))
        }
    }
}
