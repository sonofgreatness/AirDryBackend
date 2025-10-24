package org.montigny.client.data.api.repository

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.CreateWeeklyApprovalDTO
import org.montigny.client.dto.UpdateWeeklyApprovalDTO
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.WeeklyApprovalDTO
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

interface WorkApi {
    suspend  fun  getDailyWorkSummaries(page: Int =DEFAULT_PAGE,
                                        pageSize: Int= DEFAULT_PAGE_SIZE,
                                        filters: WorkDataFiltersDTO): PaginatedResponse<WorkDaySummaryDTO>?
    suspend  fun  createWork(req : CreateWorkDoneDTO): ApiResponse
    suspend fun getWorkById(id : UUID): CreateWorkDoneDTO?
   suspend  fun updateWork (  req:CreateWorkDoneDTO):ApiResponse

    suspend  fun getDailyWorkSummariesBetweenDates(page: Int, pageSize: Int,filters: WorkDataFiltersDTO): PaginatedResponse<WorkDaySummaryDTO>?
    suspend fun  getDailyWorkSummariesByUser(page: Int, pageSize: Int,filters: WorkDataFiltersDTO)
            : PaginatedResponse<WorkDaySummaryDTO>?


    suspend fun createWeeklyApproval(req: CreateWeeklyApprovalDTO): ApiResponse
    suspend fun updateWeeklyApproval(req: UpdateWeeklyApprovalDTO): ApiResponse
    suspend fun getWeeklyApprovalById(id: UUID): WeeklyApprovalDTO?
    suspend fun getWeeklyApprovals(page: Int, pageSize: Int): PaginatedResponse<WeeklyApprovalDTO>?
    suspend fun getLatestWeeklyApproval(): WeeklyApprovalDTO?

}