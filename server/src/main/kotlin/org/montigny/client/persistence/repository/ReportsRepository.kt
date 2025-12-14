package org.montigny.client.persistence.repository

import org.montigny.client.Report.AllReportData
import org.montigny.client.Report.FactWorkDoneByUserDTO
import org.montigny.client.Report.GetMonthlyReportDTO
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.persistence.MonthlyReport
import java.io.File
import java.time.Instant
import java.util.UUID

interface ReportsRepository {

    fun getFactWorkDoneByUser(startDate: String, endDate: String): List<FactWorkDoneByUserDTO>
    fun generateAllReportData(
        startDate: String,
        endDate: String,
    ): AllReportData

    fun  createMonthlyReport(user: CurrentUserDTO,
    fileName:String,
    fileData:ByteArray) : UUID

    fun  getMonthlyReportById(user: CurrentUserDTO, id : UUID): GetMonthlyReportDTO?
    fun getAllMonthlyReports(user: CurrentUserDTO, page :Int, pageSize :Int) : PaginatedResponse<GetMonthlyReportDTO>
}