package org.montigny.client.domain.api.route

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.montigny.client.API_URL_REPORT_DASHBOARD_EXCEL
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.service.ReportsService
import org.montigny.client.service.UserService

fun Route.reportRoutes(
    userService: UserService,
    reportsService: ReportsService
){
    route(API_URL_REPORT_DASHBOARD_EXCEL ){
        install(NotarizedRoute()){
            tags = setOf("Work")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = "startDate",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING),
                Parameter(
                    name = "endDate",
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING))
            get  = GetInfo.builder  {
                summary("Get preprocessed dashboard excel file")
                description("Get preprocessed dashboard excel file")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Get preprocessed dashboard excel file")
                } }
            }
            get{
                try {
                    val startDate = call.parameters["startDate"] ?: ""
                    val endDate = call.parameters["endDate"] ?: ""
                    val bytes = reportsService.generateExcelReport(startDate, endDate)
                    call.response.header(
                        HttpHeaders.ContentDisposition,
                        ContentDisposition.Attachment.withParameter(
                            ContentDisposition.Parameters.FileName,
                            "ComprehensiveWorkDoneReport.xlsx"
                        ).toString()
                    )
                    call.respondBytes(
                        bytes,
                        contentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                    )
                }catch (e: Exception){
                    e.printStackTrace()
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        ErrorResponse("Server error: ${e.message}", 500)
                    )
                }
       }
    }
}