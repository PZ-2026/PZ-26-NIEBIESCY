package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun TutorProfileScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isEditing by remember { mutableStateOf(false) }
    var editPhone by remember { mutableStateOf(user.phoneNumber) }
    var editCity by remember { mutableStateOf(user.address) }
    var showAddress by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Profil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Avatar + name
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UserAvatar(name = user.fullName, size = 80)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = user.fullName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Korepetytor",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Info card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Default.Email, label = "E-mail", value = user.email)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                if (isEditing) {
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Telefon") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editCity,
                        onValueChange = { editCity = it },
                        label = { Text("Miasto") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    ProfileInfoRow(icon = Icons.Default.Phone, label = "Telefon", value = user.phoneNumber.ifEmpty { "–" })
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ProfileInfoRow(icon = Icons.Default.LocationOn, label = "Miasto", value = user.address.ifEmpty { "–" })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Visibility settings
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Ustawienia widoczności", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Pokaż dokładny adres", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Uczniowie z zaakceptowaną lekcją zobaczą Twój adres",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }
                    Switch(checked = showAddress, onCheckedChange = { showAddress = it })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Edit button
        Button(
            onClick = { isEditing = !isEditing },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) Success else Primary)
        ) {
            Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isEditing) "Zapisz zmiany" else "Edytuj dane")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Actions card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                TextButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Wyloguj się", modifier = Modifier.weight(1f), color = OnBackground, style = MaterialTheme.typography.bodyLarge)
                }
                HorizontalDivider()
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Usuń konto", modifier = Modifier.weight(1f), color = Error, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Wyloguj się") },
            text = { Text("Czy na pewno chcesz się wylogować?") },
            confirmButton = {
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Wyloguj") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Anuluj") } }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń konto", color = Error) },
            text = { Text("Czy na pewno chcesz usunąć swoje konto? Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") } }
        )
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
