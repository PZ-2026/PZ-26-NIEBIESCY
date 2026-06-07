package com.vectorpeaks.edulink.ui.screens.admin

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.ui.components.RatingBar
import com.vectorpeaks.edulink.ui.components.UserAvatar
import com.vectorpeaks.edulink.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdminOffersScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminOffersViewModel = viewModel()
) {
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val tabs = listOf("Oczekujące", "Zaakceptowane", "Odrzucone")
    val pagerState = rememberPagerState(pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    var detailOffer by remember { mutableStateOf<Offer?>(null) }
    var confirmRejectOffer by remember { mutableStateOf<Offer?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadOffers()
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Zarządzanie ofertami",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            edgePadding = 0.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Błąd: $error", color = Error)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadOffers() }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }
            else -> {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { pageIndex ->
                    val filtered = when (pageIndex) {
                        0 -> offers.filter { it.status == "PENDING" }
                        1 -> offers.filter { it.status == "ACTIVE" || it.status == "ACCEPTED" }
                        2 -> offers.filter { it.status == "REJECTED" }
                        else -> offers
                    }

                    if (filtered.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = when (pageIndex) {
                                    0 -> "Brak oczekujących ofert"
                                    1 -> "Brak zaakceptowanych ofert"
                                    2 -> "Brak odrzuconych ofert"
                                    else -> "Brak ofert"
                                },
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
                            items(filtered) { offer ->
                                AdminOfferCard(
                                    offer = offer,
                                    pageIndex = pageIndex,
                                    onDetail = { detailOffer = offer },
                                    onApprove = { viewModel.approveOffer(offer.id) },
                                    onReject = { confirmRejectOffer = offer }
                                )
                            }
                        }
                    }
                }
            }
        }
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
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider()
                    Text("Korepetytor", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    Text(
                        offer.tutorName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cena", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(
                                "${offer.pricePerHour.toInt()} zł/h",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Typ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(
                                if (offer.isOnline) "Online" else "Stacjonarne",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
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
                        Text(
                            "${offer.rating} (${offer.reviewCount} opinii)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                    Text("Terminy", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    if (offer.availableSlots.isNullOrEmpty()) {
                        Text(
                            "Brak przypisanych terminów",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
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
                                    Icon(
                                        Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        slot.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = OnPrimaryContainer
                                    )
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

    confirmRejectOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { confirmRejectOffer = null },
            title = { Text("Odrzuć ofertę") },
            text = {
                Text("Czy na pewno chcesz odrzucić ofertę \"${offer.subject}\" korepetytora ${offer.tutorName}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.rejectOffer(offer.id)
                        confirmRejectOffer = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Error)
                ) { Text("Odrzuć") }
            },
            dismissButton = {
                TextButton(onClick = { confirmRejectOffer = null }) { Text("Anuluj") }
            }
        )
    }
}

@Composable
private fun AdminOfferCard(
    offer: Offer,
    pageIndex: Int,
    onDetail: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
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
                Text(
                    text = "${offer.pricePerHour.toInt()} zł/h",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnBackground
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = offer.tutorName, size = 28)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = offer.tutorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = offer.description,
                style = MaterialTheme.typography.bodySmall,
                color = OnSurfaceVariant,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = offer.rating)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(${offer.reviewCount})",
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (offer.isOnline) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Online", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = offer.city,
                        style = MaterialTheme.typography.labelSmall,
                        color = OnSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDetail,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Szczegóły", style = MaterialTheme.typography.labelMedium)
                }

                when (pageIndex) {
                    0 -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Odrzuć", style = MaterialTheme.typography.labelMedium) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Zatwierdź", style = MaterialTheme.typography.labelMedium) }
                    }
                    1 -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onReject,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Odrzuć", style = MaterialTheme.typography.labelMedium) }
                    }
                    2 -> {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onApprove,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Success),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) { Text("Zatwierdź", style = MaterialTheme.typography.labelMedium) }
                    }
                }
            }
        }
    }
}