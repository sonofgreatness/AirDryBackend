package org.montigny.client


import com.typesafe.config.ConfigFactory
import io.bkbn.kompendium.core.plugin.NotarizedApplication
import io.bkbn.kompendium.json.schema.definition.JsonSchema
import kotlin.reflect.typeOf
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
import io.ktor.server.request.httpMethod
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.dsl.module
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.montigny.client.config.DatabaseFactory
import org.montigny.client.config.JwtProvider
import org.montigny.client.di.appModule
import org.montigny.client.dto.Common.AnySerializer
import org.montigny.client.domain.api.route.orgRoutes
import org.montigny.client.domain.api.route.reportRoutes
import org.montigny.client.domain.api.route.userRoutes
import org.montigny.client.domain.api.route.stackRoutes
import org.montigny.client.domain.api.route.workRoutes
import org.montigny.client.service.OrganizationService
import org.montigny.client.service.ReportsService
import org.montigny.client.service.StackService
import org.montigny.client.service.UserService
import org.montigny.client.service.WorkerService
import java.io.File
import java.net.URI
import kotlin.reflect.typeOf

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
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
        allowHost("localhost:5173", schemes = listOf("http"))
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.AccessControlRequestHeaders)
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader("timezoneoffset")
        allowCredentials = true
        //remove before deploy
        anyHost()
    }
    intercept(ApplicationCallPipeline.Features) {
        if (call.request.httpMethod == HttpMethod.Options) {
            call.respond(HttpStatusCode.OK)
            finish() // stops the pipeline
        }
    }
    // Content Negotiation
    install(ContentNegotiation) { json(
        Json {
            serializersModule = SerializersModule {
                contextual(Any::class, AnySerializer)
            }
        })
    }
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
    val stackService: StackService by inject()
    val  workerService: WorkerService by inject()
    val  reportsService : ReportsService by inject()

    install(Authentication) {
        jwtProvider.configureKtorAuth(this)
    }

    routing {
        staticResources("/", "static") {
            default("index.html")
        }
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
        userRoutes(userService)
        orgRoutes(organizationService,userService)
        stackRoutes(stackService, userService)
        workRoutes(userService,workerService)
        reportRoutes(userService, reportsService)
        }
    }



