package org.montigny.client.domain.api.route

import io.bkbn.kompendium.core.metadata.GetInfo
import io.bkbn.kompendium.core.plugin.NotarizedRoute
import io.bkbn.kompendium.json.schema.definition.TypeDefinition
import io.bkbn.kompendium.oas.payload.Parameter
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.montigny.client.API_URL_PAYROLL_DATA_DASHBOARD_EXCEL
import org.montigny.client.API_URL_REPORT_DASHBOARD_EXCEL
import org.montigny.client.API_URL_WORK_WEEKLY_GET_LATEST_APPROVED
import org.montigny.client.END_DATE
import org.montigny.client.START_DATE
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.domain.api.route.auth.authorize
import org.montigny.client.dto.Common.ErrorResponse
import org.montigny.client.dto.Common.SimpleResponse
import org.montigny.client.service.ReportsService
import org.montigny.client.service.TrackingService
import org.montigny.client.service.UserService
import org.montigny.client.util.UserUtils

fun Route.reportRoutes(
    userService: UserService,
    reportsService: ReportsService,
    trackingService: TrackingService
){
    route (API_URL_PAYROLL_DATA_DASHBOARD_EXCEL){
        install(NotarizedRoute())
        {
            tags = setOf("Report")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = START_DATE,
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING),
                Parameter(
                    name = END_DATE,
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING))
            get  = GetInfo.builder  {
                summary("Get preprocessed payroll excel file")
                description("Get preprocessed payroll excel file")
                response {
                    responseCode(HttpStatusCode.OK)
                    responseType<SimpleResponse>()
                    description("Get preprocessed payroll excel file")
                }
            }
        }
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER){
        get {


            val user = UserUtils.getLoggedInUser(call)
            try {
                val userId = user.id
                trackingService.logOrUpdateDailyAccess(
                    userId!!,
                    API_URL_PAYROLL_DATA_DASHBOARD_EXCEL
                )
            } catch (e: Exception) {
                call.application.log.error("Failed to track access for user: ${user.id.toString()} on route $API_URL_PAYROLL_DATA_DASHBOARD_EXCEL", e)
            }

           try {
                val startDate = call.parameters[START_DATE] ?: ""
                val endDate = call.parameters[END_DATE] ?: ""
                val bytes = reportsService.generateWorkerPayrollReport(startDate, endDate)
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(
                        ContentDisposition.Parameters.FileName,
                        "PayRoll_Data.xlsx"
                    ).toString()
                )
                call.respondBytes(
                    bytes,
                    contentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
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
    route(API_URL_REPORT_DASHBOARD_EXCEL ){
        install(NotarizedRoute()){
            tags = setOf("Report")
            security = mapOf("BearerAuth" to emptyList())
            parameters = listOf(
                Parameter(
                    name = START_DATE,
                    `in` = Parameter.Location.query,
                    schema = TypeDefinition.STRING),
                Parameter(
                    name = END_DATE,
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
        authorize(userService, UserRole.ADMIN, UserRole.SUPER_USER){
        get{

            val user = UserUtils.getLoggedInUser(call)
            try {
                val userId = user.id
                trackingService.logOrUpdateDailyAccess(
                    userId!!,
                    API_URL_REPORT_DASHBOARD_EXCEL
                )
            } catch (e: Exception) {
                call.application.log.error("Failed to track access for user: ${user.id.toString()} on route $API_URL_REPORT_DASHBOARD_EXCEL", e)
            }
                try {
                    val startDate = call.parameters[START_DATE] ?: ""
                    val endDate = call.parameters[END_DATE] ?: ""
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
                    ) }}
        }
    }
}