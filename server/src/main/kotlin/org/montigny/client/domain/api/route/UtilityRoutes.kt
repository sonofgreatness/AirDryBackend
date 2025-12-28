package org.montigny.client.domain.api.route

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.montigny.client.*
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.service.VisitorTrackingService

fun Route.utilityroutes(visitorTrackingService: VisitorTrackingService) {


    route(API_HEALTH){
        install(NotarizedRoute()){
            tags = setOf("Utility")
            get = GetInfo.builder {
                summary("Get health check")
                description("Health check")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Ok for good server health")
                }
            }
        }
        get{
            call.respond(HttpStatusCode.OK, SimpleResponse(SERVER_OK_TEXT))
        }
    }

    route(API_HEALTH_HALA){
        install(NotarizedRoute()){
            tags = setOf("Utility")
            get = GetInfo.builder {
                summary("Get hala @ me")
                description("Get hala @ me")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Ok for good server health")
                }
            }
        }
        get{



            val ipAddress = call.clientIp()
            val routePath = API_HEALTH_HALA
            visitorTrackingService.logOrUpdateDailyTraffic(ipAddress, routePath)
            call.respond(HttpStatusCode.OK, SimpleResponse(SERVER_OK_TEXT))
        }
    }

    route(API_HEALTH_HALA_GALLERY){
        install(NotarizedRoute()){
            tags = setOf("Utility")
            get = GetInfo.builder {
                summary("Get hala @ me from gallery ")
                description("Get hala @ me from gallery ")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Ok for good server health")
                }
            }
        }
        get{



            val ipAddress = call.clientIp()
            val routePath = API_HEALTH_HALA_GALLERY
            visitorTrackingService.logOrUpdateDailyTraffic(ipAddress, routePath)
            call.respond(HttpStatusCode.OK, SimpleResponse(SERVER_OK_TEXT))
        }
    }
    route(API_HEALTH_HALA_RELEASES){
        install(NotarizedRoute()){
            tags = setOf("Utility")
            get = GetInfo.builder {
                summary("Get hala @ me from releases ")
                description("Get hala @ me from releases ")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Ok for good server health")
                }
            }
        }
        get{



            val ipAddress = call.clientIp()
            val routePath = API_HEALTH_HALA_RELEASES
            visitorTrackingService.logOrUpdateDailyTraffic(ipAddress, routePath)
            call.respond(HttpStatusCode.OK, SimpleResponse(SERVER_OK_TEXT))
        }
    }
}

fun ApplicationCall.clientIp(): String {
    val xForwardedFor = request.headers["X-Forwarded-For"]
    val xRealIp = request.headers["X-Real-IP"]

    return when {
        !xForwardedFor.isNullOrBlank() ->
            xForwardedFor.split(",").first().trim()
        !xRealIp.isNullOrBlank() ->
            xRealIp
        else ->
            request.origin.remoteHost
    }
}