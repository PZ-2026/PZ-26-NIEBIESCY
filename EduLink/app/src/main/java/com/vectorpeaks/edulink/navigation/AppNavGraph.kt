package com.vectorpeaks.edulink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.security.AuthPreferencesManager
import com.vectorpeaks.edulink.security.SessionManager
import com.vectorpeaks.edulink.ui.components.MaintenanceBannerWrapper
import com.vectorpeaks.edulink.ui.screens.admin.AdminMainScreen
import com.vectorpeaks.edulink.ui.screens.login.AutoLoginScreen
import com.vectorpeaks.edulink.ui.screens.login.LoginScreen
import com.vectorpeaks.edulink.ui.screens.register.RegisterScreen
import com.vectorpeaks.edulink.ui.screens.student.OfferDetailScreen
import com.vectorpeaks.edulink.ui.screens.student.ReviewsScreen
import com.vectorpeaks.edulink.ui.screens.student.StudentMainScreen
import com.vectorpeaks.edulink.ui.screens.tutor.TutorMainScreen
import com.vectorpeaks.edulink.ui.screens.tutor.TutorReviewsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context       = LocalContext.current
    val authPrefs     = remember { AuthPreferencesManager(context) }

    var currentUser by remember { mutableStateOf<User?>(null) }

    val startDestination = remember {
        if (authPrefs.getRefreshToken() != null) NavRoutes.AutoLogin.route
        else NavRoutes.Login.route
    }

    LaunchedEffect(Unit) {
        SessionManager.sessionExpired.collect {
            currentUser = null
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = startDestination) {



        // ── Auto Login ───────────────────────────────────────────
        composable(NavRoutes.AutoLogin.route) {
            AutoLoginScreen(
                authPrefs = authPrefs,
                onSuccess = { user ->
                    currentUser = user
                    val destination = when (user.getRole()) {
                        RoleID.STUDENT -> NavRoutes.StudentMain.route
                        RoleID.TUTOR   -> NavRoutes.TutorMain.route
                        RoleID.ADMIN   -> NavRoutes.AdminMain.route
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.AutoLogin.route) { inclusive = true }
                    }
                },
                onFailure = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.AutoLogin.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Login ────────────────────────────────────────────────
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    val destination = when (user.getRole()) {
                        RoleID.STUDENT -> NavRoutes.StudentMain.route
                        RoleID.TUTOR   -> NavRoutes.TutorMain.route
                        RoleID.ADMIN   -> NavRoutes.AdminMain.route
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(NavRoutes.Register.route) }
            )
        }

        // ── Register ─────────────────────────────────────────────
        composable(NavRoutes.Register.route) {
            RegisterScreen(onBackToLogin = { navController.popBackStack() })
        }

        // ── Student ──────────────────────────────────────────────
        composable(NavRoutes.StudentMain.route) {
            currentUser?.let { user ->
                MaintenanceBannerWrapper {
                    StudentMainScreen(
                        user = user,
                        onLogout = {
                            currentUser = null
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },

                        onNavigateToOfferDetail = { offerId ->
                            navController.navigate(NavRoutes.OfferDetail.createRoute(offerId))
                        },
                        onNavigateToReviews = { tutorId, tutorName ->
                            navController.navigate(
                                NavRoutes.TutorReviews.createRoute(tutorId, tutorName)
                            )
                        }
                    )
                }
            }
        }

            // ── Offer Detail ──────────────────────────────────────
            composable(
                route = NavRoutes.OfferDetail.route,
                arguments = listOf(navArgument("offerId") { type = NavType.IntType })
            ) { backStackEntry ->
                val offerId = backStackEntry.arguments?.getInt("offerId") ?: return@composable

                OfferDetailScreen(
                    offerId = offerId,
                    studentId = currentUser?.id ?: 0,
                    // onBack - need to prevent bug when user multi clicks in a short time
                    onBack = {
                        if (navController.currentBackStackEntry?.destination?.route == NavRoutes.OfferDetail.route) {
                            navController.popBackStack()
                        }
                    },
                    onTutorClick = { tutorId, tutorName ->
                        navController.navigate(NavRoutes.TutorReviews.createRoute(tutorId, tutorName))
                    }
                )
            }

        // ── Tutor ────────────────────────────────────────────────
        composable(
            route = NavRoutes.TutorMain.route + "?startTab={startTab}",
            arguments = listOf(
                navArgument("startTab") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val startTab = backStackEntry.arguments?.getInt("startTab") ?: 0
            currentUser?.let { user ->
                MaintenanceBannerWrapper {
                    TutorMainScreen(
                        user = user,
                        startTab = startTab,
                        onLogout = {
                            currentUser = null
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onNavigateToReviews = { tutorId, tutorName ->
                            navController.navigate(
                                NavRoutes.TutorReviewsFromTutor.createRoute(tutorId, tutorName)
                            )
                        }
                    )
                }
            }
        }

        // ── Admin ────────────────────────────────────────────────
        composable(NavRoutes.AdminMain.route) {
            currentUser?.let { user ->
                MaintenanceBannerWrapper {
                    AdminMainScreen(
                        user = user,
                        onLogout = {
                            currentUser = null
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }

        // ── Reviews (Student) ────────────────────────────────────
        composable(
            route = NavRoutes.TutorReviews.route,
            arguments = listOf(
                navArgument("tutorId") { type = NavType.IntType },
                navArgument("tutorName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tutorId   = backStackEntry.arguments?.getInt("tutorId") ?: return@composable
            val tutorName = backStackEntry.arguments?.getString("tutorName") ?: ""
            ReviewsScreen(
                tutorName = tutorName,
                tutorId   = tutorId,
                onBack = {
                    if (navController.currentBackStackEntry?.destination?.route == NavRoutes.TutorReviews.route) {
                        navController.popBackStack()
                    }
                }
            )
        }

        // ── Reviews (Tutor) ──────────────────────────────────────
        composable(
            route = NavRoutes.TutorReviewsFromTutor.route,
            arguments = listOf(
                navArgument("tutorId") { type = NavType.IntType },
                navArgument("tutorName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tutorId   = backStackEntry.arguments?.getInt("tutorId") ?: return@composable
            val tutorName = backStackEntry.arguments?.getString("tutorName") ?: ""
            TutorReviewsScreen(
                tutorName = tutorName,
                tutorId   = tutorId,
                onBack = {
                    if (navController.currentBackStackEntry?.destination?.route == NavRoutes.TutorReviewsFromTutor.route) {
                        navController.navigate(NavRoutes.TutorMain.route + "?startTab=1") {
                            popUpTo(NavRoutes.TutorMain.route + "?startTab={startTab}") {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
    }
}