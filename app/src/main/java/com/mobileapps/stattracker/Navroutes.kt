package com.mobileapps.stattracker

sealed class NavRoutes(val route: String) {
    object Login        : NavRoutes("login")
    object SignUp       : NavRoutes("signup")
    object Home         : NavRoutes("home")

    object CheckEmail : NavRoutes("check_email/{email}") {
        fun createRoute(email: String) = "check_email/$email"
    }
    object CreateGroup : NavRoutes("create group")

    object GroupDetails : NavRoutes("group_details/{groupId}") {
        fun createRoute(groupId: String) = "group_details/$groupId"
    }

    object CreateGame : NavRoutes("create_game/{groupId}") {
        fun createRoute(groupId: String) = "create_game/$groupId"
    }

    object ActiveGame : NavRoutes("active_game/{gameId}") {
        fun createRoute(gameId: String) = "active_game/$gameId"
    }

    object PostGameSummary : NavRoutes("post_game_summary/{gameId}") {
        fun createRoute(gameId: String) = "post_game_summary/$gameId"
    }

    object Games : NavRoutes("games/{groupId}") {
        fun createRoute(groupId: String) = "games/$groupId"
        const val ALL_GAMES = "all"
    }
}