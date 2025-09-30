package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.montigny.client.auth.SessionManager
import io.ktor.client.plugins.logging.*

fun provideHttpClient(sessionManager: SessionManager): HttpClient {
    return HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }

        install(DefaultRequest) {
            // Attach JWT header if available
            sessionManager.jwtToken?.let { token ->
                headers.append(HttpHeaders.Authorization,
                    "Bearer $token") } }


        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.DEFAULT
        }

    }
}