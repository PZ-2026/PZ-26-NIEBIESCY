package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.Reservation
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.ui.components.ClickableRatingBar
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentHistoryScreen(
    studentId: Int,
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Define filters at the beginning as we need their size for PagerState
    val statusFilters = listOf("Wszystkie", "Oczekujące", "Zaakceptowane", "Zakończone", "Odrzucone")

    // PagerState manages the current page and swipe gestures
    val pagerState = rememberPagerState(pageCount = { statusFilters.size })
    val coroutineScope = rememberCoroutineScope()

    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingBooking by remember { mutableStateOf<BookingResponse?>(null) }
    var ratingValue by remember { mutableStateOf(0) }
    var ratingComment by remember { mutableStateOf("") }

    var showCompleteDialog by remember { mutableStateOf(false) }
    var completingBooking by remember { mutableStateOf<BookingResponse?>(null) }

    LaunchedEffect(studentId) {
        viewModel.loadBookings(studentId)
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

        // Tabs now track the pagerState.currentPage state
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Background,
            edgePadding = 0.dp,
            divider = {}
        ) {
            statusFilters.forEachIndexed { index, label ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        // Clicking a tab animates the transition to the corresponding page
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(
                            text = label,
                            fontWeight = if (pagerState.currentPage == index) FontWeight.SemiBold else FontWeight.Normal
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

            else -> {
                // HorizontalPager enables swiping content horizontally (left/right)
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->

                    // Filter the list locally for each page of the pager
                    val filteredBookings = when (pageIndex) {
                        1 -> bookings.filter { it.status == "PENDING" }
                        2 -> bookings.filter { it.status == "ACCEPTED" }
                        3 -> bookings.filter { it.status == "COMPLETED" }
                        4 -> bookings.filter { it.status == "REJECTED" }
                        else -> bookings
                    }

                    if (filteredBookings.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "Brak rezerwacji",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredBookings) { booking ->
                                ReservationCard(
                                    reservation = convertToReservation(booking),
                                    showActions = false,
                                    onClick = {
                                        if (booking.status == "COMPLETED") {
                                            ratingBooking = booking
                                            ratingValue = booking.rating ?: 0
                                            ratingComment = booking.reviewComment ?: ""
                                            showRatingDialog = true
                                        }
                                    },
                                    onComplete = if (booking.status == "ACCEPTED") {
                                        {
                                            completingBooking = booking
                                            showCompleteDialog = true
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- Dialogs (Rating and Complete) ---
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
                                bookingId = ratingBooking!!.id,
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

    if (showCompleteDialog && completingBooking != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Zakończ lekcję") },
            text = { Text("Czy na pewno chcesz oznaczyć lekcję ${completingBooking!!.subject} z ${completingBooking!!.tutorName} jako zakończoną?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeBooking(completingBooking!!.id) {
                            viewModel.loadBookings(studentId)
                        }
                        showCompleteDialog = false
                        completingBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Text("Zakończ")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCompleteDialog = false
                    completingBooking = null
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