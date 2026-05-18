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
import com.vectorpeaks.edulink.ui.screens.admin.AdminMainScreen
import com.vectorpeaks.edulink.ui.screens.login.AutoLoginScreen
import com.vectorpeaks.edulink.ui.screens.login.LoginScreen
import com.vectorpeaks.edulink.ui.screens.register.RegisterScreen
import com.vectorpeaks.edulink.ui.screens.student.ReviewsScreen
import com.vectorpeaks.edulink.ui.screens.student.StudentMainScreen
import com.vectorpeaks.edulink.ui.screens.tutor.TutorMainScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context       = LocalContext.current
    val authPrefs     = remember { AuthPreferencesManager(context) }

    var currentUser by remember { mutableStateOf<User?>(null) }

    // Determine the start destination: if a refresh token exists -> the user was logged in
    // Note: currentUser will be null after an app restart — handled below
    val startDestination = remember {
        if (authPrefs.getRefreshToken() != null) NavRoutes.AutoLogin.route
        else NavRoutes.Login.route
    }

    // Observe session expiration from AuthInterceptor
    // When the interceptor clears authPrefs -> navigate to login
    LaunchedEffect(Unit) {
        SessionManager.sessionExpired.collect {
            currentUser = null
            navController.navigate(NavRoutes.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController    = navController,
        startDestination = startDestination
    ) {

        // ── Auto Login on App Restart ────────────────────────────
        // The user had a valid refresh token — fetch their data
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
                    // Refresh token expired or network error -> login screen
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
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        // ── Student ──────────────────────────────────────────────
        composable(NavRoutes.StudentMain.route) {
            currentUser?.let { user ->
                StudentMainScreen(
                    user = user,
                    onLogout = {
                        currentUser = null
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToReviews = { tutorId, tutorName ->
                        navController.navigate(
                            NavRoutes.TutorReviews.createRoute(tutorId, tutorName)
                        )
                    }
                )
            }
        }

        // ── Tutor ────────────────────────────────────────────────
        composable(NavRoutes.TutorMain.route) {
            currentUser?.let { user ->
                TutorMainScreen(
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

        // ── Admin ────────────────────────────────────────────────
        composable(NavRoutes.AdminMain.route) {
            currentUser?.let { user ->
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

        composable(
            route = NavRoutes.TutorReviews.route,
            arguments = listOf(
                navArgument("tutorId") { type = NavType.IntType },
                navArgument("tutorName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val tutorId = backStackEntry.arguments?.getInt("tutorId") ?: return@composable
            val tutorName = backStackEntry.arguments?.getString("tutorName") ?: ""
            ReviewsScreen(
                tutorName = tutorName,
                tutorId = tutorId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}