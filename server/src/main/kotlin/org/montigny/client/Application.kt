package org.montigny.client


import com.typesafe.config.ConfigFactory
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.oas.OpenApiSpec
import io.bkbn.kompendium.oas.component.Components
import io.bkbn.kompendium.oas.info.Info
import io.bkbn.kompendium.oas.security.CustomBearerAuth
import io.bkbn.kompendium.oas.server.Server
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.montigny.client.config.DatabaseFactory
import org.montigny.client.config.JwtProvider
import org.montigny.client.di.appModule
import org.montigny.client.domain.api.dto.Common.AnySerializer
import org.montigny.client.domain.api.route.orgRoutes
import org.montigny.client.domain.api.route.userRoutes
import org.montigny.client.service.OrganizationService
import org.montigny.client.service.UserService
import java.io.File
import java.net.URI

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {

    println("Loaded Config Keys: ${environment.config.toMap().keys}")

    val configStream = object {}.javaClass.classLoader.getResourceAsStream("application.conf")
        ?: throw IllegalStateException("Cannot find application.conf in resources")
    val config = HoconApplicationConfig(ConfigFactory.parseReader(configStream.reader()))
    println("Loaded Config Keys: ${config.toMap().keys}") // debug

    DatabaseFactory.init(config.config("ktor"))

    val appConfig = config


    install(Koin) {
        modules(
            module {
                single<ApplicationConfig> { appConfig }
            },
            appModule
        )
    }

    install(CORS) {
        allowHost("*", schemes = listOf("http"))
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
    }
    // Content Negotiation
    install(ContentNegotiation) { json(

        Json {
            serializersModule = SerializersModule {
                contextual(Any::class, AnySerializer)
            }
        }) }
    install(NotarizedApplication()) {
        spec =
            OpenApiSpec(
                openapi = "3.0.3",
                jsonSchemaDialect = "https://json-schema.org/draft/2020-12/schema",
                info = Info(
                    title = "User API",
                    version = "1.0.0",
                    description = "API for user registration, login, approval and activation"
                ),
                servers = listOf<Server>(
                    Server(
                        url = URI("http://localhost:9090"),
                        description = "Local development server "
                    )
                ) as MutableList<Server>,
                components = Components(
                    securitySchemes = mutableMapOf(
                        "BearerAuth" to CustomBearerAuth(  type= "http", scheme = "bearer",
                            bearerFormat="JWT")
                    )
                )
            )
    }

    val jwtProvider: JwtProvider by inject()
    val userService: UserService by inject()
    val organizationService: OrganizationService by inject()

    install(Authentication) {
        jwtProvider.configureKtorAuth(this)
    }

    routing {
        staticResources("/swagger-ui", "swagger")
        // Serve your OpenAPI specification file from the root of the resources directory
        get("/openapi.json") {
            val resource = this::class.java.classLoader.getResource("openapi.json")
            if (resource == null) {
                call.respondText("openapi.json not found", status = HttpStatusCode.NotFound)
            } else {
                call.respondFile(File(resource.toURI()))
            }
        }
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        userRoutes(userService)
        orgRoutes(organizationService,userService)
        }
    }



