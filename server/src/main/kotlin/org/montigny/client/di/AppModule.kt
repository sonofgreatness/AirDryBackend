package org.montigny.client.di

import io.ktor.server.config.*
import org.koin.dsl.module
import org.montigny.client.config.DatabaseFactory
import org.montigny.client.config.JwtProvider
import org.montigny.client.persistence.implementation.BCryptPasswordHasher
import org.montigny.client.persistence.implementation.OrganizationRepositoryImpl
import org.montigny.client.persistence.implementation.StackRepositoryImpl
import org.montigny.client.persistence.implementation.UserRepositoryImpl
import org.montigny.client.persistence.repository.OrganizationRepository
import org.montigny.client.persistence.repository.PasswordHasher
import org.montigny.client.persistence.repository.StackRepository
import org.montigny.client.persistence.repository.UserRepository
import org.montigny.client.service.OrganizationService
import org.montigny.client.service.StackService
import org.montigny.client.service.UserService

val appModule = module {
    single<PasswordHasher> { BCryptPasswordHasher() }
    // Repository
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<OrganizationRepository> { OrganizationRepositoryImpl()}
    single <StackRepository>{ StackRepositoryImpl()}
    // JWT
    single {
        val config = get<ApplicationConfig>().config("ktor.jwt")
        JwtProvider(
            secret = config.property("secret").getString(),
            issuer = config.property("issuer").getString(),
            audience = config.property("audience").getString(),
            expirySeconds = config.property("expirySeconds").getString().toLong()
        )
    }
    // Service
    single { UserService(get(), get()) }
    single { OrganizationService(get()) }
    single {StackService(get()) }
}
