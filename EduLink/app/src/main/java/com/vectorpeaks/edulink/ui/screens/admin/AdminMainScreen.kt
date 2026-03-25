package com.vectorpeaks.edulink.ui.screens.admin

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.vectorpeaks.edulink.data.model.User
import com.vectorpeaks.edulink.ui.theme.*

data class AdminTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun AdminMainScreen(
    user: User,
    onLogout: () -> Unit
) {
    val tabs = listOf(
        AdminTab("Pulpit", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
        AdminTab("Użytkownicy", Icons.Filled.People, Icons.Outlined.People),
        AdminTab("Raporty", Icons.Filled.Assessment, Icons.Outlined.Assessment),
        AdminTab("Ustawienia", Icons.Filled.Settings, Icons.Outlined.Settings)
    )
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Surface) {
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
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                style = MaterialTheme.typography.labelSmall
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
    ) { innerPadding ->
        when (selectedTab) {
            0 -> AdminDashboardScreen(user = user, modifier = Modifier.padding(innerPadding))
            1 -> AdminUsersScreen(modifier = Modifier.padding(innerPadding))
            2 -> AdminReportsScreen(modifier = Modifier.padding(innerPadding))
            3 -> AdminSettingsScreen(user = user, onLogout = onLogout, modifier = Modifier.padding(innerPadding))
        }
    }
}
