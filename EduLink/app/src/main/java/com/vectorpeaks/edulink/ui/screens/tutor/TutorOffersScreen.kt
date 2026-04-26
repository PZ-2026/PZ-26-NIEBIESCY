package com.vectorpeaks.edulink.ui.screens.tutor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.FakeData
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.components.OfferCard
import com.vectorpeaks.edulink.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorOffersScreen(user: User, modifier: Modifier = Modifier) {
    val myOffers = FakeData.offers.filter { it.tutorId == user.id }
    var showAddDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var newPrice by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf(user.address) }
    var isOnline by remember { mutableStateOf(false) }

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

        if (myOffers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nie masz jeszcze ofert.\nDodaj swoją pierwszą ofertę!", style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(myOffers) { offer ->
                    OfferCard(offer = offer, onClick = { /* TODO: edit */ })
                }
            }
        }
    }

    // Add offer dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Nowa oferta") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Subject dropdown
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = newSubject,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Przedmiot") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            FakeData.subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        newSubject = subject
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text("Opis") },
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPrice,
                        onValueChange = { newPrice = it },
                        label = { Text("Cena za godzinę (zł)") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCity,
                        onValueChange = { newCity = it },
                        label = { Text("Miasto") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isOnline, onCheckedChange = { isOnline = it })
                        Text("Dostępne online")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAddDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = newSubject.isNotEmpty() && newDescription.isNotEmpty() && newPrice.isNotEmpty()
                ) {
                    Text("Dodaj ofertę")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Anuluj") }
            }
        )
    }
}
