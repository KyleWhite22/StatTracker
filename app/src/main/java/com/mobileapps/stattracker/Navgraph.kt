package com.mobileapps.stattracker

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mobileapps.stattracker.screens.*
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        // ── Auth ──────────────────────────────────────────────────────────────
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess  = { navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                }},
                onGoToSignUp    = { navController.navigate(NavRoutes.SignUp.route) }
            )
        }

        composable(NavRoutes.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(NavRoutes.Home.route) {
                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                }},
                onGoToLogin     = { navController.popBackStack() }
            )
        }

        // ── Home (groups list) ────────────────────────────────────────────────
        composable(NavRoutes.Home.route) {
            HomeScreen(
                onGroupClick = { groupId ->
                    navController.navigate(NavRoutes.GroupView.createRoute(groupId))
                }
            )
        }

    }
}