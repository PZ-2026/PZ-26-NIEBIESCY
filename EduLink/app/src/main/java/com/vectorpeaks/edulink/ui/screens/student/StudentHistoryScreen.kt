package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.ReservationStatus
import com.vectorpeaks.edulink.data.model.User
import com.vectorpeaks.edulink.ui.components.ClickableRatingBar
import com.vectorpeaks.edulink.ui.components.ReservationCard
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun StudentHistoryScreen(user: User, modifier: Modifier = Modifier) {
    val studentReservations = FakeData.reservations.filter { it.studentId == user.id }
    val statusFilters = listOf("Wszystkie", "Oczekujące", "Zaakceptowane", "Zakończone", "Odrzucone")
    var selectedFilter by remember { mutableIntStateOf(0) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var ratingReservationId by remember { mutableIntStateOf(-1) }
    var ratingValue by remember { mutableIntStateOf(0) }

    val filteredReservations = when (selectedFilter) {
        1 -> studentReservations.filter { it.status == ReservationStatus.PENDING }
        2 -> studentReservations.filter { it.status == ReservationStatus.ACCEPTED }
        3 -> studentReservations.filter { it.status == ReservationStatus.COMPLETED }
        4 -> studentReservations.filter { it.status == ReservationStatus.REJECTED }
        else -> studentReservations
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

        if (filteredReservations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Brak rezerwacji", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredReservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        showActions = false,
                        onRate = {
                            ratingReservationId = reservation.id
                            ratingValue = 0
                            showRatingDialog = true
                        }
                    )
                }
            }
        }
    }

    // Rating dialog
    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text("Oceń lekcję") },
            text = {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("Jak oceniasz tę lekcję?", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    ClickableRatingBar(
                        rating = ratingValue,
                        onRatingChange = { ratingValue = it }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRatingDialog = false },
                    enabled = ratingValue > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Zapisz ocenę")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}
