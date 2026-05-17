package com.vectorpeaks.edulink.utils

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

object DateUtils {
    // Backend przesyła format ISO, np. 2023-09-20T12:05:00
    // Jeśli w stringu brakuje "Z" na końcu, używamy LocalDateTime

    fun formatChatTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return ""
        return try {
            val date = LocalDateTime.parse(timestamp).toLocalDate()
            val today = LocalDate.now()

            when {
                date == today -> "dzisiaj"
                date == today.minusDays(1) -> "wczoraj"
                else -> date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            }
        } catch (e: Exception) {
            "brak daty"
        }
    }

    fun formatMessageTimestamp(timestamp: String?): String {
        if (timestamp.isNullOrEmpty()) return ""
        return try {
            val dateTime = LocalDateTime.parse(timestamp)
            val today = LocalDate.now()

            if (dateTime.toLocalDate() == today) {
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
            } else {
                dateTime.format(DateTimeFormatter.ofPattern("dd.MM, HH:mm"))
            }
        } catch (e: Exception) {
            "--:--"
        }
    }
}