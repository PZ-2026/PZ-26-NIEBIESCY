package com.vectorpeaks.edulink.ui.screens.student

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
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.ui.components.RatingBar
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferDetailScreen(
    offer: Offer,
    onBack: () -> Unit
) {
    var showBookingDialog by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableStateOf<String?>(null) }
    var bookingConfirmed by remember { mutableStateOf(false) }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Surface
                )
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
                            text = offer.subject,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryContainer
                        ) {
                            Text(
                                text = "${offer.pricePerHour.toInt()} zł/h",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = OnPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RatingBar(rating = offer.rating, starSize = 20f)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${offer.rating} (${offer.reviewCount} opinii)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tutor info
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(name = offer.tutorName, size = 56)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = offer.tutorName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = offer.city,
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                        if (offer.isOnline) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Laptop, contentDescription = null, tint = Tertiary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Dostępne online",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Tertiary
                                )
                            }
                        }
                    }
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
                        text = offer.description,
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
                    offer.availableSlots.forEach { slot ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = slot,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = {
                                selectedSlot = slot
                                showBookingDialog = true
                            }) {
                                Text("Rezerwuj", color = Primary)
                            }
                        }
                        if (slot != offer.availableSlots.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Full-width booking button
            Button(
                onClick = { showBookingDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.BookOnline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Zarezerwuj lekcję", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Booking confirmation dialog
    if (showBookingDialog) {
        AlertDialog(
            onDismissRequest = { showBookingDialog = false },
            title = { Text("Potwierdź rezerwację") },
            text = {
                Column {
                    Text("Przedmiot: ${offer.subject}")
                    Text("Korepetytor: ${offer.tutorName}")
                    if (selectedSlot != null) {
                        Text("Termin: $selectedSlot")
                    }
                    Text("Cena: ${offer.pricePerHour.toInt()} zł/h")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBookingDialog = false
                        bookingConfirmed = true
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

    // Booking success snackbar-like dialog
    if (bookingConfirmed) {
        AlertDialog(
            onDismissRequest = { bookingConfirmed = false },
            title = { Text("Sukces!", color = Success) },
            text = { Text("Rezerwacja została złożona. Korepetytor otrzyma powiadomienie.") },
            confirmButton = {
                Button(
                    onClick = {
                        bookingConfirmed = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("OK")
                }
            }
        )
    }
}
