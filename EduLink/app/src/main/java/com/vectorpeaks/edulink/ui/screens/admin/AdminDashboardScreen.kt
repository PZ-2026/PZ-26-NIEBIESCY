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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.Reservation
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.components.SectionHeader
import com.vectorpeaks.edulink.ui.components.StatCard
import com.vectorpeaks.edulink.ui.theme.*
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import com.vectorpeaks.edulink.ui.components.RatingBar
import com.vectorpeaks.edulink.ui.theme.OnPrimaryContainer
import com.vectorpeaks.edulink.ui.theme.PrimaryContainer
import com.vectorpeaks.edulink.ui.theme.Success

@Composable
fun AdminDashboardScreen(
    user: User,
    modifier: Modifier = Modifier,
    onNavigateToTab: (Int) -> Unit = {},
    viewModel: AdminDashboardViewModel = viewModel(),
    settingsViewModel: AdminSettingsViewModel = viewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val pendingBookings by viewModel.pendingBookings.collectAsState()
    val pendingOffers by viewModel.pendingOffers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var newSubjectName by remember { mutableStateOf("") }
    var detailOffer by remember { mutableStateOf<Offer?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Panel administratora",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Text(
            text = "Witaj, ${user.firstName}",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 48.dp), contentAlignment = Alignment.Center) {
                    Text("Błąd: $error", color = Error)
                }
            }
            else -> {
                val s = stats
                if (s != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Użytkownicy",
                            value = "${s.totalUsers}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = PrimaryContainer,
                            textColor = OnPrimaryContainer
                        )
                        StatCard(
                            title = "Oferty",
                            value = "${s.totalOffers}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = SecondaryContainer,
                            textColor = OnSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Korepetytorzy",
                            value = "${s.tutorsCount}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = TertiaryContainer,
                            textColor = OnTertiaryContainer
                        )
                        StatCard(
                            title = "Uczniowie",
                            value = "${s.studentsCount}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = SuccessContainer,
                            textColor = OnBackground
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard(
                            title = "Rezerwacje",
                            value = "${s.totalBookings}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = WarningContainer,
                            textColor = OnBackground
                        )
                        StatCard(
                            title = "Oczekujące",
                            value = "${s.pendingCount}",
                            modifier = Modifier.weight(1f),
                            backgroundColor = ErrorContainer,
                            textColor = OnBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (pendingOffers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SectionHeader(title = "Oczekujące oferty")
                    Spacer(modifier = Modifier.height(12.dp))
                    pendingOffers.forEach { offer ->
                        PendingOfferCard(
                            offer = offer,
                            onAccept = { viewModel.approveOffer(offer.id) },
                            onReject = { viewModel.rejectOffer(offer.id) },
                            onDetail = { detailOffer = offer }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader(title = "Szybkie akcje")
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        onClick = { onNavigateToTab(1) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Dodaj użytkownika", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Card(
                        onClick = {
                            newSubjectName = ""
                            showAddSubjectDialog = true
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Tertiary, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Dodaj przedmiot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }

    detailOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { detailOffer = null },
            title = {
                Text(
                    text = offer.subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    val (statusText, statusColor) = when (offer.status.orEmpty()) {
                        "ACTIVE", "ACCEPTED" -> "Zaakceptowana" to Success
                        "PENDING"            -> "Oczekuje na akceptację" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
                        "REJECTED"           -> "Odrzucona" to Error
                        else                 -> "Nieznany" to OnSurfaceVariant
                    }
                    Text(statusText, style = MaterialTheme.typography.labelLarge, color = statusColor, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()
                    Text("Korepetytor", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(offer.tutorName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    HorizontalDivider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Cena", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("${offer.pricePerHour.toInt()} zł/h", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Typ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(if (offer.isOnline) "Online" else "Stacjonarne", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    HorizontalDivider()
                    Text("Opis", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(offer.description, style = MaterialTheme.typography.bodyMedium)
                    HorizontalDivider()
                    Text("Ocena tutora", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBar(rating = offer.rating)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${offer.rating} (${offer.reviewCount} opinii)", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                    HorizontalDivider()
                    Text("Terminy", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    if (offer.availableSlots.isNullOrEmpty()) {
                        Text("Brak przypisanych terminów", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    } else {
                        offer.availableSlots.orEmpty().forEach { slot ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = PrimaryContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = OnPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailOffer = null }) { Text("Zamknij") }
            }
        )
    }

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
                            settingsViewModel.addSubject(newSubjectName.trim())
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
private fun PendingOfferCard(
    offer: Offer,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
                    text = offer.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = WarningContainer
                ) {
                    Text(
                        text = "Oczekująca",
                        color = OnBackground,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Korepetytor: ${offer.tutorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${offer.pricePerHour.toInt()} zł/h  •  ${if (offer.isOnline) "Online" else "Stacjonarne"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
            }
            if (offer.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = offer.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    maxLines = 2
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDetail) {
                    Text("Szczegóły", color = Primary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                OutlinedButton(
                    onClick = onReject,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Odrzuć")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Zatwierdź")
                }
            }
        }
    }
}

private fun convertBookingToReservation(booking: BookingResponse): Reservation {
    return Reservation(
        id = booking.id,
        offerId = booking.offerId,
        studentId = 0,
        studentName = "",
        tutorId = booking.tutorId,
        tutorName = booking.tutorName,
        subject = booking.subject,
        date = booking.date,
        time = booking.time,
        price = booking.price,
        status = when (booking.status) {
            "PENDING" -> ReservationStatus.PENDING
            "ACCEPTED" -> ReservationStatus.ACCEPTED
            "REJECTED" -> ReservationStatus.REJECTED
            "COMPLETED" -> ReservationStatus.COMPLETED
            else -> ReservationStatus.PENDING
        },
        rating = booking.rating
    )
}
