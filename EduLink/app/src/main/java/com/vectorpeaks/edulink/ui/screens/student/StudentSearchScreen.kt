package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.ui.screens.student.OffersViewModel

@Composable
fun StudentSearchScreen(modifier: Modifier = Modifier) {
    val viewModel: OffersViewModel = viewModel()
    val offers by viewModel.offers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val dataViewModel: DataViewModel = viewModel()
    val subjects by dataViewModel.subjects.collectAsState()
    val cities by dataViewModel.cities.collectAsState()

    LaunchedEffect(Unit) {
        dataViewModel.loadSubjects()
        dataViewModel.loadCities()
    }

    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    var selectedCity by remember { mutableStateOf<String?>(null) }
    var onlineOnly by remember { mutableStateOf(false) }
    var selectedOffer by remember { mutableStateOf<Offer?>(null) }

    LaunchedEffect(searchQuery, selectedSubject, selectedCity, onlineOnly) {
        viewModel.loadOffers(
            subject = selectedSubject,
            city = selectedCity,
            onlineOnly = onlineOnly,
            search = searchQuery.takeIf { it.isNotBlank() }
        )
    }

    if (selectedOffer != null) {
        OfferDetailScreen(offer = selectedOffer!!, onBack = { selectedOffer = null })
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSubject == null,
                                onClick = { selectedSubject = null },
                                label = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        val subjectsList = if (subjects.isNotEmpty()) subjects else listOf("Matematyka", "Fizyka")
                        items(subjectsList) { subject ->
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
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedCity == null,
                                onClick = { selectedCity = null },
                                label = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        val citiesList = if (cities.isNotEmpty()) cities else listOf("Rzeszów", "Kraków")
                        items(citiesList) { city ->
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

            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Text("Błąd: $error", color = Error, modifier = Modifier.padding(16.dp))
                }
                else -> {
                    Text(
                        text = "Znaleziono: ${offers.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(offers) { offer ->
                            OfferCard(offer = offer, onClick = { selectedOffer = offer })
                        }
                    }
                }
            }
        }
    }
}
