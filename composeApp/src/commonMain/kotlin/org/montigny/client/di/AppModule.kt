package org.montigny.client.di

import org.koin.dsl.module
import org.montigny.client.data.AppStorage
import org.montigny.client.auth.SessionManager

val appModule = module {
    single<AppStorage> { AppStorage() }
    single { SessionManager(get()) }
}