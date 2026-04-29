package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.BookingResponse
import com.vectorpeaks.edulink.data.model.Reservation
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.ui.components.ClickableRatingBar
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun StudentHistoryScreen(
    studentId: Int,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    var selectedFilter by remember { mutableIntStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingBooking by remember { mutableStateOf<BookingResponse?>(null) }
    var ratingValue by remember { mutableStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }

    LaunchedEffect(studentId) {
        viewModel.loadBookings(studentId)
    }

    val statusFilters =
        listOf("Wszystkie", "Oczekujące", "Zaakceptowane", "Zakończone", "Odrzucone")
    val filteredBookings = when (selectedFilter) {
        1 -> bookings.filter { it.status == "PENDING" }
        2 -> bookings.filter { it.status == "ACCEPTED" }
        3 -> bookings.filter { it.status == "COMPLETED" }
        4 -> bookings.filter { it.status == "REJECTED" }
        else -> bookings
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Historia rezerwacji",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedFilter,
            containerColor = Background,
            edgePadding = 0.dp,
            divider = {}
        ) {
            statusFilters.forEachIndexed { index, label ->
                Tab(
                    selected = selectedFilter == index,
                    onClick = { selectedFilter = index },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (selectedFilter == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selectedContentColor = Primary,
                    unselectedContentColor = OnSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

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

            filteredBookings.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Brak rezerwacji",
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredBookings) { booking ->
                        ReservationCard(
                            reservation = convertToReservation(booking),
                            showActions = false,
                            onClick = {
                                if (booking.status == "COMPLETED") {
                                    ratingBooking = booking
                                    ratingValue = booking.rating ?: 0
                                    showRatingDialog = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // Dialog oceny
    if (showRatingDialog && ratingBooking != null) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Oceń lekcję") },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Jak oceniasz lekcję ${ratingBooking!!.subject}?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    ClickableRatingBar(
                        rating = ratingValue,
                        onRatingChange = { ratingValue = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = ratingComment,
                        onValueChange = { ratingComment = it },
                        label = { Text("Komentarz (opcjonalny)") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ratingValue > 0) {
                            viewModel.addReview(
                                bookingId = ratingBooking!!.id.toLong(),
                                tutorId = ratingBooking!!.tutorId,
                                rating = ratingValue,
                                comment = ratingComment.takeIf { it.isNotBlank() }
                            ) {
                                viewModel.loadBookings(studentId)
                            }
                            showRatingDialog = false
                            ratingComment = ""
                        }
                    },
                    enabled = ratingValue > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Zapisz ocenę")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRatingDialog = false
                    ratingComment = ""
                }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

private fun convertToReservation(booking: BookingResponse): Reservation {
    return Reservation(
        id = booking.id,
        offerId = booking.offerId,
        studentId = 0,
        studentName = "",
        tutorId = 0,
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