package org.montigny.client.di

import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import org.koin.dsl.module
import org.montigny.client.AppWorkerManager
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionExpiryRepository
import org.montigny.client.auth.SessionExpiryWorker
import org.montigny.client.data.AppStorage
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.implementation.OrganizationApiImpl
import org.montigny.client.data.api.implementation.StackApiImpl
import org.montigny.client.data.api.implementation.UserApiImpl
import org.montigny.client.data.api.implementation.provideHttpClient
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.domain.synchronization.BackgroundSyncWorker
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.domain.synchronization.SyncSettingsRepository
import org.montigny.client.ui.Theme.ThemeManager
import org.montigny.client.ui.organizations.viewModels.OrganizationDepartmentsAndSectionsViewModel
import org.montigny.client.ui.organizations.viewModels.OrganizationViewModel
import org.montigny.client.ui.settings.stacks.viewModel.StacksViewModel
import org.montigny.client.ui.users.viewModels.AuthViewModel
import org.montigny.client.ui.users.viewModels.UserViewModel
import org.montigny.client.util.ConnectivityObserver
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val appModule = module {
    single <Settings>{Settings() }
    single<Logger> { LoggerFactory.getLogger("DesktopApp") }
    single<AppStorage>{AppStorage()}
    single {SessionManager(get(),get())}
    single { OfflineSessionManager(get()) }


    single <HttpClient>{provideHttpClient(get())}
    single<UserApi> {UserApiImpl(get(), get(),get(), get ())}
    single<OrganizationApi>{ OrganizationApiImpl(get(), get())}
    single <ConnectivityObserver>{ ConnectivityObserver(get(), get()) }

    single<SyncManager>{SyncManager(get(), get(), get(),
        get(), get(), get(),
        get(), get(),get()
        ,get()
        ) }
    single {AuthViewModel(get(),get(),
        get(),
        get(), get(),get()) }

    single { UserViewModel(
        get(),
        get(), get(), get(),
        get(), get(),get(), get()
    ) }

    single<OrganizationViewModel> { OrganizationViewModel(get(), get(), get()
    ,get(), get(),
        get(), get()) }
single <OrganizationDepartmentsAndSectionsViewModel> {
    OrganizationDepartmentsAndSectionsViewModel(
        get(), get(),
        get(), get(), get(), get(), get()
    )
}
single<SyncSettingsRepository> { SyncSettingsRepository(get())}
single <BackgroundSyncWorker>{BackgroundSyncWorker(get(), get())}
single<SessionExpiryRepository>{SessionExpiryRepository(get())}
single<SessionExpiryWorker>{SessionExpiryWorker(get(), get(),get())}
single {BackgroundSyncWorker(get(), get()) }
single<AppWorkerManager> { AppWorkerManager(get(), get()) }
single <ThemeManager>{ThemeManager()}

single<StackApi>{ StackApiImpl(get(), get())}
single<StacksViewModel>{StacksViewModel(get(), get(), get(),
    get(),get())}
}
