package com.vectorpeaks.edulink.data.remote

import com.vectorpeaks.edulink.data.model.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @GET("api/users")
    suspend fun getUsers(): List<User>

    @POST("api/users")
    suspend fun addUser(@Body user: User): User
}