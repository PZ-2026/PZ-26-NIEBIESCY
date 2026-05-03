package com.vectorpeaks.edulink.network

import com.vectorpeaks.edulink.data.model.BookingRequest
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.data.model.user.UserResponse
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.user.ReviewRequest
import com.vectorpeaks.edulink.data.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.DELETE

interface ApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): UserResponse

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: Int): User

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): User

    @GET("api/offers")
    suspend fun getOffers(
        @Query("subject") subject: String? = null,
        @Query("city") city: String? = null,
        @Query("onlineOnly") onlineOnly: Boolean? = null,
        @Query("search") search: String? = null
    ): List<Offer>

    @GET("api/data/subjects")
    suspend fun getSubjects(): List<String>

    @GET("api/data/cities")
    suspend fun getCities(): List<String>

    @GET("api/bookings/student/{studentId}")
    suspend fun getBookingsForStudent(@Path("studentId") studentId: Int): List<BookingResponse>

    @POST("api/reviews")
    suspend fun addReview(@Body review: ReviewRequest)

    @GET("api/offers/{id}")
    suspend fun getOfferById(@Path("id") id: Int): Offer

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<Unit>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>
}