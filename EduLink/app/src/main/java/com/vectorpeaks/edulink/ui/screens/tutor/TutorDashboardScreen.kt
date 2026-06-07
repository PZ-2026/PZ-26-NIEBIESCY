package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.Reservation
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.components.SectionHeader
import com.vectorpeaks.edulink.ui.components.StatCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun TutorDashboardScreen(
    user: User,
    modifier: Modifier = Modifier,
    viewModel: TutorDashboardViewModel = viewModel()
) {
    val bookings by viewModel.bookings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(user.id) {
        viewModel.loadBookings(user.id)
    }

    val reservations = remember(bookings) {
        bookings.map { convertToReservation(it) }
    }

    val pendingReservations = reservations.filter { it.status == ReservationStatus.PENDING }
    val upcomingReservations = reservations.filter { it.status == ReservationStatus.ACCEPTED }


    val myOffers = FakeData.offers.filter { it.tutorId == user.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Witaj, ${user.firstName}!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Text(
            text = "Twój panel korepetytora",
            style = MaterialTheme.typography.bodyMedium,
            color = OnSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Box(modifier = Modifier.fillMaxSize().height(300.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Błąd: $error", color = Error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadBookings(user.id) }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }
            else -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Aktywne oferty",
                        value = "${myOffers.size}",
                        modifier = Modifier.weight(1f),
                        backgroundColor = PrimaryContainer,
                        textColor = OnPrimaryContainer
                    )
                    StatCard(
                        title = "Oczekujące",
                        value = "${pendingReservations.size}",
                        modifier = Modifier.weight(1f),
                        backgroundColor = WarningContainer,
                        textColor = OnBackground
                    )
                    StatCard(
                        title = "Nadchodzące",
                        value = "${upcomingReservations.size}",
                        modifier = Modifier.weight(1f),
                        backgroundColor = SuccessContainer,
                        textColor = OnBackground
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))

                if (pendingReservations.isNotEmpty()) {
                    SectionHeader(title = "Nowe rezerwacje")
                    Spacer(modifier = Modifier.height(12.dp))
                    pendingReservations.forEach { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            showActions = true,
                            isTutorView = true,
                            onAccept = {
                                viewModel.updateStatus(reservation.id, "ACCEPTED", user.id)
                            },
                            onReject = {
                                viewModel.updateStatus(reservation.id, "REJECTED", user.id)
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (upcomingReservations.isNotEmpty()) {
                    SectionHeader(title = "Nadchodzące zajęcia")
                    Spacer(modifier = Modifier.height(12.dp))
                    upcomingReservations.forEach { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            showActions = false,
                            isTutorView = true,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (pendingReservations.isEmpty() && upcomingReservations.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventAvailable,
                                contentDescription = null,
                                tint = OnSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Brak nowych rezerwacji",
                                style = MaterialTheme.typography.bodyLarge,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
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