package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.OfferCreateRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.OfferCard
import com.vectorpeaks.edulink.ui.components.RatingBar
import com.vectorpeaks.edulink.ui.theme.Error
import com.vectorpeaks.edulink.ui.theme.OnBackground
import com.vectorpeaks.edulink.ui.theme.OnPrimaryContainer
import com.vectorpeaks.edulink.ui.theme.OnSurfaceVariant
import com.vectorpeaks.edulink.ui.theme.Primary
import com.vectorpeaks.edulink.ui.theme.PrimaryContainer
import com.vectorpeaks.edulink.ui.theme.Success
import com.vectorpeaks.edulink.ui.theme.Surface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorOffersScreen(
    user: User,
    modifier: Modifier = Modifier,
    viewModel: TutorOffersViewModel = viewModel(),
    onNavigateToReviews: (tutorId: Int, tutorName: String) -> Unit = { _, _ -> }
) {
    val offers by viewModel.offers.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()
    val slotsByDay by viewModel.slotsByDay.collectAsState()
    val editSlotsByDay by viewModel.editSlotsByDay.collectAsState()
    val currentOfferSlots by viewModel.currentOfferSlots.collectAsState()

    val days = listOf(
        1 to "Poniedziałek", 2 to "Wtorek", 3 to "Środa",
        4 to "Czwartek", 5 to "Piątek", 6 to "Sobota", 0 to "Niedziela"
    )

    var selectedFilter by remember { mutableIntStateOf(0) }
    val statusFilters = listOf("Wszystkie", "Aktywne", "Oczekujące", "Odrzucone")
    val filteredOffers = when (selectedFilter) {
        1 -> offers.filter { it.status == "ACTIVE" }
        2 -> offers.filter { it.status == "PENDING" }
        3 -> offers.filter { it.status == "REJECTED" }
        else -> offers
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editingOffer by remember { mutableStateOf<Offer?>(null) }
    var detailOffer by remember { mutableStateOf<Offer?>(null) }

    var pendingAddRequest by remember { mutableStateOf<OfferCreateRequest?>(null) }
    var pendingEditRequest by remember { mutableStateOf<Pair<Int, OfferCreateRequest>?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var selectedDay by remember { mutableStateOf<Int?>(null) }
    var selectedSlotIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    var editSubjectId by remember { mutableStateOf<Int?>(null) }
    var editDescription by remember { mutableStateOf("") }
    var editPrice by remember { mutableStateOf("") }
    var editIsOnline by remember { mutableStateOf(false) }
    var editSelectedDay by remember { mutableStateOf<Int?>(null) }
    var editSelectedSlotIds by remember { mutableStateOf<List<Int>>(emptyList()) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(user.id) {
        viewModel.loadData(user.id)
    }

    Column(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Moje oferty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = OnBackground
            )
            FilledTonalButton(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = PrimaryContainer,
                    contentColor = OnPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dodaj")
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedFilter,
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
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
                        Button(onClick = { viewModel.loadData(user.id) }) {
                            Text("Spróbuj ponownie")
                        }
                    }
                }
            }
            filteredOffers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = when (selectedFilter) {
                            1 -> "Brak aktywnych ofert"
                            2 -> "Brak ofert oczekujących na akceptację"
                            3 -> "Brak odrzuconych ofert"
                            else -> "Nie masz jeszcze ofert.\nDodaj swoją pierwszą ofertę!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = OnSurfaceVariant
                    )
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredOffers) { offer ->
                        OfferCard(
                            offer = offer,
                            onClick = { },
                            onEdit = {
                                editingOffer = offer
                                editSubjectId = subjects.find { it.name == offer.subject }?.id
                                editDescription = offer.description
                                editPrice = offer.pricePerHour.toInt().toString()
                                editIsOnline = offer.isOnline
                                editSelectedDay = null
                                editSelectedSlotIds = offer.availableSlots.map { it.id }
                                viewModel.loadCurrentOfferSlots(offer)
                            },
                            onDelete = { viewModel.deleteOffer(offer.id, user.id) },
                            onDetail = { detailOffer = offer }
                        )
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
                    val (statusText, statusColor) = when (offer.status) {
                        "ACTIVE" -> "Aktywna" to Success
                        "PENDING" -> "Oczekuje na akceptację" to androidx.compose.ui.graphics.Color(0xFFF59E0B)
                        "REJECTED" -> "Odrzucona" to Error
                        else -> "Nieznany" to OnSurfaceVariant
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelLarge,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Cena", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text("${offer.pricePerHour.toInt()} zł/h", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Typ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                            Text(if (offer.isOnline) "Online" else "Stacjonarne", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
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
                        Text("${offer.rating} (${offer.reviewCount} opinii)", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }

                    HorizontalDivider()

                    Text("Terminy", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    if (offer.availableSlots.isEmpty()) {
                        Text("Brak przypisanych terminów", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    } else {
                        offer.availableSlots.forEach { slot ->
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
                                    Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = OnPrimaryContainer)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick = { onNavigateToReviews(offer.tutorId, offer.tutorName) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Pokaż opinie i recenzje")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { detailOffer = null }) {
                    Text("Zamknij")
                }
            }
        )
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nowa oferta") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    var subjectExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = !subjectExpanded }) {
                        OutlinedTextField(
                            value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Przedmiot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(text = { Text(subject.name) }, onClick = { selectedSubjectId = subject.id; subjectExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Opis") }, minLines = 3, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Cena za godzinę (zł)") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                    var typeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = !typeExpanded }) {
                        OutlinedTextField(
                            value = if (isOnline) "Online" else "Stacjonarne",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Typ zajęć") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                            listOf("Stacjonarne", "Online").forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = { isOnline = (type == "Online"); typeExpanded = false })
                            }
                        }
                    }

                    var dayExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = !dayExpanded }) {
                        OutlinedTextField(
                            value = days.find { it.first == selectedDay }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dzień tygodnia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                            days.forEach { (num, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { selectedDay = num; selectedSlotIds = emptyList(); dayExpanded = false; viewModel.loadSlotsByDay(num, user.id) })
                            }
                        }
                    }

                    if (selectedDay != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Dostępne godziny:", style = MaterialTheme.typography.labelLarge, color = OnBackground)
                        if (slotsByDay.isEmpty()) {
                            Text("Brak dostępnych terminów w tym dniu", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        } else {
                            slotsByDay.forEach { slot ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = slot.id in selectedSlotIds,
                                        onCheckedChange = { checked -> selectedSlotIds = if (checked) selectedSlotIds + slot.id else selectedSlotIds - slot.id }
                                    )
                                    Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val subjectId = selectedSubjectId
                        val priceValue = price.toDoubleOrNull()
                        if (subjectId != null && description.isNotBlank() && priceValue != null && priceValue > 0) {
                            pendingAddRequest = OfferCreateRequest(
                                tutorId = user.id,
                                subjectId = subjectId,
                                details = description,
                                price = priceValue,
                                offerType = if (isOnline) "Online" else "Offline",
                                availabilitySlotIds = selectedSlotIds
                            )
                        }
                    },
                    enabled = selectedSubjectId != null && description.isNotBlank() && price.toDoubleOrNull() != null && !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Dodaj ofertę")
                }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Anuluj") } }
        )
    }

    editingOffer?.let { offer ->
        AlertDialog(
            onDismissRequest = { editingOffer = null; viewModel.clearEditSlots() },
            title = { Text("Edytuj ofertę") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    var editSubjectExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = editSubjectExpanded, onExpandedChange = { editSubjectExpanded = !editSubjectExpanded }) {
                        OutlinedTextField(
                            value = subjects.find { it.id == editSubjectId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Przedmiot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editSubjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = editSubjectExpanded, onDismissRequest = { editSubjectExpanded = false }) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(text = { Text(subject.name) }, onClick = { editSubjectId = subject.id; editSubjectExpanded = false })
                            }
                        }
                    }

                    OutlinedTextField(value = editDescription, onValueChange = { editDescription = it }, label = { Text("Opis") }, minLines = 3, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPrice, onValueChange = { editPrice = it }, label = { Text("Cena za godzinę (zł)") }, singleLine = true, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())

                    var editTypeExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = editTypeExpanded, onExpandedChange = { editTypeExpanded = !editTypeExpanded }) {
                        OutlinedTextField(
                            value = if (editIsOnline) "Online" else "Stacjonarne",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Typ zajęć") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editTypeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = editTypeExpanded, onDismissRequest = { editTypeExpanded = false }) {
                            listOf("Stacjonarne", "Online").forEach { type ->
                                DropdownMenuItem(text = { Text(type) }, onClick = { editIsOnline = (type == "Online"); editTypeExpanded = false })
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Aktualne terminy:", style = MaterialTheme.typography.labelLarge, color = OnBackground)
                    if (currentOfferSlots.isEmpty()) {
                        Text("Brak przypisanych terminów", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    } else {
                        currentOfferSlots.forEach { slot ->
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = slot.id in editSelectedSlotIds,
                                    onCheckedChange = { checked -> editSelectedSlotIds = if (checked) editSelectedSlotIds + slot.id else editSelectedSlotIds - slot.id }
                                )
                                Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Dodaj nowe terminy:", style = MaterialTheme.typography.labelLarge, color = OnBackground)

                    var editDayExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = editDayExpanded, onExpandedChange = { editDayExpanded = !editDayExpanded }) {
                        OutlinedTextField(
                            value = days.find { it.first == editSelectedDay }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Dzień tygodnia") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editDayExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = editDayExpanded, onDismissRequest = { editDayExpanded = false }) {
                            days.forEach { (num, name) ->
                                DropdownMenuItem(text = { Text(name) }, onClick = { editSelectedDay = num; editDayExpanded = false; viewModel.loadEditSlotsByDay(num, user.id, offer.id) })
                            }
                        }
                    }

                    if (editSelectedDay != null) {
                        if (editSlotsByDay.isEmpty()) {
                            Text("Brak dostępnych terminów w tym dniu", color = OnSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                        } else {
                            editSlotsByDay.filter { it.id !in currentOfferSlots.map { s -> s.id } }.forEach { slot ->
                                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = slot.id in editSelectedSlotIds,
                                        onCheckedChange = { checked -> editSelectedSlotIds = if (checked) editSelectedSlotIds + slot.id else editSelectedSlotIds - slot.id }
                                    )
                                    Text(slot.label, style = MaterialTheme.typography.bodyMedium, color = OnBackground)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val subjectId = editSubjectId
                        val priceValue = editPrice.toDoubleOrNull()
                        if (subjectId != null && editDescription.isNotBlank() && priceValue != null && priceValue > 0) {
                            pendingEditRequest = Pair(
                                offer.id,
                                OfferCreateRequest(
                                    tutorId = user.id,
                                    subjectId = subjectId,
                                    details = editDescription,
                                    price = priceValue,
                                    offerType = if (editIsOnline) "Online" else "Offline",
                                    availabilitySlotIds = editSelectedSlotIds
                                )
                            )
                        }
                    },
                    enabled = editSubjectId != null && editDescription.isNotBlank() && editPrice.toDoubleOrNull() != null && !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isCreating) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    else Text("Zapisz zmiany")
                }
            },
            dismissButton = { TextButton(onClick = { editingOffer = null; viewModel.clearEditSlots() }) { Text("Anuluj") } }
        )
    }

    pendingAddRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { pendingAddRequest = null },
            title = { Text("Wyślij do administratora") },
            text = { Text("Czy na pewno chcesz wysłać ofertę do zaakceptowania przez administratora?") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingAddRequest = null
                        scope.launch {
                            viewModel.createOffer(request) {
                                showAddDialog = false
                                selectedSubjectId = null
                                description = ""
                                price = ""
                                isOnline = false
                                selectedDay = null
                                selectedSlotIds = emptyList()
                                showSuccessDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Tak, wyślij") }
            },
            dismissButton = { TextButton(onClick = { pendingAddRequest = null }) { Text("Nie") } }
        )
    }

    pendingEditRequest?.let { (offerId, request) ->
        AlertDialog(
            onDismissRequest = { pendingEditRequest = null },
            title = { Text("Wyślij do administratora") },
            text = { Text("Czy na pewno chcesz wysłać ofertę do zaakceptowania przez administratora?") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingEditRequest = null
                        scope.launch {
                            viewModel.updateOffer(offerId, request) {
                                editingOffer = null
                                editSelectedDay = null
                                editSelectedSlotIds = emptyList()
                                viewModel.clearEditSlots()
                                showSuccessDialog = true
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) { Text("Tak, wyślij") }
            },
            dismissButton = { TextButton(onClick = { pendingEditRequest = null }) { Text("Nie") } }
        )
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            title = { Text("Gotowe!", color = Success) },
            text = { Text("Oferta została wysłana do administratora w celu jej potwierdzenia.") },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Success)) {
                    Text("OK")
                }
            }
        )
    }
}