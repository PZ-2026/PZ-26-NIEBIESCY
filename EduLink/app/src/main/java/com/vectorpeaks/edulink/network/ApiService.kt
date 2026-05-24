package com.vectorpeaks.edulink.network

import com.vectorpeaks.edulink.data.model.BookingRequest
import com.vectorpeaks.edulink.data.model.user.BookingResponse
import com.vectorpeaks.edulink.data.model.LoginRequest
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.data.model.user.UserResponse
import com.vectorpeaks.edulink.data.model.Offer
import com.vectorpeaks.edulink.data.model.OfferCreateRequest
import com.vectorpeaks.edulink.data.model.user.ReviewRequest
import com.vectorpeaks.edulink.data.model.RegisterRequest
import com.vectorpeaks.edulink.data.model.Slot
import com.vectorpeaks.edulink.data.model.SubjectDto
import com.vectorpeaks.edulink.data.model.chat.ChatResponse
import com.vectorpeaks.edulink.data.model.chat.CreateChatRequest
import com.vectorpeaks.edulink.data.model.chat.MessageResponse
import com.vectorpeaks.edulink.data.model.chat.SendMessageRequest
import com.vectorpeaks.edulink.data.model.user.AdminStatsResponse
import com.vectorpeaks.edulink.data.model.user.AdminReportsResponse
import com.vectorpeaks.edulink.data.model.user.GlobalLimitDto
import com.vectorpeaks.edulink.data.model.user.ReviewResponse
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.Call
import retrofit2.http.Streaming

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

    @GET("api/reviews/tutor/{tutorId}")
    suspend fun getReviewsByTutor(@Path("tutorId") tutorId: Int): List<ReviewResponse>

    @GET("api/offers/{id}")
    suspend fun getOfferById(@Path("id") id: Int): Offer

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<Unit>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: Int): Response<Unit>

    @POST("api/users/register")
    suspend fun register(@Body request: RegisterRequest): Response<Unit>

    @GET("api/admin/reports")
    suspend fun getAdminReports(): AdminReportsResponse

    @GET("api/admin/stats")
    suspend fun getAdminStats(): AdminStatsResponse

    @GET("api/admin/bookings/pending")
    suspend fun getPendingBookings(): List<BookingResponse>

    @GET("api/admin/settings")
    suspend fun getAdminSettings(): GlobalLimitDto

    @PUT("api/admin/settings")
    suspend fun updateAdminSettings(@Body settings: GlobalLimitDto): Response<Unit>

    @GET("api/users")
    suspend fun getAllUsers(): List<User>

    @PUT("api/users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: Int, @Body body: Map<String, Int>): Response<Unit>

    @GET("api/bookings/tutor/{tutorId}")
    suspend fun getBookingsForTutor(@Path("tutorId") tutorId: Int): List<BookingResponse>

    @PUT("api/bookings/{bookingId}/status")
    suspend fun updateBookingStatus(
        @Path("bookingId") bookingId: Int,
        @Query("status") status: String): Response<Unit>

    @GET("api/offers/tutor/{tutorId}")
    suspend fun getOffersByTutor(@Path("tutorId") tutorId: Int): List<Offer>

    @POST("api/offers")
    suspend fun createOffer(@Body request: OfferCreateRequest): Response<Unit>

    @GET("api/slots")
    suspend fun getAvailabilitySlots(): List<Slot>

    @GET("api/data/subjects-with-id")
    suspend fun getSubjectsWithId(): List<SubjectDto>

    @GET("api/slots/by-day/{dayOfWeek}")
    suspend fun getSlotsByDay(@Path("dayOfWeek") dayOfWeek: Int): List<Slot>

    @GET("api/slots/available/{tutorId}")
    suspend fun getAvailableSlotsForTutor(@Path("tutorId") tutorId: Int): List<Slot>

    @GET("api/slots/available/{tutorId}/excluding/{offerId}")
    suspend fun getAvailableSlotsExcludingOffer(
        @Path("tutorId") tutorId: Int,
        @Path("offerId") offerId: Int
    ): List<Slot>

    @DELETE("api/offers/{id}")
    suspend fun deleteOffer(@Path("id") id: Int): Response<Unit>

    @PUT("api/offers/{id}")
    suspend fun updateOffer(@Path("id") id: Int, @Body request: OfferCreateRequest): Response<Unit>

// --------- CHAT ---------

    /**
     * Creates a new chat between two users or returns the existing one.
     * Typically called when a student taps "Message tutor" on the offer screen.
     *
     * @param request containing userId1 and userId2
     * @return ChatResponse with chat thread details and participants
     */
    @POST("api/chats")
    suspend fun createOrGetChat(@Body request: CreateChatRequest): ChatResponse

    /**
     * Returns all chat threads (conversations) for the given user,
     * ordered by most recent activity.
     *
     * @param userId the ID of the logged-in user
     * @return list of ChatResponse objects
     */
    @GET("api/chats/user/{userId}")
    suspend fun getChatsForUser(@Path("userId") userId: Int): List<ChatResponse>

    /**
     * Returns the full message history for the given chat thread
     * in chronological order (oldest first).
     *
     * @param chatId the ID of the chat thread
     * @return list of MessageResponse objects
     */
    @GET("api/chats/{chatId}/messages")
    suspend fun getMessages(@Path("chatId") chatId: Int): List<MessageResponse>

    /**
     * Sends a new message in the given chat thread.
     * May trigger a push notification to the other participant.
     *
     * @param chatId the ID of the chat thread
     * @param request containing senderId and message content
     * @return Response containing the saved MessageResponse
     */
    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessage(
        @Path("chatId") chatId: Int,
        @Body request: SendMessageRequest
    ): Response<MessageResponse>

    /**
     * Sends the device's FCM registration token to the backend so the server
     * can deliver push notifications to this specific device.
     *
     * @param userId the ID of the logged-in user
     * @param token  the FCM registration token for this device
     */
    @POST("api/users/{userId}/fcm-token")
    suspend fun updateFcmToken(
        @Path("userId") userId: Int,
        @Body token: Map<String, String>
    ): Response<Unit>
  
    // ==================== Admin: Offers ====================

    @GET("api/admin/offers/pending")
    suspend fun getPendingOffers(): List<Offer>

    @PUT("api/admin/offers/{id}/status")
    suspend fun updateOfferStatus(
        @Path("id") id: Int,
        @Query("status") status: String
    ): Response<Unit>

    // ==================== Admin: Subjects ====================

    @POST("api/admin/subjects")
    suspend fun addSubject(@Body body: Map<String, String>): Response<SubjectDto>

    @DELETE("api/admin/subjects/{id}")
    suspend fun deleteSubject(@Path("id") id: Int): Response<Unit>

    // Synchronous version for AuthInterceptor (runs outside coroutines)
    @POST("api/auth/refresh")
    fun refreshTokenSync(@Body body: Map<String, String>): Call<ResponseBody>

    // Suspend version for ViewModel
    @POST("api/auth/refresh")
    suspend fun refreshToken(@Body body: Map<String, String>): Response<ResponseBody>

    @POST("api/auth/logout")
    suspend fun logout(@Body body: Map<String, String>): Response<Unit>

    @Streaming
    @GET("api/admin/reports/pdf")
    suspend fun downloadReportPdf(
        @Query("from") from: String,
        @Query("to")   to: String
    ): ResponseBody
}