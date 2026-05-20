// AuthInterceptor.kt
package com.vectorpeaks.edulink.security

import com.vectorpeaks.edulink.network.ApiService
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import timber.log.Timber

/**
 * OkHttp interceptor that:
 * 1. Adds Authorization: Bearer <token> to every request
 * 2. On 401 response — tries to refresh the access token using refresh token
 * 3. On successful refresh — retries the original request with new token
 * 4. On failed refresh — clears session (user must log in again)
 *
 * @version 1.3
 * @author EduLink Team
 */
class AuthInterceptor(
    private val authPrefs: AuthPreferencesManager,
    private val getApiService: () -> ApiService  // lambda to avoid circular dependency
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Do not add the token to public endpoints
        if (isPublicEndpoint(originalRequest)) {
            return chain.proceed(originalRequest)
        }

        // Add the current access token
        val token = authPrefs.getToken()
        val requestWithToken = originalRequest.withBearerToken(token)
        val response = chain.proceed(requestWithToken)

        // If 401 → access token has expired → try to refresh
        if (response.code == 401) {
            response.close()

            val refreshToken = authPrefs.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                // No refresh token available → force re-authentication
                authPrefs.clearAll()
                SessionManager.notifySessionExpired()
                return chain.proceed(originalRequest) // will return 401, handled by navigation
            }

            return tryRefresh(chain, originalRequest, refreshToken)
        }

        return response
    }

    /**
     * Attempts to refresh the access token.
     * If successful → retries the original request with the new token.
     * If failed → clears the session and returns 401.
     */
    private fun tryRefresh(
        chain: Interceptor.Chain,
        originalRequest: Request,
        refreshToken: String
    ): Response {
        return try {
            // Synchronous call — the interceptor operates outside of coroutines
            val refreshResponse = getApiService()
                .refreshTokenSync(mapOf("refreshToken" to refreshToken))
                .execute()

            if (refreshResponse.isSuccessful) {
                val body    = refreshResponse.body()?.string() ?: ""
                val newToken = JSONObject(body).getString("token")

                // Save the new access token
                authPrefs.saveToken(newToken)
                Timber.d("Token refreshed successfully")

                // Retry the original request with the new token
                chain.proceed(originalRequest.withBearerToken(newToken))
            } else {
                // Refresh token is expired or revoked → force login
                Timber.w("Refresh failed (${refreshResponse.code()}) — clearing session")
                authPrefs.clearAll()
                SessionManager.notifySessionExpired()
                chain.proceed(originalRequest) // will return 401
            }
        } catch (e: Exception) {
            Timber.e(e, "Token refresh exception — clearing session")
            authPrefs.clearAll()
            SessionManager.notifySessionExpired()
            chain.proceed(originalRequest)
        }
    }

    // ── Helpers ─────────────────────────────────────────────────

    private fun Request.withBearerToken(token: String?): Request {
        return if (!token.isNullOrBlank()) {
            newBuilder().header("Authorization", "Bearer $token").build()
        } else {
            this
        }
    }

    private fun isPublicEndpoint(request: Request): Boolean {
        val path = request.url.encodedPath
        return path.contains("/api/auth/login") ||
                path.contains("/api/auth/refresh") ||
                path.contains("/api/users/register") ||
                path.contains("/api/public/")
    }
}