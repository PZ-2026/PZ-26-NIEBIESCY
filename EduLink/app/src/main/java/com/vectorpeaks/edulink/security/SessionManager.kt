package com.vectorpeaks.edulink.security

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * A singleton that communicates session expiration between the network layer
 * (AuthInterceptor) and the UI layer (navigation).
 *
 * AuthInterceptor emits an event when the refresh token expires.
 * MainActivity observes it and navigates back to the login screen.
 */
object SessionManager {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired = _sessionExpired.asSharedFlow()

    /** Call this from AuthInterceptor when the session expires */
    fun notifySessionExpired() {
        _sessionExpired.tryEmit(Unit)
    }
}