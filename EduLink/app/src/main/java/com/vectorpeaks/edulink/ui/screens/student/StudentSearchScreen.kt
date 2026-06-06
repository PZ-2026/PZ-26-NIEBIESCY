package com.vectorpeaks.edulink.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.user.DataViewModel
import com.vectorpeaks.edulink.data.model.user.OffersViewModel
import com.vectorpeaks.edulink.ui.components.*
import com.vectorpeaks.edulink.ui.theme.*
import androidx.compose.runtime.saveable.rememberSaveable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSearchScreen(
    studentId: Int,
    modifier: Modifier = Modifier,
    onNavigateToOfferDetail: (Int) -> Unit,
    onNavigateToReviews: (tutorId: Int, tutorName: String) -> Unit,
    offersViewModel: OffersViewModel  // received from StudentMainScreen, not created here
) {
    // --- State from ViewModel ---
    val offers        by offersViewModel.offers.collectAsState()
    val isLoading     by offersViewModel.isLoading.collectAsState()
    val isLoadingMore by offersViewModel.isLoadingMore.collectAsState()
    val isRefreshing  by offersViewModel.isRefreshing.collectAsState()
    val error         by offersViewModel.error.collectAsState()
    val totalElements by offersViewModel.totalElements.collectAsState()

    // --- Data for filter dropdowns ---
    val dataViewModel: DataViewModel = viewModel()
    val subjects by dataViewModel.subjects.collectAsState()
    val cities   by dataViewModel.cities.collectAsState()

    LaunchedEffect(Unit) {
        dataViewModel.loadSubjects()
        dataViewModel.loadCities()
    }

    // --- Local UI state (filters) ---
    var searchQuery     by rememberSaveable { mutableStateOf("") }
    var showFilters     by rememberSaveable { mutableStateOf(false) }
    var selectedSubject by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedCity    by rememberSaveable { mutableStateOf<String?>(null) }
    var onlineOnly      by rememberSaveable { mutableStateOf(false) }

    // Sort: "createdAt" = newest, "rating" = best rated
    var sortBy by rememberSaveable { mutableStateOf("createdAt") }

    // --- List state for scroll-to-bottom detection ---
    val listState = rememberLazyListState()

    // Reload first page whenever filters or sort change
    LaunchedEffect(searchQuery, selectedSubject, selectedCity, onlineOnly, sortBy) {
        offersViewModel.loadOffers(
            subject    = selectedSubject,
            city       = selectedCity,
            onlineOnly = onlineOnly,
            search     = searchQuery.takeIf { it.isNotBlank() },
            sortBy     = sortBy,
            sortDir    = "desc"
        )
    }

    // Trigger next page load when user scrolls near the bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 3
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) offersViewModel.loadNextPage()
    }

    // Show "Usuń filtry" when any filter or non-default sort is active
    val hasActiveFilters = selectedSubject != null
            || selectedCity != null
            || onlineOnly
            || searchQuery.isNotBlank()
            || sortBy != "createdAt"

    // --- Main list screen ---
    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text       = "Znajdź korepetytora",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = OnBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        // Search bar + filter toggle button
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EduSearchBar(
                query         = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder   = "Szukaj korepetytora lub przedmiotu...",
                modifier      = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            FilledIconButton(
                onClick = { showFilters = !showFilters },
                colors  = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (showFilters) Primary else SurfaceVariant,
                    contentColor   = if (showFilters) Surface else OnSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.FilterList, contentDescription = "Filtry")
            }
        }

        // Expandable filter + sort panel
        AnimatedVisibility(visible = showFilters) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {

                // Sort chips
                Text("Sortowanie:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = sortBy == "createdAt",
                            onClick  = { sortBy = "createdAt" },
                            label    = { Text("Najnowsze", style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                    item {
                        FilterChip(
                            selected = sortBy == "rating",
                            onClick  = { sortBy = "rating" },
                            label    = { Text("Najlepiej oceniane", style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Clear all filters + reset sort
                if (hasActiveFilters) {
                    AssistChip(
                        onClick = {
                            selectedSubject = null
                            selectedCity    = null
                            onlineOnly      = false
                            searchQuery     = ""
                            sortBy          = "createdAt"
                        },
                        label = { Text("Usuń filtry", style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Usuń filtry",
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Subject filter
                Text("Przedmiot:", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedSubject == null,
                            onClick  = { selectedSubject = null },
                            label    = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                    val subjectsList = if (subjects.isNotEmpty()) subjects else listOf("Matematyka", "Fizyka")
                    items(subjectsList) { subject ->
                        FilterChip(
                            selected = selectedSubject == subject,
                            onClick  = { selectedSubject = if (selectedSubject == subject) null else subject },
                            label    = { Text(subject, style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // City filter
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
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
                            onClick  = { selectedCity = null },
                            label    = { Text("Wszystkie", style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                    val citiesList = if (cities.isNotEmpty()) cities else listOf("Rzeszów", "Kraków")
                    items(citiesList) { city ->
                        FilterChip(
                            selected = selectedCity == city,
                            onClick  = { selectedCity = if (selectedCity == city) null else city },
                            label    = { Text(city, style = MaterialTheme.typography.labelSmall) },
                            shape    = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Online only toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Laptop, contentDescription = null, tint = OnSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tylko online", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked         = onlineOnly,
                        onCheckedChange = { onlineOnly = it }
                    )
                }
            }
        }

        // Content area
        when {
            isLoading -> {
                // Full-screen spinner on first page load
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            error != null -> {
                Text("Błąd: $error", color = Error, modifier = Modifier.padding(16.dp))
            }
            else -> {
                // Total count from backend — stays accurate across pages
                Text(
                    text     = "Znaleziono: $totalElements",
                    style    = MaterialTheme.typography.labelMedium,
                    color    = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh    = { offersViewModel.refresh() },
                    modifier     = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state               = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding      = PaddingValues(bottom = 16.dp)
                    ) {
                        items(
                            items = offers,
                            key   = { offer -> offer.id } // stable keys prevent list jumping
                        ) { offer ->
                            OfferCard(
                                offer   = offer,
                                onClick = { onNavigateToOfferDetail(offer.id) }
                            )
                        }
                        // Spinner at bottom while loading next page
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier          = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment  = Alignment.Center
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}