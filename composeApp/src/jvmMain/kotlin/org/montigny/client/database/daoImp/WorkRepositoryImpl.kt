package org.montigny.client.database.daoImp

import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.PasswordHasher
import org.montigny.client.database.dao.WorkRepository
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.slf4j.Logger

class WorkRepositoryImpl(
    private val db: AppDatabase,
    private val offlineSessionManager: OfflineSessionManager,
    private val logger : Logger,
    private val sessionManager: SessionManager,

    ): WorkRepository {
  override  suspend  fun  getDailyWorkSummaries(page: Int,
                                                pageSize: Int,
                                                filters: WorkDataFiltersDTO): PaginatedResponse<WorkDaySummaryDTO>?
  {
      return  getDailyWorkSummaries(page, pageSize,filters)
  }
}