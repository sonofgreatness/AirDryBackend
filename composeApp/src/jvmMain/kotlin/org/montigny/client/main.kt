package org.montigny.client

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.compose.koinInject
import org.koin.core.context.startKoin
import org.montigny.client.auth.SessionManager
import org.montigny.client.di.appModule
import org.montigny.client.di.databaseModule
import org.montigny.client.ui.users.viewModels.AuthViewModel

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "IMS (GUNDANE@MONTIGNY)",
    ) {
        startKoin {
            modules(appModule, databaseModule)
        }
       val sessionManager: SessionManager = koinInject()
       val authViewModel: AuthViewModel = koinInject()
       App(sessionManager,authViewModel)
    }
}