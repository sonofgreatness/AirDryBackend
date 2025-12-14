package org.montigny.client.database.dao

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE

interface WorkRepository {

    suspend  fun  getDailyWorkSummaries(page: Int =DEFAULT_PAGE,
                                        pageSize: Int= DEFAULT_PAGE_SIZE,
                                        filters: WorkDataFiltersDTO): PaginatedResponse<WorkDaySummaryDTO>?

}