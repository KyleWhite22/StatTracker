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
import com.mobileapps.stattracker.screens.*
import com.mobileapps.stattracker.viewmodels.GroupViewModel
import com.mobileapps.stattracker.viewmodels.GameViewModel

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    val groupViewModel: GroupViewModel = viewModel()
    val gameViewModel: GameViewModel = viewModel()

    NavHost(
        navController = navController,
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
                groups = groupViewModel.groups,
                onGroupDetailsClick = { groupId ->
                    navController.navigate(NavRoutes.GroupDetails.createRoute(groupId))
                },
                onViewGamesClick = {
                    navController.navigate(NavRoutes.Games.createRoute(NavRoutes.Games.ALL_GAMES))
                }
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

        //Group Details
        composable(
            route = NavRoutes.GroupDetails.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GroupScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onStartGameClick = { id ->
                    navController.navigate(NavRoutes.CreateGame.createRoute(id))
                },
                onViewPastGamesClick = { id ->
                    navController.navigate(NavRoutes.Games.createRoute(id))
                },
                groupViewModel = groupViewModel
            )
        }

        //Create Game
        composable(
            route = NavRoutes.CreateGame.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            CreateGameScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                onStartGame = { settings, t1, t2 ->
                    gameViewModel.startGame(groupId, settings, t1, t2) { gameId ->
                        navController.navigate(NavRoutes.ActiveGame.createRoute(gameId)) {
                            popUpTo(NavRoutes.GroupDetails.route) { inclusive = false }
                        }
                    }
                },
                groupViewModel = groupViewModel
            )
        }

        //Active Game
        composable(
            route = NavRoutes.ActiveGame.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            ActiveGameScreen(
                gameId = gameId,
                onGameEnded = { id ->
                    navController.navigate(NavRoutes.PostGameSummary.createRoute(id)) {
                        popUpTo(NavRoutes.ActiveGame.route) { inclusive = true }
                    }
                },
                gameViewModel = gameViewModel
            )
        }

        //Post Game Summary
        composable(
            route = NavRoutes.PostGameSummary.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            PostGameSummaryScreen(
                gameId = gameId,
                onDoneClick = {
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                },
                gameViewModel = gameViewModel
            )
        }

        //Past Games
        composable(
            route = NavRoutes.Games.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            GamesScreen(
                groupId = groupId,
                onBackClick = { navController.popBackStack() },
                gameViewModel = gameViewModel
            )
        }
    }
}
