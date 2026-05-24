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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.screens.login.ProfileViewModel
import com.vectorpeaks.edulink.ui.screens.login.ProfileUiState
import com.vectorpeaks.edulink.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun TutorProfileScreen(
    userId: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showAddress by viewModel.showAddress.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editPhone by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    var showScheduleDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    // Checkboxy dla planu zajęć
    var scheduleIncludeSubjects by remember { mutableStateOf(true) }
    var scheduleIncludeStudents by remember { mutableStateOf(false) }
    var scheduleIncludeHours by remember { mutableStateOf(false) }

    // Dni tygodnia
    var scheduleIncludeDates by remember { mutableStateOf(false) }
    var scheduleDatesAll by remember { mutableStateOf(true) } // true = wszystkie, false = wybrane dni tygodnia
    val daysOfWeek = listOf("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")
    var scheduleSelectedDays by remember { mutableStateOf(setOf<String>()) }

    var reportIncludeEarnings by remember { mutableStateOf(true) }
    var reportIncludeLessonsCount by remember { mutableStateOf(true) }
    var reportIncludeStudents by remember { mutableStateOf(false) }
    var reportIncludeRatings by remember { mutableStateOf(false) }
    var reportRatingsCount by remember { mutableStateOf("5") }
    var reportRatingsCountError by remember { mutableStateOf<String?>(null) }

    // Przedmioty
    var reportIncludeSubjects by remember { mutableStateOf(false) }
    var reportSubjectsAll by remember { mutableStateOf(true) } // true = wszystkie, false = wybrane
    val availableSubjects by viewModel.tutorSubjects.collectAsState()
    var reportSelectedSubjects by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
        viewModel.loadTutorSubjects(userId)
    }


    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val user = (uiState as ProfileUiState.Success).user
            editPhone = user.phoneNumber ?: ""
            editCity = user.address ?: ""
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
            text = "Profil",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        when (uiState) {
            is ProfileUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ProfileUiState.Error -> {
                Text(
                    text = "Błąd: ${(uiState as ProfileUiState.Error).message}",
                    color = Error
                )
            }
            is ProfileUiState.Success -> {
                val user = (uiState as ProfileUiState.Success).user

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

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRow(icon = Icons.Default.Email, label = "E-mail", value = user.email)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        if (isEditing) {
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = {
                                    editPhone = it
                                    phoneError = null
                                },
                                label = { Text("Telefon") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                singleLine = true,
                                isError = phoneError != null,
                                supportingText = {
                                    if (phoneError != null) {
                                        Text(
                                            text = phoneError!!,
                                            color = Error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
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
                            ProfileRow(icon = Icons.Default.Phone, label = "Telefon", value = user.phoneNumber ?: "–")
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ProfileRow(icon = Icons.Default.LocationOn, label = "Miasto", value = user.address.ifEmpty { "–" })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

//                Card(
//                    shape = RoundedCornerShape(16.dp),
//                    colors = CardDefaults.cardColors(containerColor = Surface),
//                    elevation = CardDefaults.cardElevation(2.dp)
//                ) {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text("Ustawienia widoczności", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Column {
//                                Text("Pokaż dokładny adres", style = MaterialTheme.typography.bodyMedium)
//                                Text(
//                                    "Uczniowie z zaakceptowaną lekcją zobaczą Twój adres",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = OnSurfaceVariant
//                                )
//                            }
//                            Switch(
//                                checked = showAddress,
//                                onCheckedChange = { viewModel.updateShowAddress(it) } // ← ZMIANA
//                            )
//                        }
//                    }
//                }
//                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showScheduleDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Plan zajęć", style = MaterialTheme.typography.bodyMedium)
                    }

                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Raport", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isEditing) {
                            if (!isPhoneValid(editPhone)) {
                                phoneError = "Numer telefonu musi zawierać 9 cyfr (lub być pusty)"
                                return@Button
                            }
                            val updatedUser = user.copy(phoneNumber = editPhone, address = editCity)
                            viewModel.updateUser(user.id, updatedUser) { }
                        }
                        isEditing = !isEditing
                        if (!isEditing) phoneError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isEditing) Success else Primary
                    )
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isEditing) "Zapisz zmiany" else "Edytuj dane")
                }
                Spacer(modifier = Modifier.height(24.dp))

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
                            Text(
                                "Wyloguj się",
                                modifier = Modifier.weight(1f),
                                color = OnBackground,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        HorizontalDivider()
                        TextButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Error)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Usuń konto",
                                modifier = Modifier.weight(1f),
                                color = Error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
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
                Button(
                    onClick = {
                        viewModel.logout(context = context) {
                            showLogoutDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Wyloguj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Anuluj") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Usuń konto", color = Error) },
            text = { Text("Czy na pewno chcesz usunąć swoje konto? Ta operacja jest nieodwracalna.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(userId = userId, context = context) {
                            showDeleteDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("Usuń")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") }
            }
        )
    }

    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Wygeneruj plan zajęć", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                        Column(
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                "Wybierz, co ma zawierać plan:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            CheckboxRow("Przedmioty", reportIncludeSubjects) {
                                reportIncludeSubjects = it
                                if (!it) reportSelectedSubjects = setOf()
                            }
                            if (reportIncludeSubjects) {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = reportSubjectsAll,
                                            onClick = {
                                                reportSubjectsAll = true
                                                reportSelectedSubjects = setOf()
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wszystkie", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = !reportSubjectsAll,
                                            onClick = { reportSubjectsAll = false },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wybrane", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (!reportSubjectsAll) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (availableSubjects.isEmpty()) {
                                            Text(
                                                text = "Brak przypisanych przedmiotów",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        } else {
                                            availableSubjects.forEach { subject ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = subject in reportSelectedSubjects,
                                                        onCheckedChange = { checked ->
                                                            reportSelectedSubjects = if (checked)
                                                                reportSelectedSubjects + subject
                                                            else
                                                                reportSelectedSubjects - subject
                                                        },
                                                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                                                    )
                                                    Text(subject, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            CheckboxRow("Imiona uczniów", scheduleIncludeStudents) { scheduleIncludeStudents = it }
                            CheckboxRow("Łączna liczba godzin", scheduleIncludeHours) { scheduleIncludeHours = it }

                            // --- Sekcja: Daty ---
                            CheckboxRow("Daty zajęć", scheduleIncludeDates) {
                                scheduleIncludeDates = it
                                if (!it) scheduleSelectedDays = setOf()
                            }
                            if (scheduleIncludeDates) {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = scheduleDatesAll,
                                            onClick = {
                                                scheduleDatesAll = true
                                                scheduleSelectedDays = setOf()
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wszystkie", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = !scheduleDatesAll,
                                            onClick = { scheduleDatesAll = false },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wybrane dni tygodnia", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (!scheduleDatesAll) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        daysOfWeek.forEach { day ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = day in scheduleSelectedDays,
                                                    onCheckedChange = { checked ->
                                                        scheduleSelectedDays = if (checked)
                                                            scheduleSelectedDays + day
                                                        else
                                                            scheduleSelectedDays - day
                                                    },
                                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                                )
                                                Text(day, style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
            },

            confirmButton = {
                Button(
                    onClick = {
                        // TODO: logika generowania planu z wybranymi opcjami
                        showScheduleDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Generuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) { Text("Anuluj") }
            }
        )
    }


    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Raport korepetycji", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        Column {
                            Text(
                                "Wybierz, co ma zawierać raport:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CheckboxRow("Liczba prowadzonych korepetycji", reportIncludeLessonsCount) { reportIncludeLessonsCount = it }
                            CheckboxRow("Liczba zakończonych korepetycji", reportIncludeLessonsCount) { reportIncludeLessonsCount = it }
                            CheckboxRow("Lista uczniów", reportIncludeStudents) { reportIncludeStudents = it }
                            CheckboxRow("Przedmioty", reportIncludeSubjects) {
                                reportIncludeSubjects = it
                                if (!it) reportSelectedSubjects = setOf()
                            }
                            if (reportIncludeSubjects) {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = reportSubjectsAll,
                                            onClick = {
                                                reportSubjectsAll = true
                                                reportSelectedSubjects = setOf()
                                            },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wszystkie", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(
                                            selected = !reportSubjectsAll,
                                            onClick = { reportSubjectsAll = false },
                                            colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                        )
                                        Text("Wybrane", style = MaterialTheme.typography.bodyMedium)
                                    }
                                    if (!reportSubjectsAll) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        if (availableSubjects.isEmpty()) {
                                            Text(
                                                text = "Brak przypisanych przedmiotów",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = OnSurfaceVariant,
                                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                            )
                                        } else {
                                            availableSubjects.forEach { subject ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(start = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Checkbox(
                                                        checked = subject in reportSelectedSubjects,
                                                        onCheckedChange = { checked ->
                                                            reportSelectedSubjects = if (checked)
                                                                reportSelectedSubjects + subject
                                                            else
                                                                reportSelectedSubjects - subject
                                                        },
                                                        colors = CheckboxDefaults.colors(checkedColor = Primary)
                                                    )
                                                    Text(subject, style = MaterialTheme.typography.bodyMedium)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            CheckboxRow("Opinie uczniów", reportIncludeRatings) { reportIncludeRatings = it }
                            if (reportIncludeRatings) {
                                Column(
                                    modifier = Modifier
                                        .padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
                                        .fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = reportRatingsCount,
                                        onValueChange = { value ->
                                            reportRatingsCount = value.filter { it.isDigit() }
                                            reportRatingsCountError = when {
                                                reportRatingsCount.isEmpty() -> "Podaj liczbę opinii"
                                                reportRatingsCount.toInt() < 1 -> "Minimalna wartość to 1"
                                                reportRatingsCount.toInt() > 100 -> "Maksymalna wartość to 100"
                                                else -> null
                                            }
                                        },
                                        label = { Text("Liczba najnowszych opinii") },
                                        singleLine = true,
                                        isError = reportRatingsCountError != null,
                                        supportingText = {
                                            Text(
                                                text = reportRatingsCountError ?: "Zakres: 1–100",
                                                color = if (reportRatingsCountError != null) Error else OnSurfaceVariant,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // TODO: logika generowania raportu
                        showReportDialog = false
                    },
                    enabled = !reportIncludeRatings || reportRatingsCountError == null && reportRatingsCount.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Generuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun ProfileRow(
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

fun isPhoneValid(phone: String): Boolean {
    return phone.isEmpty() || phone.matches(Regex("\\d{9}"))
}

@Composable
private fun CheckboxRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}