package com.vectorpeaks.edulink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vectorpeaks.edulink.data.model.user.User
import com.vectorpeaks.edulink.ui.screens.admin.AdminMainScreen
import com.vectorpeaks.edulink.ui.screens.login.LoginScreen
import com.vectorpeaks.edulink.ui.screens.student.ReviewsScreen
import com.vectorpeaks.edulink.ui.screens.student.StudentMainScreen
import com.vectorpeaks.edulink.ui.screens.tutor.TutorMainScreen
import com.vectorpeaks.edulink.data.model.user.RoleID
import com.vectorpeaks.edulink.ui.screens.register.RegisterScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    var currentUser by remember { mutableStateOf<User?>(null) }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = { user ->
                    currentUser = user
                    val destination = when (user.getRole()) {
                        RoleID.STUDENT -> NavRoutes.StudentMain.route
                        RoleID.TUTOR -> NavRoutes.TutorMain.route
                        RoleID.ADMIN -> NavRoutes.AdminMain.route
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate(NavRoutes.Register.route) }
            )
        }

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
                            NavRoutes.TutorReviews.createRoute(tutorId, tutorName)
                        )
                    }
                )
            }
        }

        composable(NavRoutes.Register.route) {
            RegisterScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

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

        composable(NavRoutes.TutorMain.route) {
            currentUser?.let { user ->
                TutorMainScreen(
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
                onBack = {
                    navController.navigate(NavRoutes.TutorMain.route + "?startTab=1") {
                        popUpTo(NavRoutes.TutorMain.route + "?startTab={startTab}") {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}