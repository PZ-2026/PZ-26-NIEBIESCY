package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.ui.components.*
import com.vectorpeaks.edulink.ui.theme.*

@Composable
fun StudentSearchScreen(modifier: Modifier = Modifier) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var onlineOnly by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<Offer?>(null) }

    val filteredOffers = FakeData.offers.filter { offer ->
        val matchesSearch = searchQuery.isBlank() ||
                offer.tutorName.contains(searchQuery, ignoreCase = true) ||
                offer.subject.contains(searchQuery, ignoreCase = true)
        val matchesSubject = selectedSubject == null || offer.subject == selectedSubject
        val matchesCity = selectedCity == null || offer.city == selectedCity
        val matchesOnline = !onlineOnly || offer.isOnline
        matchesSearch && matchesSubject && matchesCity && matchesOnline
    }

    if (selectedOffer != null) {
        OfferDetailScreen(
            offer = selectedOffer!!,
            onBack = { selectedOffer = null }
        )
    } else {
        Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Znajdź korepetytora",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EduSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Szukaj korepetytora lub przedmiotu...",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                FilledIconButton(
                    onClick = { showFilters = !showFilters },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (showFilters) Primary else SurfaceVariant,
                        contentColor = if (showFilters) Surface else OnSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filtry")
                }
            }

            AnimatedVisibility(visible = showFilters) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    // Subject filter
                    Text("Przedmiot:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedSubject == null,
                            onClick = { selectedSubject = null },
                            label = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FakeData.offers.map { it.subject }.distinct().take(4).forEach { subject ->
                            FilterChip(
                                selected = selectedSubject == subject,
                                onClick = {
                                    selectedSubject = if (selectedSubject == subject) null else subject
                                },
                                label = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // City filter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Text("Miasto:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = selectedCity == null,
                            onClick = { selectedCity = null },
                            label = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                            shape = RoundedCornerShape(8.dp)
                        )
                        FakeData.offers.map { it.city }.distinct().forEach { city ->
                            FilterChip(
                                selected = selectedCity == city,
                                onClick = {
                                    selectedCity = if (selectedCity == city) null else city
                                },
                                label = { Text(city, style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Online filter
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Laptop, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tylko online", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = onlineOnly,
                            onCheckedChange = { onlineOnly = it }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Znaleziono: ${filteredOffers.size}",
                style = MaterialTheme.typography.labelMedium,
                color = OnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredOffers) { offer ->
                    OfferCard(
                        offer = offer,
                        onClick = { selectedOffer = offer }
                    )
                }
            }
        }
    }
}
