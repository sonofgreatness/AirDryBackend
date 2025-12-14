package org.montigny.client.domain.api.route

import io.bkbn.kompendium.core.metadata.DeleteInfo
import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.metadata.PostInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.MediaType
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.json.Json
import org.montigny.client.API_URL_RELEASE_DELETE
import org.montigny.client.API_URL_RELEASE_LOAD_ALL
import org.montigny.client.API_URL_RELEASE_LOAD_ANDROID
import org.montigny.client.API_URL_RELEASE_LOAD_BY_ID
import org.montigny.client.API_URL_RELEASE_LOAD_WINDOWS
import org.montigny.client.API_URL_RELEASE_REGISTER
import org.montigny.client.API_URL_RELEASE_UPDATE
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.IdResponse
import org.montigny.client.dto.Common.ReleaseDTO
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.service.ReleaseService
import org.montigny.client.service.UserService
import org.montigny.client.util.Constants.Companion.BEARER_AUTH_TAG
import org.montigny.client.util.Constants.Companion.DELETED_SUCCESS_DESCRIPTION
import java.util.UUID

fun Route.releaseRoutes(
   releaseService: ReleaseService, userService : UserService) {
   route(API_URL_RELEASE_REGISTER) {
      install(NotarizedRoute()) {
         tags = setOf("Release")
         security = mapOf(BEARER_AUTH_TAG to emptyList())
         post = PostInfo.builder {
            summary("Register Release")
            description("Register a release")
            request {
               requestType<ReleaseDTO>()
               description("Register a release")
               examples(
                  "application/json" to MediaType.Example(
                     value = Json.encodeToJsonElement(
                        ReleaseDTO.serializer(),
                        ReleaseDTO(
                            id = UUID.randomUUID(),
                            link = "link",
                            dateOfRelease = "disregarded",
                            platform = "ANDROID",
                        )
                     )
                  )
               )
               mediaTypes("application/json")
            }
            response {
               responseCode(HttpStatusCode.Created)
               responseType<IdResponse>()
               description("Create a release")
            }
         }
      }
      authorize(userService, UserRole.SUPER_USER) {
         post {
            try {
               val release  = call.receive<ReleaseDTO>()
               val createdId = releaseService.createRelease(release)

               call.respond(HttpStatusCode.Created, IdResponse(createdId.toString()))
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
   }
   route(API_URL_RELEASE_UPDATE) {
      install(NotarizedRoute()) {
         tags = setOf("Release")
         security = mapOf(BEARER_AUTH_TAG to emptyList())
         post = PostInfo.builder {
            summary("Update Release")
            description("Update a release")
            request {
               requestType<ReleaseDTO>()
               description("Update a release")
               examples(
                  "application/json" to MediaType.Example(
                     value = Json.encodeToJsonElement(
                        ReleaseDTO.serializer(),
                        ReleaseDTO(
                           id = UUID.randomUUID(),
                           link = "link",
                           dateOfRelease = "disregarded",
                           platform = "ANDROID",
                        )
                     )
                  )
               )
               mediaTypes("application/json")
            }
            response {
               responseCode(HttpStatusCode.OK)
               responseType<SimpleResponse>()
               description("Update a release")
            }
         }
      }
      authorize(userService, UserRole.SUPER_USER) {
         post {
            try {
               val release  = call.receive<ReleaseDTO>()
               releaseService.updateRelease(release)
               call.respond(HttpStatusCode.OK, SimpleResponse("Release Updated"))
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
   }

   route(API_URL_RELEASE_DELETE){
      install(NotarizedRoute()){
         tags = setOf("Release")
         security = mapOf(BEARER_AUTH_TAG to emptyList())
         parameters = listOf(
            Parameter(
               name = "id",
               `in` = Parameter.Location.path,
               schema = TypeDefinition.STRING))
         delete = DeleteInfo.builder {
            summary("Delete User")
            description("Deletes a user.")
            response {
               responseCode(HttpStatusCode.OK)
               responseType<SimpleResponse>()
               description(DELETED_SUCCESS_DESCRIPTION)
            }
      }
   }

      authorize(userService,UserRole.SUPER_USER) {
         delete{
            try {
               val releaseId = call.parameters["id"]?.let { UUID.fromString(it) }
                  ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

               releaseService.deleteRelease(releaseId)
               call.respond(HttpStatusCode.OK, SimpleResponse(DELETED_SUCCESS_DESCRIPTION))
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
   }


   route(API_URL_RELEASE_LOAD_BY_ID){
      install(NotarizedRoute()){
         tags = setOf("Release")
         security = mapOf(BEARER_AUTH_TAG to emptyList())
         parameters = listOf(
            Parameter(
               name = "id",
               `in` = Parameter.Location.path,
               schema = TypeDefinition.STRING))
         get = GetInfo.builder {
            summary("Delete Release")
            description("Deletes a release.")
            response {
               responseCode(HttpStatusCode.OK)
               responseType<SimpleResponse>()
               description(DELETED_SUCCESS_DESCRIPTION)
            }
         }
      }

      authorize(userService,UserRole.SUPER_USER) {
         get{
            try {
               val releaseId = call.parameters["id"]?.let { UUID.fromString(it) }
                  ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Missing id", 400))

               val release = releaseService.getReleaseById(releaseId)
               release?.let{

                  call.respond(HttpStatusCode.OK, it)
               }
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
   }
   route(API_URL_RELEASE_LOAD_ALL){
      install(NotarizedRoute()){
         tags = setOf("Release")
         security = mapOf(BEARER_AUTH_TAG to emptyList())
         get = GetInfo.builder {
            summary("Load all releases")
            description("Load all releases.")
            response {
               responseCode(HttpStatusCode.OK)
               responseType<List<ReleaseDTO>>()
               description("list of all releases")
            }
         }
      }

      authorize(userService,UserRole.SUPER_USER) {
         get{
            try {
               val releases = releaseService.getAllReleases()
               releases.let{
                  call.respond(HttpStatusCode.OK, it)
               }
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
   }



   route(API_URL_RELEASE_LOAD_WINDOWS){
      install(NotarizedRoute()){
         tags = setOf("Release")
         get = GetInfo.builder {
            summary("Load windows releases")
            description("Load windows releases.")
            response {
               responseCode(HttpStatusCode.OK)
               responseType<List<ReleaseDTO>>()
               description("list of windows releases")
            }
         }
      }

         get{
            try {
               val releases = releaseService.getAllWindowsReleases()
               releases.let{

                  call.respond(HttpStatusCode.OK, it)
               }
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }

   }


   route(API_URL_RELEASE_LOAD_ANDROID){
      install(NotarizedRoute()){
         tags = setOf("Release")
         get = GetInfo.builder {
            summary("Load android releases")
            description("Load android releases.")
            response {
               responseCode(HttpStatusCode.OK)
               responseType<List<ReleaseDTO>>()
               description("list of android releases")
            }
         }
      }

         get{
            try {
               val releases = releaseService.getAllAndroidReleases()
               releases.let{

                  call.respond(HttpStatusCode.OK, it)
               }
            } catch (e: Exception) {
               e.printStackTrace()
               call.respond(
                  HttpStatusCode.InternalServerError,
                  ErrorResponse("Unexpected server error: ${e.message}", 500)
               )
            }
         }
      }
}