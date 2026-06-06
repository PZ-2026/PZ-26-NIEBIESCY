package com.vectorpeaks.edulink.data.model

/**
 * Generic wrapper for paginated API responses.
 * Matches the JSON structure returned by the backend's /paged endpoint.
 *
 * @param T the type of items in the content list
 * @property content list of items on the current page
 * @property totalElements total number of items across all pages
 * @property totalPages total number of pages
 * @property number current page index (0-based)
 * @property last true if this is the last page
 */
data class PagedResponse<T>(
    val content: List<T>,
    val totalElements: Int,
    val totalPages: Int,
    val number: Int,
    val last: Boolean
)