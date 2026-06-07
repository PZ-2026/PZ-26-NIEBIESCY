package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.Reservation
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.theme.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TutorReservationsScreen(
    tutorId: Int,
    modifier: Modifier = Modifier,
    viewModel: TutorReservationsViewModel = viewModel(),
    onOpenChat: (studentId: Int) -> Unit = {},
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(tutorId) {
        viewModel.loadBookings(tutorId)
    }

    val statusFilters = listOf("Wszystkie", "Oczekujące", "Zaakceptowane", "Zakończone", "Odrzucone")
    // Pager state to control the horizontal swipe gesture and active tab index
    val pagerState = rememberPagerState(pageCount = { statusFilters.size })
    val coroutineScope = rememberCoroutineScope()
    var showCompleteDialog by remember { mutableStateOf(false) }
    var completingBooking by remember { mutableStateOf<BookingResponse?>(null) }


    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Rezerwacje",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->

                    val pageFilteredBookings = when (pageIndex) {
                        1 -> bookings.filter { it.status == "PENDING" }
                        2 -> bookings.filter { it.status == "ACCEPTED" }
                        3 -> bookings.filter { it.status == "COMPLETED" }
                        4 -> bookings.filter { it.status == "REJECTED" }
                        else -> bookings
                    }

                    if (pageFilteredBookings.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
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
                            modifier = Modifier.fillMaxSize() // Ważne, aby LazyColumn wypełniał stronę
                        ) {
                            items(pageFilteredBookings) { booking ->
                                ReservationCard(
                                    reservation = convertToReservation(booking),
                                    showActions = booking.status == "PENDING",
                                    isTutorView = true,
                                    onAccept = {
                                        viewModel.updateStatus(booking.id, "ACCEPTED", tutorId)
                                    },
                                    onReject = {
                                        viewModel.updateStatus(booking.id, "REJECTED", tutorId)
                                    },
                                    onComplete = if (booking.status == "ACCEPTED") {
                                        {
                                            completingBooking = booking
                                            showCompleteDialog = true
                                        }
                                    } else null,
                                    onChat = if (booking.status == "ACCEPTED") {
                                        { onOpenChat(booking.studentId) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showCompleteDialog && completingBooking != null) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Zakończ lekcję") },
            text = {
                Text(
                    "Czy na pewno chcesz oznaczyć lekcję " +
                            "${completingBooking!!.subject} ze studentem " +
                            "${completingBooking!!.studentName } jako zakończoną?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateStatus(
                            completingBooking!!.id,
                            "COMPLETED",
                            tutorId
                        )
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
        studentId = booking.studentId,
        studentName = booking.studentName ?: "",
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