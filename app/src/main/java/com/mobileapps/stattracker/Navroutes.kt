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
}