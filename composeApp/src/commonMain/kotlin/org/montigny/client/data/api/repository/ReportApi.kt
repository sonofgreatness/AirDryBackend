package org.montigny.client.data.api.repository

interface ReportApi {
     suspend  fun getDashBoardExcelReport(startDate: String,endDate: String) : ByteArray?
     suspend fun  getPayRollDataReport(startDate: String,endDate: String) : ByteArray?

}