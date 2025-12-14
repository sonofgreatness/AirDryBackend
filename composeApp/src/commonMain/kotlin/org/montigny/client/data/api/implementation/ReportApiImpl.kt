package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import org.montigny.client.API_BASE_URL
import org.montigny.client.API_URL_PAYROLL_DATA_DASHBOARD_EXCEL
import org.montigny.client.API_URL_REPORT_DASHBOARD_EXCEL
import org.montigny.client.END_DATE
import org.montigny.client.PAGE
import org.montigny.client.PAGE_SIZE
import org.montigny.client.START_DATE
import org.montigny.client.data.api.repository.ReportApi

class ReportApiImpl(private val client : HttpClient): ReportApi {
  override suspend fun getDashBoardExcelReport(startDate: String,endDate: String) : ByteArray?  =
       client.get("$API_BASE_URL$API_URL_REPORT_DASHBOARD_EXCEL"){
          url {
              parameters.append(START_DATE,startDate)
              parameters.append(END_DATE, endDate)
          }
      }.body<ByteArray?>()
    override  suspend fun  getPayRollDataReport(startDate: String,endDate: String) : ByteArray?=
        client.get("$API_BASE_URL$API_URL_PAYROLL_DATA_DASHBOARD_EXCEL"){
             url {
                 parameters.append(START_DATE,startDate)
                 parameters.append(END_DATE, endDate)
             }
         }.body<ByteArray?>()
}