package com.vectorpeaks.edulink.navigation

sealed class NavRoutes(val route: String) {
    object Login : NavRoutes("login")

    // Student
    object StudentMain : NavRoutes("student_main")
    object StudentSearch : NavRoutes("student_search")
    object StudentHistory : NavRoutes("student_history")
    object StudentChat : NavRoutes("student_chat")
    object StudentProfile : NavRoutes("student_profile")
    object OfferDetail : NavRoutes("offer_detail/{offerId}") {
        fun createRoute(offerId: Int) = "offer_detail/$offerId"
    }

    // Tutor
    object TutorMain : NavRoutes("tutor_main")
    object TutorDashboard : NavRoutes("tutor_dashboard")
    object TutorOffers : NavRoutes("tutor_offers")
    object TutorReservations : NavRoutes("tutor_reservations")
    object TutorChat : NavRoutes("tutor_chat")
    object TutorProfile : NavRoutes("tutor_profile")

    // Admin
    object AdminMain : NavRoutes("admin_main")
    object AdminDashboard : NavRoutes("admin_dashboard")
    object AdminUsers : NavRoutes("admin_users")
    object AdminReports : NavRoutes("admin_reports")
    object AdminSettings : NavRoutes("admin_settings")
}
