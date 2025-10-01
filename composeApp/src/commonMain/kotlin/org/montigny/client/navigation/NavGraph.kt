package org.montigny.client.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import org.montigny.client.auth.SessionManager
import org.montigny.client.ui.organizations.OrganizationsScreen
import org.montigny.client.ui.reports.ReportsScreen
import org.montigny.client.ui.settings.MainSettingsScreen
import org.montigny.client.ui.settings.SettingsScreen
import org.montigny.client.ui.users.LoginScreen
import org.montigny.client.ui.users.UsersScreen
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.ui.work.WorkScreen

@Composable
fun NavGraph(
    navigator: Navigator,
    authViewModel: AuthViewModel,
    sessionManager: SessionManager,
    usersViewModel: UserViewModel,
) {
    val currentDestination by navigator.currentDestination

    when (currentDestination) {
        Destination.Users -> UsersScreen(usersViewModel)
        Destination.Organizations -> OrganizationsScreen()
        Destination.Work -> WorkScreen()
        Destination.Reports -> ReportsScreen()
        Destination.Settings -> SettingsScreen()
        Destination.Login -> LoginScreen(authViewModel, sessionManager) {
            navigator.navigateTo(Destination.Settings)
        }
        Destination.MainSettings -> MainSettingsScreen()
    }
}
