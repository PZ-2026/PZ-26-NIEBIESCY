package com.vectorpeaks.edulink.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.ui.components.EduSearchBar
import com.vectorpeaks.edulink.ui.components.UserCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun AdminUsersScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminUsersViewModel = viewModel()
) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedRoleFilter by remember { mutableIntStateOf(0) }
    val roleFilters = listOf("Wszyscy", "Uczniowie", "Korepetytorzy", "Administratorzy")

    var showAddUserDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadUsers()
    }

    val filteredUsers = users.filter { user ->
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
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

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Błąd: $error", color = Error)
                    }
                }
                else -> {
                    Text(
                        text = "Znaleziono: ${filteredUsers.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredUsers) { user ->
                            UserCard(
                                user = user,
                                onToggleBlock = { viewModel.toggleUserBlock(user.id, user.accountStatusId) }
                            )
                        }
                    }
                }
            }
        }

        // FAB – add user
        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            containerColor = Primary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = "Dodaj użytkownika", tint = androidx.compose.ui.graphics.Color.White)
        }
    }

    // Add user dialog
    if (showAddUserDialog) {
        AddUserDialog(
            onDismiss = { showAddUserDialog = false },
            onConfirm = { firstName, lastName, email, password, roleId, city, phone ->
                viewModel.createUser(firstName, lastName, email, password, roleId, city, phone)
                showAddUserDialog = false
            }
        )
    }
}

@Composable
private fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, String, String) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedRoleIndex by remember { mutableIntStateOf(0) }

    val roles = listOf("Uczeń" to 3, "Korepetytor" to 2, "Admin" to 1)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Dodaj użytkownika", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Imię") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Nazwisko") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Hasło") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("Miasto") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Telefon") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Rola", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    roles.forEachIndexed { index, (label, _) ->
                        SegmentedButton(
                            selected = selectedRoleIndex == index,
                            onClick = { selectedRoleIndex = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank() &&
                        email.isNotBlank() && password.isNotBlank()
                    ) {
                        onConfirm(
                            firstName.trim(), lastName.trim(), email.trim(),
                            password, roles[selectedRoleIndex].second,
                            city.trim(), phoneNumber.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Utwórz konto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Anuluj")
            }
        }
    )
}
