package org.montigny.client.di

import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.montigny.client.data.AppStorage
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.implementation.UserApiImpl
import org.montigny.client.data.api.implementation.provideHttpClient
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.slf4j.Logger
import org.slf4j.LoggerFactory



val appModule = module {
    single<Logger> { LoggerFactory.getLogger("DesktopApp") }
    single<AppStorage>{AppStorage()}
    single {SessionManager(get(),get())}



    single <HttpClient>{provideHttpClient(get())}
    single<UserApi> {UserApiImpl(get(), get(), get ())}
    single {AuthViewModel(get(),get(),get()) }
    single { UserViewModel(get(), get(), get()) }

}
