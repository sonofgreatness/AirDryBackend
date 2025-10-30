package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.montigny.client.API_URL_REPORT_DASHBOARD_EXCEL
import org.montigny.client.PAGE
import org.montigny.client.PAGE_SIZE
import org.montigny.client.data.api.repository.ReportApi

class ReportApiImpl(private val client : HttpClient): ReportApi {
  override suspend fun getDashBoardExcelReport(startDate: String,endDate: String) : ByteArray?  =
       client.get(API_URL_REPORT_DASHBOARD_EXCEL){
          url {
              parameters.append("startDate",startDate)
              parameters.append("endDate", endDate)
          }
      }.body<ByteArray?>()
}