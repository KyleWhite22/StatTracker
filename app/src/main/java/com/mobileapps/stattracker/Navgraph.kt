package com.mobileapps.stattracker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.mobileapps.stattracker.screens.*
import com.mobileapps.stattracker.viewmodels.GroupViewModel
import com.mobileapps.stattracker.classes.*

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val groupViewModel: GroupViewModel = viewModel()

    NavHost(
        navController = navController,
        //use this for debugging:
        //startDestination = NavRoutes.Home.route
        //otherwise:
        startDestination = NavRoutes.Login.route
    ) {

        //Authentication
        composable(NavRoutes.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onGoToSignUp = { navController.navigate(NavRoutes.SignUp.route) }
            )
        }

        //SignUp
        composable(NavRoutes.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = { email ->
                    navController.navigate(NavRoutes.CheckEmail.createRoute(email)) {
                        popUpTo(NavRoutes.SignUp.route) { inclusive = true }
                    }
                },
                onGoToLogin = { navController.popBackStack() }
            )
        }

        //Check Email
        composable(
            route = NavRoutes.CheckEmail.route,
            arguments = listOf(navArgument("email") { type = NavType.StringType })
        ) { backStackEntry ->
            val email = backStackEntry.arguments?.getString("email") ?: ""
            CheckEmailScreen(
                email = email,
                onGoToLogin = {
                    navController.navigate(NavRoutes.Login.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        //Home
        composable(NavRoutes.Home.route) {
            LaunchedEffect(Unit) {
                groupViewModel.loadGroups()
            }
            HomeScreen(
                onGroupClick = { navController.navigate(NavRoutes.CreateGroup.route) },
                onLogOut = { navController.navigate(NavRoutes.Login.route) },
                groups = groupViewModel.groups
            )
        }

        //Create Group
        composable(NavRoutes.CreateGroup.route) {
            CreateGroupScreen(
                onSubmitClick = { name, location ->
                    groupViewModel.createGroup(name, location) {
                        navController.navigate(NavRoutes.Home.route)
                    }
                }
            )
        }
    }
}