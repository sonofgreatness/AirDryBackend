package org.montigny.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.montigny.client.auth.SessionManager
import org.montigny.client.di.appModule
import org.montigny.client.di.databaseModule
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel

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
       App(sessionManager,authViewModel,usersViewModel)
    }
}