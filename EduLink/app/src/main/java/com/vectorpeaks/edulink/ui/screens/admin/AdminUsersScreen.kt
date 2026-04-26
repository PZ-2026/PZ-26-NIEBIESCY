package com.vectorpeaks.edulink.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.ui.components.EduSearchBar
import com.vectorpeaks.edulink.ui.components.UserCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun AdminUsersScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableIntStateOf(0) }
    val roleFilters = listOf("Wszyscy", "Uczniowie", "Korepetytorzy", "Administratorzy")

    val filteredUsers = FakeData.users.filter { user ->
        val matchesSearch = searchQuery.isBlank() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true)
        val matchesRole = when (selectedRoleFilter) {
            1 -> user.getRole() == RoleID.STUDENT
            2 -> user.getRole() == RoleID.TUTOR
            3 -> user.getRole() == RoleID.ADMIN
            else -> true
        }
        matchesSearch && matchesRole
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Zarządzanie użytkownikami",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        EduSearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Szukaj użytkownika..."
        )
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedRoleFilter,
            containerColor = Background,
            edgePadding = 0.dp,
            divider = {}
        ) {
            roleFilters.forEachIndexed { index, label ->
                Tab(
                    selected = selectedRoleFilter == index,
                    onClick = { selectedRoleFilter = index },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (selectedRoleFilter == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Primary,
                    unselectedContentColor = OnSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Znaleziono: ${filteredUsers.size}",
            style = MaterialTheme.typography.labelMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(filteredUsers) { user ->
                UserCard(
                    user = user,
                    onToggleBlock = { /* TODO: toggle block */ }
                )
            }
        }
    }
}
