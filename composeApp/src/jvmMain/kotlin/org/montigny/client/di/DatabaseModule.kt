package org.montigny.client.di

import org.koin.dsl.module
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.DatabaseFactory
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.database.daoImp.UserRepositoryImpl

val databaseModule = module {
    single<AppDatabase> { DatabaseFactory.database }
    single <UserRepository>{UserRepositoryImpl(get()) }
}
