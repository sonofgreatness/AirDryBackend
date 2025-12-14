package org.montigny.client.di

import org.koin.dsl.module
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.BCryptPasswordHasher
import org.montigny.client.database.DatabaseFactory
import org.montigny.client.database.PasswordHasher
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.database.dao.WorkRepository
import org.montigny.client.database.daoImp.OrganizationRepositoryImpl
import org.montigny.client.database.daoImp.StackRepositoryImpl
import org.montigny.client.database.daoImp.UserRepositoryImpl
import org.montigny.client.database.daoImp.WorkRepositoryImpl

val databaseModule = module {
    single<PasswordHasher> { BCryptPasswordHasher() }
    single<AppDatabase> { DatabaseFactory.database }
    single <UserRepository>{UserRepositoryImpl(get(),get(), get(),get(),
        get())}
    single<OrganizationRepository>{ OrganizationRepositoryImpl(get(), get()) }
    single<StackRepository>{ StackRepositoryImpl(get(), get()) }
    single<WorkRepository>{ WorkRepositoryImpl(get(), get(),
    get(), get())}


}
