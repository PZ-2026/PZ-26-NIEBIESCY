package com.vectorpeaks.edulink.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import com.vectorpeaks.edulink.data.model.Slot
import com.vectorpeaks.edulink.ui.components.RatingBar
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offer: Offer,
    studentId: Int,
    onBack: () -> Unit,
    onTutorClick: (tutorId: Int, tutorName: String) -> Unit,
    bookingViewModel: BookingViewModel = viewModel()
) {
    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<Slot?>(null) }
    var bookingConfirmed by remember { mutableStateOf(false) }
    var showSlotTakenDialog by remember { mutableStateOf(false) }
    val offerDetailViewModel: OfferDetailViewModel = viewModel()
    val refreshedOffer by offerDetailViewModel.offer.collectAsState()
    val displayOffer = refreshedOffer ?: offer
    val bookingUiState by bookingViewModel.uiState.collectAsState()

    LaunchedEffect(offer.id) {
        offerDetailViewModel.loadOffer(offer.id)
    }

    LaunchedEffect(offer.id, studentId) {
        showBookingDialog = false
        selectedSlot = null
        bookingConfirmed = false
        bookingViewModel.resetUiState()
    }

    BackHandler {
        onBack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Szczegóły oferty") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Wstecz")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Subject & Price
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayOffer.subject,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = "${displayOffer.pricePerHour.toInt()} zł/h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBar(rating = displayOffer.rating, starSize = 20f)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${displayOffer.rating} (${displayOffer.reviewCount} opinii)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tutor card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTutorClick(displayOffer.tutorId, displayOffer.tutorName) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = displayOffer.tutorName, size = 56)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = displayOffer.tutorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayOffer.city,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                        if (displayOffer.isOnline) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Laptop,
                                    contentDescription = null,
                                    tint = Tertiary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dostępne online",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Tertiary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zobacz opinie →",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "Zobacz opinie",
                        tint = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Opis",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = displayOffer.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Available slots
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dostępne terminy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    displayOffer.availableSlots.forEach { slot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slot.label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Button(
                                onClick = {
                                    selectedSlot = slot
                                    showBookingDialog = true
                                },
                                enabled = !slot.isBooked,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Primary,
                                    disabledContainerColor = OnSurfaceVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    if (slot.isBooked) "Zajęty" else "Rezerwuj",
                                    color = if (slot.isBooked) OnSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        if (slot != displayOffer.availableSlots.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Booking confirmation dialog
    if (showBookingDialog) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = { Text("Potwierdź rezerwację") },
            text = {
                Column {
                    Text("Przedmiot: ${displayOffer.subject}")
                    Text("Korepetytor: ${displayOffer.tutorName}")
                    if (selectedSlot != null) {
                        Text("Termin: ${selectedSlot!!.label}")
                    }
                    Text("Cena: ${displayOffer.pricePerHour.toInt()} zł/h")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        bookingViewModel.createBooking(
                            offerId = displayOffer.id,
                            studentId = studentId,
                            slotId = selectedSlot!!.id,
                            onSuccess = {
                                showBookingDialog = false
                                bookingConfirmed = true
                            },
                            onSlotTaken = {
                                showBookingDialog = false
                                showSlotTakenDialog = true
                                offerDetailViewModel.loadOffer(displayOffer.id)
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Rezerwuj")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }

    when (val state = bookingUiState) {
        is BookingUiState.Success -> {
            LaunchedEffect(Unit) {
                if (!bookingConfirmed) bookingConfirmed = true
            }
        }
        is BookingUiState.Error -> {
            LaunchedEffect(state.message) {
                bookingConfirmed = false
            }
        }
        else -> {}
    }

    if (bookingConfirmed) {
        AlertDialog(
            onDismissRequest = {
                bookingConfirmed = false
                bookingViewModel.resetUiState()
                onBack()
            },
            title = { Text("Sukces!", color = Success) },
            text = { Text("Rezerwacja została złożona. Korepetytor otrzyma powiadomienie.") },
            confirmButton = {
                Button(
                    onClick = {
                        bookingConfirmed = false
                        bookingViewModel.resetUiState()
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("OK")
                }
            }
        )
    }

    if (showSlotTakenDialog) {
        AlertDialog(
            onDismissRequest = {
                showSlotTakenDialog = false
                bookingViewModel.resetUiState()
            },
            title = { Text("Termin niedostępny", color = Error) },
            text = { Text("Ten termin został właśnie zarezerwowany przez kogoś innego. Wybierz inny termin.") },
            confirmButton = {
                Button(
                    onClick = {
                        showSlotTakenDialog = false
                        bookingViewModel.resetUiState()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) {
                    Text("OK")
                }
            }
        )
    }
}