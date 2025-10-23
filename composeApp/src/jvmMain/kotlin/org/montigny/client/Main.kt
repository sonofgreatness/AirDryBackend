package org.montigny.client

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.di.appModule
import org.montigny.client.di.databaseModule
import org.montigny.client.domain.synchronization.SyncSettingsRepository
import org.montigny.client.ui.Theme.ThemeManager
import org.montigny.client.ui.organizations.viewModels.OrganizationDepartmentsAndSectionsViewModel
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.ui.work.viewModel.CreateAndUpdateWorkViewModel
import org.montigny.client.ui.work.viewModel.LoadUserWorkViewModel
import org.montigny.client.ui.work.viewModel.LoadWorkViewModel
import org.montigny.client.util.ConnectivityObserver

object Main {
    @JvmStatic
    fun main(args: Array<String>) = application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "IMS (GUNDANE@MONTIGNY)",
        ) {
            startKoin { modules(databaseModule, appModule) }

            val sessionManager: SessionManager = koinInject()
            val authViewModel: AuthViewModel = koinInject()
            val usersViewModel: UserViewModel = koinInject()
            val offlineSessionManager: OfflineSessionManager = koinInject()
            val connectivityObserver: ConnectivityObserver = koinInject()
            val organizationViewModel: OrganizationViewModel = koinInject()
            val organizationDepartmentsAndSectionsViewModel: OrganizationDepartmentsAndSectionsViewModel = koinInject()
            val syncSettingsRepository: SyncSettingsRepository = koinInject()
            val appWorkerManager: AppWorkerManager = koinInject()
            val themeManager: ThemeManager = koinInject()
            val stacksViewModel: StacksViewModel = koinInject()
            val loadWorkViewModel: LoadWorkViewModel = koinInject()
            val createAndUpdateWorkViewModel: CreateAndUpdateWorkViewModel = koinInject()
            val  loadUserWorkViewModel : LoadUserWorkViewModel = koinInject()
            LaunchedEffect(Unit) {
                appWorkerManager.startAllWorkers()
            }
            App(
                sessionManager, authViewModel, usersViewModel,
                offlineSessionManager, connectivityObserver,
                organizationViewModel, organizationDepartmentsAndSectionsViewModel,
                syncSettingsRepository, themeManager, stacksViewModel,
                loadWorkViewModel, createAndUpdateWorkViewModel,
                loadUserWorkViewModel)
        }
    }
}