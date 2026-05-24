package com.vectorpeaks.edulink.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.model.*
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.theme.*
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

// ==================== SEARCH BAR ====================

@Composable
fun EduSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Szukaj...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Szukaj") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

// ==================== RATING BAR ====================

@Composable
fun RatingBar(
    rating: Float,
    maxRating: Int = 5,
    modifier: Modifier = Modifier,
    starSize: Float = 18f
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxRating) {
            val icon = when {
                i <= rating.toInt() -> Icons.Default.Star
                i - 0.5f <= rating -> Icons.Default.StarHalf
                else -> Icons.Default.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Warning,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

@Composable
fun ClickableRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    maxRating: Int = 5,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..maxRating) {
            val icon = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder
            Icon(
                imageVector = icon,
                contentDescription = "Ocena $i",
                tint = Warning,
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChange(i) }
                    .padding(2.dp)
            )
        }
    }
}

// ==================== STATUS BADGE ====================

@Composable
fun StatusBadge(status: ReservationStatus, modifier: Modifier = Modifier) {
    val (text, backgroundColor, textColor) = when (status) {
        ReservationStatus.PENDING -> Triple("Oczekująca", WarningContainer, Color(0xFF9A6F00))
        ReservationStatus.ACCEPTED -> Triple("Zaakceptowana", SuccessContainer, Color(0xFF1B5E20))
        ReservationStatus.REJECTED -> Triple("Odrzucona", ErrorContainer, Color(0xFFB71C1C))
        ReservationStatus.COMPLETED -> Triple("Zakończona", PrimaryContainer, OnPrimaryContainer)
    }
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

// ==================== USER AVATAR ====================

@Composable
fun UserAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 40
) {
    val initials = name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(PrimaryContainer)
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                style = if (size > 48) MaterialTheme.typography.titleMedium
                else MaterialTheme.typography.labelMedium,
                color = OnPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                tint = OnPrimaryContainer,
                modifier = Modifier.size((size * 0.5f).dp)
            )
        }
    }
}

// ==================== OFFER CARD ====================

@Composable
fun OfferCard(
    offer: Offer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                overflow = TextOverflow.Ellipsis
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
        }
    }
}

// ==================== RESERVATION CARD ====================

@Composable
fun ReservationCard(
    reservation: Reservation,
    showActions: Boolean = false,
    onClick: (() -> Unit)? = null,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    onRate: (() -> Unit)? = null,
    onComplete: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
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
                    text = reservation.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = reservation.status)
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    name = if (showActions) reservation.studentName else reservation.tutorName,
                    size = 28
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (showActions) reservation.studentName else reservation.tutorName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${reservation.date}  •  ${reservation.time}",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = "${reservation.price.toInt()} zł",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (reservation.rating != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Ocena: ", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                    RatingBar(rating = reservation.rating.toFloat())
                }
            }

            if (showActions && reservation.status == ReservationStatus.PENDING) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onReject?.invoke() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Odrzuć")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onAccept?.invoke() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("Potwierdź")
                    }
                }
            }
            if (!showActions && reservation.status == ReservationStatus.ACCEPTED && onComplete != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = { onComplete.invoke() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Success),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zakończ lekcję", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

// ==================== USER CARD (Admin) ====================

@Composable
fun UserCard(
    user: User,
    onToggleBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(name = user.fullName, size = 48)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant
                )
                Text(
                    text = when (user.getRole()) {
                        RoleID.STUDENT -> "Uczeń"
                        RoleID.TUTOR -> "Korepetytor"
                        RoleID.ADMIN -> "Administrator"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = Primary
                )
            }
            if (user.getRole() != RoleID.ADMIN) {
                Button(
                    onClick = onToggleBlock,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (user.isBlocked) Success else Error
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (user.isBlocked) "Odblokuj" else "Zablokuj",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ==================== STAT CARD ====================

@Composable
fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = PrimaryContainer,
    textColor: Color = OnPrimaryContainer
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = textColor.copy(alpha = 0.7f)
            )
        }
    }
}

// ==================== SECTION HEADER ====================

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = OnBackground
        )
        action?.invoke()
    }
}

// ==================== OFFER CARD ====================

@Composable
fun OfferCard(
    offer: Offer,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onDetail: (() -> Unit)? = null
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = offer.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Primary)
                Text(text = "${offer.pricePerHour.toInt()} zł/h", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnBackground)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(name = offer.tutorName, size = 28)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = offer.tutorName, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = offer.description, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RatingBar(rating = offer.rating)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "(${offer.reviewCount})", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (offer.isOnline) {
                        SuggestionChip(onClick = {}, label = { Text("Online", style = MaterialTheme.typography.labelSmall) }, shape = RoundedCornerShape(8.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(text = offer.city, style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                }
            }

            if (onEdit != null || onDelete != null || onDetail != null) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (onDetail != null) {
                        OutlinedButton(
                            onClick = onDetail,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Szczegóły", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Szczegóły", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (onEdit != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onEdit,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edytuj", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edytuj", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    if (onDelete != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Usuń", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Usuń", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Usuń ofertę") },
            text = { Text("Czy na pewno chcesz usunąć ofertę z przedmiotu ${offer.subject}? Tej operacji nie można cofnąć.") },
            confirmButton = {
                Button(onClick = { showDeleteConfirm = false; onDelete?.invoke() }, colors = ButtonDefaults.buttonColors(containerColor = Error)) {
                    Text("Usuń")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Anuluj") } }
        )
    }
}
