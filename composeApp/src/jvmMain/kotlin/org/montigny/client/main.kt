package org.montigny.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.di.appModule
import org.montigny.client.di.databaseModule
import org.montigny.client.ui.organizations.viewModels.OrganizationDepartmentsAndSectionsViewModel
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.ConnectivityObserver

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "IMS (GUNDANE@MONTIGNY)",
    ) {
        startKoin {
            modules(databaseModule,appModule)
        }
    val sessionManager: SessionManager = koinInject()
    val authViewModel: AuthViewModel = koinInject()
    val usersViewModel: UserViewModel = koinInject()
    val offlineSessionManager: OfflineSessionManager = koinInject()
    val connectivityObserver: ConnectivityObserver = koinInject ()
    val  organizationViewModel: OrganizationViewModel = koinInject ()
    val organizationDepartmentsAndSectionsViewModel: OrganizationDepartmentsAndSectionsViewModel = koinInject()
    App(sessionManager,authViewModel,usersViewModel,
        offlineSessionManager, connectivityObserver,
        organizationViewModel, organizationDepartmentsAndSectionsViewModel)
    }
}