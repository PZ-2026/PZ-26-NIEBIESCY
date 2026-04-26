package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.components.SectionHeader
import com.vectorpeaks.edulink.ui.components.StatCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun TutorDashboardScreen(user: User, modifier: Modifier = Modifier) {
    val myOffers = FakeData.offers.filter { it.tutorId == user.id }
    val myReservations = FakeData.reservations.filter { it.tutorId == user.id }
    val pendingReservations = myReservations.filter { it.status == ReservationStatus.PENDING }
    val upcomingReservations = myReservations.filter { it.status == ReservationStatus.ACCEPTED }

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

        // Stats row
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

        // Pending reservations
        if (pendingReservations.isNotEmpty()) {
            SectionHeader(title = "Nowe rezerwacje")
            Spacer(modifier = Modifier.height(12.dp))
            pendingReservations.forEach { reservation ->
                ReservationCard(
                    reservation = reservation,
                    showActions = true,
                    onAccept = { /* TODO: accept */ },
                    onReject = { /* TODO: reject */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Upcoming lessons
        if (upcomingReservations.isNotEmpty()) {
            SectionHeader(title = "Nadchodzące zajęcia")
            Spacer(modifier = Modifier.height(12.dp))
            upcomingReservations.forEach { reservation ->
                ReservationCard(reservation = reservation, showActions = false)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (pendingReservations.isEmpty() && upcomingReservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventAvailable, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Brak nowych rezerwacji", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
    }
}
