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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vectorpeaks.edulink.data.model.OfferCreateRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.OfferCard
import com.vectorpeaks.edulink.ui.theme.Error
import com.vectorpeaks.edulink.ui.theme.OnBackground
import com.vectorpeaks.edulink.ui.theme.OnPrimaryContainer
import com.vectorpeaks.edulink.ui.theme.OnSurfaceVariant
import com.vectorpeaks.edulink.ui.theme.Primary
import com.vectorpeaks.edulink.ui.theme.PrimaryContainer
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorOffersScreen(
    user: User,
    modifier: Modifier = Modifier,
    viewModel: TutorOffersViewModel = viewModel()
) {
    val offers by viewModel.offers.collectAsState()
    val slots by viewModel.slots.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isCreating by viewModel.isCreating.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    var selectedSubjectId by remember { mutableStateOf<Int?>(null) }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var selectedSlotId by remember { mutableStateOf<Int?>(null) }

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
            offers.isEmpty() -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nie masz jeszcze ofert.\nDodaj swoją pierwszą ofertę!", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(offers) { offer ->
                        OfferCard(offer = offer, onClick = { /* edycja – opcjonalnie */ })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nowa oferta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    var subjectExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = !subjectExpanded }
                    ) {
                        OutlinedTextField(
                            value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Przedmiot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        selectedSubjectId = subject.id
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Opis") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        label = { Text("Cena za godzinę (zł)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    var typeExpanded by remember { mutableStateOf(false) }
                    val typeOptions = listOf("Stacjonarne", "Online")
                    val selectedType = if (isOnline) "Online" else "Stacjonarne"
                    ExposedDropdownMenuBox(
                        expanded = typeExpanded,
                        onExpandedChange = { typeExpanded = !typeExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Typ zajęć") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = typeExpanded,
                            onDismissRequest = { typeExpanded = false }
                        ) {
                            typeOptions.forEach { type ->
                                DropdownMenuItem(
                                    text = { Text(type) },
                                    onClick = {
                                        isOnline = (type == "Online")
                                        typeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var slotExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = slotExpanded,
                        onExpandedChange = { slotExpanded = !slotExpanded }
                    ) {
                        OutlinedTextField(
                            value = slots.find { it.id == selectedSlotId }?.label ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Preferowany termin (opcjonalnie)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = slotExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = slotExpanded,
                            onDismissRequest = { slotExpanded = false }
                        ) {
                            slots.forEach { slot ->
                                DropdownMenuItem(
                                    text = { Text(slot.label) },
                                    onClick = {
                                        selectedSlotId = slot.id
                                        slotExpanded = false
                                    }
                                )
                            }
                            if (slots.isEmpty()) {
                                DropdownMenuItem(text = { Text("Brak dostępnych terminów") }, onClick = {})
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
                            val request = OfferCreateRequest(
                                tutorId = user.id,
                                subjectId = subjectId,
                                details = description,
                                price = priceValue,
                                offerType = if (isOnline) "Online" else "Offline",
                                availabilitySlotId = selectedSlotId
                            )
                            scope.launch {
                                viewModel.createOffer(request) {
                                    showAddDialog = false
                                    selectedSubjectId = null
                                    description = ""
                                    price = ""
                                    isOnline = false
                                    selectedSlotId = null
                                }
                            }
                        }
                    },
                    enabled = selectedSubjectId != null && description.isNotBlank() && price.toDoubleOrNull() != null && !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Dodaj ofertę")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Anuluj") }
            }
        )
    }
}