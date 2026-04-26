package com.vectorpeaks.edulink.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.components.SectionHeader
import com.vectorpeaks.edulink.ui.components.StatCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun AdminDashboardScreen(user: User, modifier: Modifier = Modifier) {
    val totalUsers = FakeData.users.size
    val totalOffers = FakeData.offers.size
    val totalReservations = FakeData.reservations.size
    val pendingReservations = FakeData.reservations.filter { it.status == ReservationStatus.PENDING }
    val tutorsCount = FakeData.users.count { it.getRole() == RoleID.TUTOR }
    val studentsCount = FakeData.users.count { it.getRole() == RoleID.STUDENT }

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

        // Stats grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Użytkownicy",
                value = "$totalUsers",
                modifier = Modifier.weight(1f),
                backgroundColor = PrimaryContainer,
                textColor = OnPrimaryContainer
            )
            StatCard(
                title = "Oferty",
                value = "$totalOffers",
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
                value = "$tutorsCount",
                modifier = Modifier.weight(1f),
                backgroundColor = TertiaryContainer,
                textColor = OnTertiaryContainer
            )
            StatCard(
                title = "Uczniowie",
                value = "$studentsCount",
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
                value = "$totalReservations",
                modifier = Modifier.weight(1f),
                backgroundColor = WarningContainer,
                textColor = OnBackground
            )
            StatCard(
                title = "Oczekujące",
                value = "${pendingReservations.size}",
                modifier = Modifier.weight(1f),
                backgroundColor = ErrorContainer,
                textColor = OnBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Pending approvals
        if (pendingReservations.isNotEmpty()) {
            SectionHeader(title = "Oczekujące zgłoszenia")
            Spacer(modifier = Modifier.height(12.dp))
            pendingReservations.forEach { reservation ->
                ReservationCard(
                    reservation = reservation,
                    showActions = true,
                    onAccept = { /* TODO */ },
                    onReject = { /* TODO */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Quick actions
        Spacer(modifier = Modifier.height(16.dp))
        SectionHeader(title = "Szybkie akcje")
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
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
        Spacer(modifier = Modifier.height(32.dp))
    }
}
