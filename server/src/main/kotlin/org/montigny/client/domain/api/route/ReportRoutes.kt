package org.montigny.client.domain.api.route

import io.ktor.http.ContentDisposition
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.server.application.call
import io.ktor.server.response.header
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.montigny.client.service.ReportsService
import org.montigny.client.service.UserService

fun Route.reportRoutes(
    userService: UserService,
    reportsService: ReportsService
){
    route("/get/excel"){
       get{
           val bytes = reportsService.generateExcelReport("","")
           call.response.header(
               HttpHeaders.ContentDisposition,
               ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "ComprehensiveWorkDoneReport.xlsx").toString()
           )
           call.respondBytes(
               bytes,
               contentType = ContentType.parse("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
           )
       }
    }
}