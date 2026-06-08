package com.vectorpeaks.edulink.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vectorpeaks.edulink.data.model.MaintenanceStatus
import com.vectorpeaks.edulink.network.RetrofitClient
import com.vectorpeaks.edulink.ui.theme.*
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Wraps screen content with top banners for maintenance mode and global admin messages.
 * Polls the backend every 30 seconds for status updates.
 *
 * @param content The screen content to wrap below the banners.
 */
@Composable
fun MaintenanceBannerWrapper(
    content: @Composable () -> Unit
) {
    var maintenanceStatus by remember { mutableStateOf(MaintenanceStatus()) }
    var globalMessage by remember { mutableStateOf("") }
    var remainingSeconds by remember { mutableLongStateOf(0L) }

    // Poll maintenance status and global message every 30 seconds
    LaunchedEffect(Unit) {
        while (true) {
            try {
                maintenanceStatus = RetrofitClient.apiService.getMaintenanceStatus()
            } catch (_: Exception) {
                // Ignore network errors – banner just stays hidden
            }
            try {
                globalMessage = RetrofitClient.apiService.getGlobalMessage().message
            } catch (_: Exception) {
                // Ignore network errors – banner just stays hidden
            }
            delay(30_000)
        }
    }

    // Countdown timer – ticks every second when maintenance is scheduled
    LaunchedEffect(maintenanceStatus) {
        if (maintenanceStatus.active && maintenanceStatus.startsAt != null) {
            while (true) {
                try {
                    val startsAt = LocalDateTime.parse(maintenanceStatus.startsAt)
                    val now = LocalDateTime.now()
                    val diff = Duration.between(now, startsAt)
                    remainingSeconds = if (diff.isNegative) 0L else diff.seconds
                } catch (_: Exception) {
                    remainingSeconds = 0L
                }
                delay(1_000)
            }
        } else {
            remainingSeconds = 0L
        }
    }

    val showMaintenanceBanner = maintenanceStatus.active
    val showGlobalMessageBanner = globalMessage.isNotBlank()

    Column(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = showGlobalMessageBanner,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Primary)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = globalMessage,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        AnimatedVisibility(
            visible = showMaintenanceBanner,
            enter = slideInVertically { -it },
            exit = slideOutVertically { -it }
        ) {
            val isFullyActive = maintenanceStatus.fullyActive || remainingSeconds <= 0L

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isFullyActive) Error else Warning)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                if (isFullyActive) {
                    Text(
                        text = "Trwają prace serwisowe",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    val minutes = remainingSeconds / 60
                    val seconds = remainingSeconds % 60
                    Text(
                        text = "Prace serwisowe za ${"%02d:%02d".format(minutes, seconds)}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            content()
        }
    }
}
