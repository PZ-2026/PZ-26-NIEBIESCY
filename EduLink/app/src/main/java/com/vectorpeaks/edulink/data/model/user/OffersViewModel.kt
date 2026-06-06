package com.vectorpeaks.edulink.data.model.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.network.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * ViewModel for the student offer search screen.
 * Handles paginated loading, filtering, sorting and pull-to-refresh of offers.
 */
class OffersViewModel : ViewModel() {

    // --- Public state ---

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    /** Accumulated list of offers loaded so far (grows as user scrolls). */
    val offers: StateFlow<List<Offer>> = _offers

    private val _isLoading = MutableStateFlow(false)
    /** True only during the first page load (shows full-screen spinner). */
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isLoadingMore = MutableStateFlow(false)
    /** True when loading the next page (shows spinner at bottom of list). */
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore

    private val _isRefreshing = MutableStateFlow(false)
    /** True while pull-to-refresh is in progress. */
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _error = MutableStateFlow<String?>(null)
    /** Holds error message if the last request failed, null otherwise. */
    val error: StateFlow<String?> = _error

    private val _totalElements = MutableStateFlow(0)
    /** Total number of offers matching current filters (used for "Znaleziono: X"). */
    val totalElements: StateFlow<Int> = _totalElements

    // --- Pagination state ---

    /** Index of the next page to fetch (0-based). */
    private var currentPage = 0

    /** True when the backend reports no more pages exist. */
    private var isLastPage = false

    /**
     * Tracks the currently running load job.
     * Cancelling it before starting a new one prevents duplicate requests.
     */
    private var loadJob: Job? = null

    // --- Last used filters (saved so loadNextPage can reuse them) ---

    private var lastSubject: String? = null
    private var lastCity: String? = null
    private var lastOnlineOnly: Boolean = false
    private var lastSearch: String? = null
    private var lastSortBy: String = "createdAt"
    private var lastSortDir: String = "desc"

    // -- if is loaded, then don't load again --
    private var hasLoaded = false

    // --- Public API ---

    /**
     * Resets pagination and loads the first page with the given filters and sort order.
     * Cancels any in-progress load before starting.
     * Call this whenever filters or sort mode change.
     *
     * @param subject    optional subject name filter
     * @param city       optional city filter
     * @param onlineOnly if true, returns only online offers
     * @param search     optional search text
     * @param sortBy     field to sort by: "createdAt" or "rating"
     * @param sortDir    sort direction: "asc" or "desc"
     */
    fun loadOffers(
        subject: String? = null,
        city: String? = null,
        onlineOnly: Boolean = false,
        search: String? = null,
        sortBy: String = "createdAt",
        sortDir: String = "desc"
    ) {

        // Skip if already loaded with the same filters — called again on recomposition
        if (hasLoaded
            && lastSubject == subject
            && lastCity == city
            && lastOnlineOnly == onlineOnly
            && lastSearch == search
            && lastSortBy == sortBy
        ) return  // nothing changed, keep existing list

        hasLoaded = true

        // Save filters for reuse by loadNextPage
        lastSubject = subject
        lastCity = city
        lastOnlineOnly = onlineOnly
        lastSearch = search
        lastSortBy = sortBy
        lastSortDir = sortDir

        // Reset pagination and list
        currentPage = 0
        isLastPage = false
        _offers.value = emptyList()

        // Cancel previous job and start fresh
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            fetchPage(isFirstPage = true)
        }
    }

    /**
     * Loads the next page using the same filters as the last [loadOffers] call.
     * Does nothing if already loading or if there are no more pages.
     * Call this when the user scrolls near the end of the list.
     */
    fun loadNextPage() {
        if (isLastPage || _isLoadingMore.value || _isLoading.value) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            fetchPage(isFirstPage = false)
        }
    }

    /**
     * Reloads the first page with current filters.
     * Used by pull-to-refresh gesture.
     */
    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isRefreshing.value = true
            hasLoaded = false
            currentPage = 0
            isLastPage = false
            _offers.value = emptyList()
            fetchPage(isFirstPage = true)
            _isRefreshing.value = false
        }
    }

    // --- Private ---

    /**
     * Performs the actual network request for the current page.
     * Must be called from within a coroutine (suspend fun).
     *
     * @param isFirstPage if true, shows full-screen spinner; otherwise shows bottom spinner
     */
    private suspend fun fetchPage(isFirstPage: Boolean) {
        if (isFirstPage) _isLoading.value = true
        else _isLoadingMore.value = true

        _error.value = null

        try {
            val response = RetrofitClient.apiService.getOffersPaged(
                subject    = lastSubject,
                city       = lastCity,
                onlineOnly = if (lastOnlineOnly) true else null,
                search     = lastSearch,
                page       = currentPage,
                size       = 10,
                sortBy     = lastSortBy,
                sortDir    = lastSortDir
            )

            // Append new page to existing list
            _offers.value = _offers.value + response.content
            _totalElements.value = response.totalElements
            isLastPage = response.last
            currentPage++

        } catch (e: HttpException) {
            _error.value = "HTTP error: ${e.code()}"
        } catch (e: IOException) {
            _error.value = "Network error"
        } catch (e: Exception) {
            _error.value = "Unexpected error: ${e.message}"
        } finally {
            _isLoading.value = false
            _isLoadingMore.value = false
        }
    }
}