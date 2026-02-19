package com.mobileapps.stattracker

sealed class NavRoutes(val route: String) {
    object Login        : NavRoutes("login")
    object SignUp       : NavRoutes("signup")
    object Home         : NavRoutes("home")
    object GroupView    : NavRoutes("group/{groupId}") {
        fun createRoute(groupId: String) = "group/$groupId"
    }
    object CreateGame   : NavRoutes("group/{groupId}/create_game") {
        fun createRoute(groupId: String) = "group/$groupId/create_game"
    }
    object ActiveGame   : NavRoutes("group/{groupId}/active_game/{gameId}") {
        fun createRoute(groupId: String, gameId: String) = "group/$groupId/active_game/$gameId"
    }
    object CheckEmail : NavRoutes("check_email/{email}") {
        fun createRoute(email: String) = "check_email/$email"
    }
}