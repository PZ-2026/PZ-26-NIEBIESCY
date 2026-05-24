package com.vectorpeaks.edulink.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import com.vectorpeaks.edulink.security.AuthInterceptor
import com.vectorpeaks.edulink.security.AuthPreferencesManager
import android.content.Context

/**
 * Singleton client for handling network requests via Retrofit.
 * Automatically injects JWT tokens into authorized endpoints using [AuthInterceptor].
 *
 * @version 1.2
 * @author EduLink Team
 */

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8080/"
    private lateinit var authPrefs: AuthPreferencesManager

    /**
     * Initializes the preferences manager.
     * Must be called in Application's onCreate() before making any API requests.
     */
    fun initialize(context: Context) {
        authPrefs = AuthPreferencesManager(context.applicationContext)
    }

    // Lazy initialization of OkHttpClient ensuring initialize() has been called
    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // 1. Logging Interceptor (for debugging)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })

            // Custom interceptor adding Authorization header (jwt token)
            .addInterceptor(AuthInterceptor(authPrefs) { apiService })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}