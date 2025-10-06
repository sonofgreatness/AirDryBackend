package org.montigny.client.navigation

sealed class Destination(val route: String) {
    object Users : Destination("users")
    object Organizations : Destination("organizations")
    object Work : Destination("work")
    object Reports : Destination("reports")
    object Settings : Destination("settings")
    object Login : Destination("login")
    object MainSettings : Destination("main_settings")
    data class EditUser(val userId: String) : Destination("edit_user/$userId")
    object  CreateAccount: Destination("create_user")

}