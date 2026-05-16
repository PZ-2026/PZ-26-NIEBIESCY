package com.vectorpeaks.edulink.security

// AuthInterceptor.kt
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val authPrefs: AuthPreferencesManager) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Get token from preferences
        val token = authPrefs.getToken()

        // Build new request with Authorization header
        val requestBuilder = originalRequest.newBuilder()

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val newRequest = requestBuilder.build()
        return chain.proceed(newRequest)
    }
}