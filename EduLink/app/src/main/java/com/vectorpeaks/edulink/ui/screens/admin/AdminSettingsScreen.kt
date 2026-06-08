package com.vectorpeaks.edulink.ui.screens.admin

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun AdminSettingsScreen(
    user: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminSettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val maintenanceStatus by viewModel.maintenanceStatus.collectAsState()
    var maxPricePerHour by remember { mutableStateOf("200") }
    var globalMessage by remember { mutableStateOf("") }
    var isMessageEnabled by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    var subjectsExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadSettings()
        viewModel.loadSubjects()
        viewModel.loadMaintenanceStatus()
    }

    LaunchedEffect(settings) {
        settings?.let {
            maxPricePerHour = it.maxPricePerHour.toInt().toString()
            globalMessage = it.globalMessage
            isMessageEnabled = it.globalMessageEnabled
        }
    }

    // Show snackbar on save success
    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            viewModel.resetSaveSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Ustawienia",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Admin profile
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
                text = "Administrator",
                style = MaterialTheme.typography.bodyMedium,
                color = Primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Profile info
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsRow(icon = Icons.Default.Email, label = "E-mail", value = user.email)
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                SettingsRow(icon = Icons.Default.Phone, label = "Telefon", value = user.phoneNumber.ifEmpty { "–" })
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text("Błąd: $error", color = Error)
            }
            else -> {
                // Global settings
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Konfiguracja globalna",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Maintenance mode
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Tryb serwisowy", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    "Blokuje dostęp do aplikacji dla użytkowników",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = maintenanceStatus.active,
                                onCheckedChange = { viewModel.toggleMaintenance(it) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = Warning
                                )
                            )
                        }
                        if (maintenanceStatus.active && maintenanceStatus.startsAt != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "⏱ Prace serwisowe zaplanowane. Użytkownicy widzą odliczanie.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Warning,
                                fontWeight = FontWeight.Medium
                            )
                            if (!maintenanceStatus.fullyActive) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { viewModel.shortenMaintenanceTime() },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Warning)
                                ) {
                                    Text("Skróć do 1 minuty", color = androidx.compose.ui.graphics.Color.White)
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Max price
                        Text("Maks. cena za godzinę (zł)", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = maxPricePerHour,
                            onValueChange = { maxPricePerHour = it },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                val price = maxPricePerHour.toDoubleOrNull() ?: 200.0
                                viewModel.savePriceLimit(price)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Zapisz limit cenowy")
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                        // Global message
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Globalny komunikat", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text(
                                    "Włącz, aby wyświetlić zapisany komunikat użytkownikom",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isMessageEnabled,
                                onCheckedChange = { enabled ->
                                    isMessageEnabled = enabled
                                    viewModel.setGlobalMessageEnabled(enabled, globalMessage.trim())
                                },
                                colors = SwitchDefaults.colors(checkedTrackColor = Primary)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = globalMessage,
                            onValueChange = { globalMessage = it },
                            placeholder = { Text("Wpisz komunikat...") },
                            minLines = 2,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.saveGlobalMessage(globalMessage.trim(), isMessageEnabled)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Zapisz komunikat")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // Subject management
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zarządzanie przedmiotami",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${subjects.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = OnSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        val visibleSubjects = if (subjectsExpanded) subjects else subjects.take(5)
                        visibleSubjects.forEach { subject ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subject.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(onClick = { viewModel.deleteSubject(subject.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Usuń", tint = Error, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        // Show more / less button
                        if (subjects.size > 5) {
                            TextButton(
                                onClick = { subjectsExpanded = !subjectsExpanded },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (subjectsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (subjectsExpanded) "Pokaż mniej" else "Pokaż więcej (${subjects.size - 5})",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                newSubjectName = ""
                                showAddSubjectDialog = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dodaj przedmiot")
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // Logout
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            TextButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = OnSurfaceVariant)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Wyloguj się", modifier = Modifier.weight(1f), color = OnBackground, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    // Logout dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Wyloguj się") },
            text = { Text("Czy na pewno chcesz się wylogować?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.logout(context) {
                            showLogoutDialog = false
                            onLogout()  // navigation after succes logout
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Wyloguj") }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text("Anuluj") } }
        )
    }

    // Add subject dialog
    if (showAddSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            title = { Text("Dodaj przedmiot") },
            text = {
                OutlinedTextField(
                    value = newSubjectName,
                    onValueChange = { newSubjectName = it },
                    label = { Text("Nazwa przedmiotu") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSubjectName.isNotBlank()) {
                            viewModel.addSubject(newSubjectName.trim())
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Dodaj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

@Composable
private fun SettingsRow(
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
