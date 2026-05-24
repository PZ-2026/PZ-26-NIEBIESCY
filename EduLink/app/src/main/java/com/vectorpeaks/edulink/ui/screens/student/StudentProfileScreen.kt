package com.vectorpeaks.edulink.ui.screens.student

import android.content.Context
import android.content.Intent
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
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.screens.login.ProfileUiState
import com.vectorpeaks.edulink.ui.screens.login.ProfileViewModel
import com.vectorpeaks.edulink.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun StudentProfileScreen(
    userId: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isEditing by remember { mutableStateOf(false) }
    var editPhone by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showScheduleDialog by remember { mutableStateOf(false) }

    var scheduleIncludeTutors by remember { mutableStateOf(false) }
    var scheduleIncludeHours by remember { mutableStateOf(false) }
    var scheduleIncludeDates by remember { mutableStateOf(false) }
    var scheduleDatesAll by remember { mutableStateOf(true) }
    val daysOfWeek = listOf("Poniedziałek", "Wtorek", "Środa", "Czwartek", "Piątek", "Sobota", "Niedziela")
    var scheduleSelectedDays by remember { mutableStateOf(setOf<String>()) }

    val availableSubjects by viewModel.tutorSubjects.collectAsState()
    val availableSubjectDtos by viewModel.tutorSubjectDtos.collectAsState()
    var scheduleSubjectsAll by remember { mutableStateOf(true) }
    var scheduleSelectedSubjects by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
        viewModel.loadStudentSubjects(userId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ProfileUiState.Success) {
            val user = (uiState as ProfileUiState.Success).user
            editPhone = user.phoneNumber ?: ""
            editCity  = user.address    ?: ""
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
                Text("Błąd: ${(uiState as ProfileUiState.Error).message}", color = Error)
            }
            is ProfileUiState.Success -> {
                val user = (uiState as ProfileUiState.Success).user
                val safeName  = user.fullName   .orEmpty().ifBlank { "—" }
                val safeEmail = user.email      .orEmpty().ifBlank { "—" }
                val safePhone = user.phoneNumber.orEmpty().ifBlank { "–" }
                val safeCity  = user.address    .orEmpty().ifBlank { "–" }

                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    UserAvatar(name = safeName, size = 80)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = safeName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(text = "Uczeń", style = MaterialTheme.typography.bodyMedium, color = Primary)
                }
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ProfileRow(icon = Icons.Default.Email, label = "E-mail", value = safeEmail)
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        if (isEditing) {
                            OutlinedTextField(
                                value = editPhone,
                                onValueChange = { editPhone = it; phoneError = null },
                                label = { Text("Telefon") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                singleLine = true,
                                isError = phoneError != null,
                                supportingText = {
                                    if (phoneError != null)
                                        Text(phoneError!!, color = Error, style = MaterialTheme.typography.bodySmall)
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
                            ProfileRow(icon = Icons.Default.Phone, label = "Telefon", value = safePhone)
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            ProfileRow(icon = Icons.Default.LocationOn, label = "Miasto", value = safeCity)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showScheduleDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Plan zajęć", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isEditing) {
                            if (!isPhoneValid(editPhone)) {
                                phoneError = "Numer telefonu musi zawierać 9 cyfr (lub być pusty)"
                                return@Button
                            }
                            viewModel.updateUser(user.id, user.copy(phoneNumber = editPhone, address = editCity)) {}
                        }
                        isEditing = !isEditing
                        if (!isEditing) phoneError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isEditing) Success else Primary)
                ) {
                    Icon(if (isEditing) Icons.Default.Check else Icons.Default.Edit, contentDescription = null)
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
                Button(onClick = { viewModel.logout(context = context) { showLogoutDialog = false; onLogout() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)) { Text("Wyloguj") }
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
                Button(onClick = { viewModel.deleteAccount(userId = userId, context = context) { showDeleteDialog = false; onLogout() } },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)) { Text("Usuń") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Anuluj") } }
        )
    }

    if (showScheduleDialog) {
        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Wygeneruj plan zajęć", fontWeight = FontWeight.Bold) },
            text = {
                Box(modifier = Modifier.heightIn(max = 400.dp)) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text("Wybierz, co ma zawierać plan:",
                            style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        CheckboxRow("Przedmioty", true) {}
                        Column(modifier = Modifier.padding(start = 16.dp).fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = scheduleSubjectsAll,
                                    onClick = { scheduleSubjectsAll = true; scheduleSelectedSubjects = setOf() },
                                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                )
                                Text("Wszystkie", style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = !scheduleSubjectsAll,
                                    onClick = { scheduleSubjectsAll = false },
                                    colors = RadioButtonDefaults.colors(selectedColor = Primary)
                                )
                                Text("Wybrane", style = MaterialTheme.typography.bodyMedium)
                            }
                            if (!scheduleSubjectsAll) {
                                if (availableSubjects.isEmpty()) {
                                    Text("Brak zaakceptowanych korepetycji",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = OnSurfaceVariant,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp))
                                } else {
                                    availableSubjects.forEach { subject ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = subject in scheduleSelectedSubjects,
                                                onCheckedChange = { checked ->
                                                    scheduleSelectedSubjects = if (checked)
                                                        scheduleSelectedSubjects + subject
                                                    else scheduleSelectedSubjects - subject
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = Primary)
                                            )
                                            Text(subject, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        CheckboxRow("Imiona korepetytorów", scheduleIncludeTutors) { scheduleIncludeTutors = it }
                        CheckboxRow("Łączna liczba godzin", scheduleIncludeHours) { scheduleIncludeHours = it }

                        CheckboxRow("Daty zajęć", scheduleIncludeDates) {
                            scheduleIncludeDates = it
                            if (!it) scheduleSelectedDays = setOf()
                        }
                        if (scheduleIncludeDates) {
                            Column(modifier = Modifier.padding(start = 16.dp).fillMaxWidth()) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = scheduleDatesAll,
                                        onClick = { scheduleDatesAll = true; scheduleSelectedDays = setOf() },
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
                                    daysOfWeek.forEach { day ->
                                        Row(modifier = Modifier.fillMaxWidth().padding(start = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = day in scheduleSelectedDays,
                                                onCheckedChange = { checked ->
                                                    scheduleSelectedDays = if (checked)
                                                        scheduleSelectedDays + day
                                                    else scheduleSelectedDays - day
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
                        scope.launch {
                            try {
                                val dayNameToInt = mapOf(
                                    "Poniedziałek" to 1, "Wtorek" to 2, "Środa" to 3,
                                    "Czwartek" to 4, "Piątek" to 5, "Sobota" to 6, "Niedziela" to 0
                                )
                                val selectedDayInts = if (scheduleDatesAll) emptyList()
                                else scheduleSelectedDays.mapNotNull { dayNameToInt[it] }

                                downloadAndOpenStudentSchedulePdf(
                                    context           = context,
                                    studentId         = userId,
                                    subjectIds        = if (scheduleSubjectsAll) emptyList()
                                    else availableSubjectDtos
                                        .filter { it.name in scheduleSelectedSubjects }
                                        .map { it.id },
                                    includeTutors     = scheduleIncludeTutors,
                                    includeTotalHours = scheduleIncludeHours,
                                    days              = selectedDayInts
                                )
                                showScheduleDialog = false
                            } catch (e: Exception) {
                                showScheduleDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Generuj") }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleDialog = false }) { Text("Anuluj") }
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
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
            Text(text = value, style = MaterialTheme.typography.bodyLarge)
        }
    }
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

suspend fun downloadAndOpenStudentSchedulePdf(
    context           : Context,
    studentId         : Int,
    subjectIds        : List<Int>,
    includeTutors     : Boolean,
    includeTotalHours : Boolean,
    days              : List<Int>
) = withContext(Dispatchers.IO) {
    val response = RetrofitClient.apiService.downloadStudentSchedulePdf(
        studentId         = studentId,
        subjectIds        = subjectIds,
        includeTutors     = includeTutors,
        includeTotalHours = includeTotalHours,
        days              = days
    )
    val cacheFile = File(
        context.cacheDir,
        "edulink_plan_zajec_uczen_${studentId}_${java.time.LocalDate.now()}.pdf"
    )
    response.byteStream().use { input ->
        cacheFile.outputStream().use { output -> input.copyTo(output) }
    }
    val uri = FileProvider.getUriForFile(
        context, "${context.packageName}.fileprovider", cacheFile
    )
    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

fun isPhoneValid(phone: String): Boolean {
    if (phone.isEmpty()) return true
    if (phone.matches(Regex("\\d{9}"))) return true
    return false
}