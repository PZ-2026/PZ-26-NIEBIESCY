package com.vectorpeaks.edulink.ui.screens.admin

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.components.SectionHeader
import com.vectorpeaks.edulink.ui.components.StatCard
import com.vectorpeaks.edulink.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun AdminReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReportsViewModel = viewModel()
) {
    val reports   by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error     by viewModel.error.collectAsState()

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    // Daty
    var dateFrom by remember { mutableStateOf(java.time.LocalDate.now().minusDays(30).toString()) }
    var dateTo   by remember { mutableStateOf(java.time.LocalDate.now().toString()) }

    // Checkboxy i pola numeryczne sekcji
    var includeSubjects  by remember { mutableStateOf(true) }
    var topSubjectsN     by remember { mutableStateOf("5") }
    var includeTutors    by remember { mutableStateOf(true) }
    var topTutorsN       by remember { mutableStateOf("5") }

    // Stan generowania
    // Błędy walidacji pól numerycznych
    val subjectsError = if (includeSubjects) {
        val n = topSubjectsN.toIntOrNull()
        when {
            n == null     -> "Podaj liczbę"
            n < 1         -> "Min. 1"
            n > 100       -> "Max. 100"
            else          -> null
        }
    } else null

    val tutorsError = if (includeTutors) {
        val n = topTutorsN.toIntOrNull()
        when {
            n == null     -> "Podaj liczbę"
            n < 1         -> "Min. 1"
            n > 100       -> "Max. 100"
            else          -> null
        }
    } else null

    val hasValidationError = subjectsError != null || tutorsError != null

    var pdfLoading by remember { mutableStateOf(false) }
    var pdfError   by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.loadReports() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Raporty i statystyki",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            error != null -> Box(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp),
                contentAlignment = Alignment.Center
            ) { Text("Błąd: $error", color = Error) }

            else -> {
                val r = reports
                if (r != null) {
                    SectionHeader(title = "Statystyki platformy")
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title           = "Łącznie rezerwacji",
                            value           = "${r.totalBookings}",
                            modifier        = Modifier.weight(1f),
                            backgroundColor = PrimaryContainer,
                            textColor       = OnPrimaryContainer
                        )
                        StatCard(
                            title           = "Łącznie ofert",
                            value           = "${r.totalOffers}",
                            modifier        = Modifier.weight(1f),
                            backgroundColor = TertiaryContainer,
                            textColor       = OnTertiaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    SectionHeader(title = "Popularne przedmioty")
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        shape     = RoundedCornerShape(16.dp),
                        colors    = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            r.popularSubjects.forEachIndexed { index, entry ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape    = RoundedCornerShape(8.dp),
                                            color    = when (index) {
                                                0    -> WarningContainer
                                                1    -> PrimaryContainer
                                                else -> SurfaceVariant
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text       = "${index + 1}",
                                                    style      = MaterialTheme.typography.labelLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text       = entry.name,
                                            style      = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text  = "${entry.reviewCount} opinii",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnSurfaceVariant
                                    )
                                }
                                if (index < r.popularSubjects.lastIndex) HorizontalDivider()
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Generuj raport PDF ──────────────────────────────────────
                SectionHeader(title = "Generuj raport PDF")
                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape     = RoundedCornerShape(16.dp),
                    colors    = CardDefaults.cardColors(containerColor = Surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        Text(
                            text       = "Raport statystyk systemowych",
                            style      = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text  = "Raport zawiera: rezerwacje, nowe oferty, nowych użytkowników, " +
                                    "top przedmioty i top korepetytorów w wybranym przedziale.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // ── Daty ───────────────────────────────────────────
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value         = dateFrom,
                                onValueChange = { dateFrom = it },
                                label         = { Text("Od (YYYY-MM-DD)") },
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                modifier      = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value         = dateTo,
                                onValueChange = { dateTo = it },
                                label         = { Text("Do (YYYY-MM-DD)") },
                                singleLine    = true,
                                shape         = RoundedCornerShape(12.dp),
                                modifier      = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text       = "Sekcje raportu",
                            style      = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = OnSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Checkbox: top przedmioty ───────────────────────
                        SectionToggleRow(
                            label         = "Top przedmioty",
                            checked       = includeSubjects,
                            onChecked     = { includeSubjects = it },
                            countValue    = topSubjectsN,
                            onCountChange = { topSubjectsN = it },
                            enabled       = includeSubjects,
                            errorMessage  = subjectsError
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ── Checkbox: top korepetytorzy ────────────────────
                        SectionToggleRow(
                            label         = "Top korepetytorzy",
                            checked       = includeTutors,
                            onChecked     = { includeTutors = it },
                            countValue    = topTutorsN,
                            onCountChange = { topTutorsN = it },
                            enabled       = includeTutors,
                            errorMessage  = tutorsError
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // ── Błąd ───────────────────────────────────────────
                        if (pdfError != null) {
                            Text(
                                text     = "Błąd: $pdfError",
                                color    = Error,
                                style    = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        // ── Przycisk ───────────────────────────────────────
                        Button(
                            onClick = {
                                scope.launch {
                                    pdfLoading = true
                                    pdfError   = null
                                    try {
                                        downloadAndOpenPdf(
                                            context         = context,
                                            from            = dateFrom,
                                            to              = dateTo,
                                            topSubjectsN    = topSubjectsN.toIntOrNull()
                                                ?.coerceIn(1, 100) ?: 5,
                                            topTutorsN      = topTutorsN.toIntOrNull()
                                                ?.coerceIn(1, 100) ?: 5,
                                            includeSubjects = includeSubjects,
                                            includeTutors   = includeTutors
                                        )
                                    } catch (e: Exception) {
                                        pdfError = e.message ?: "Nieznany błąd"
                                    } finally {
                                        pdfLoading = false
                                    }
                                }
                            },
                            enabled  = !pdfLoading && !hasValidationError,
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            if (pdfLoading) {
                                CircularProgressIndicator(
                                    modifier    = Modifier.size(18.dp),
                                    color       = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generowanie...")
                            } else {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generuj raport PDF")
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}

/**
 * Wiersz z checkboxem i polem numerycznym dla jednej sekcji raportu.
 *
 * @param label        nazwa sekcji
 * @param checked      czy sekcja jest włączona
 * @param onChecked    callback zmiany checkboxa
 * @param countValue   aktualna wartość pola liczbowego (jako String)
 * @param onCountChange callback zmiany pola liczbowego
 * @param enabled      czy pole liczbowe jest aktywne (= czy checkbox zaznaczony)
 */
@Composable
private fun SectionToggleRow(
    label        : String,
    checked      : Boolean,
    onChecked    : (Boolean) -> Unit,
    countValue   : String,
    onCountChange: (String) -> Unit,
    enabled      : Boolean,
    errorMessage : String? = null
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Checkbox + etykieta
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier          = Modifier.weight(1f)
        ) {
            Checkbox(
                checked         = checked,
                onCheckedChange = onChecked,
                colors          = CheckboxDefaults.colors(
                    checkedColor = Primary
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) OnSurface else OnSurfaceVariant
            )
        }

        // Pole numeryczne z walidacją
        Column(horizontalAlignment = Alignment.End) {
            OutlinedTextField(
                value         = countValue,
                onValueChange = { new ->
                    if (new.all { it.isDigit() } && new.length <= 3) {
                        onCountChange(new)
                    }
                },
                label           = { Text("Ile") },
                singleLine      = true,
                enabled         = enabled,
                isError         = errorMessage != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape           = RoundedCornerShape(10.dp),
                modifier        = Modifier.width(88.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = OnSurfaceVariant.copy(alpha = 0.3f),
                    disabledLabelColor  = OnSurfaceVariant.copy(alpha = 0.4f),
                    disabledTextColor   = OnSurfaceVariant.copy(alpha = 0.4f),
                    errorBorderColor    = Error,
                    errorLabelColor     = Error
                )
            )
            if (errorMessage != null) {
                Text(
                    text     = errorMessage,
                    color    = Error,
                    style    = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                )
            }
        }
    }
}

/**
 * Pobiera PDF z backendu z wybranymi opcjami, zapisuje w cache i otwiera
 * w zewnętrznej przeglądarce PDF przy użyciu FileProvider.
 */
private suspend fun downloadAndOpenPdf(
    context        : Context,
    from           : String,
    to             : String,
    topSubjectsN   : Int,
    topTutorsN     : Int,
    includeSubjects: Boolean,
    includeTutors  : Boolean
) = withContext(Dispatchers.IO) {

    val responseBody = RetrofitClient.apiService.downloadReportPdf(
        from            = from,
        to              = to,
        topSubjectsN    = topSubjectsN,
        topTutorsN      = topTutorsN,
        includeSubjects = includeSubjects,
        includeTutors   = includeTutors
    )
    val bytes = responseBody.bytes()

    val cacheFile = File(context.cacheDir, "edulink_raport_${from}_${to}.pdf")
    cacheFile.writeBytes(bytes)

    val uri: Uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        cacheFile
    )

    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/pdf")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    context.startActivity(intent)
}