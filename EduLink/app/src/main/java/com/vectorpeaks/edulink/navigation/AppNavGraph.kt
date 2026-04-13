package com.vectorpeaks.edulink.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vectorpeaks.edulink.data.model.User
import com.vectorpeaks.edulink.ui.screens.admin.AdminMainScreen
import com.vectorpeaks.edulink.ui.screens.login.LoginScreen
import com.vectorpeaks.edulink.ui.screens.student.StudentMainScreen
import com.vectorpeaks.edulink.ui.screens.tutor.TutorMainScreen
import com.vectorpeaks.edulink.data.model.RoleID

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
                    val destination = when (user.role) {
                        RoleID.STUDENT -> NavRoutes.StudentMain.route
                        RoleID.TUTOR   -> NavRoutes.TutorMain.route
                        RoleID.ADMIN   -> NavRoutes.AdminMain.route
                    }
                    navController.navigate(destination) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
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
    }
}
