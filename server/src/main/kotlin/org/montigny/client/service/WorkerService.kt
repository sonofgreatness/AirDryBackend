package org.montigny.client.service

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.persistence.repository.WorkRepository
import java.util.*

class WorkerService
    (private val workRepo: WorkRepository){
    fun  createWork(user: CurrentUserDTO, req : CreateWorkDoneDTO): UUID = workRepo.createWork(user, req)
    fun updateWork (user: CurrentUserDTO,  req:CreateWorkDoneDTO): Int = workRepo.updateWork(user, req)
    fun deleteWorkById (user: CurrentUserDTO, id: UUID): Int= workRepo.deleteWorkById(user, id)

    fun  getWorkByUserId(user: CurrentUserDTO, id: UUID, page: Int,pageSize: Int): PaginatedResponse<CreateWorkDoneDTO>
    = workRepo.getWorkByUserId(user, id, page, pageSize)
    fun getWorkById(currentUserDTO: CurrentUserDTO,id : UUID): CreateWorkDoneDTO?
    =workRepo.getWorkById(currentUserDTO, id)
    fun  getAllWork(currentUserDTO: CurrentUserDTO, page: Int,pageSize: Int): PaginatedResponse<CreateWorkDoneDTO>
    = workRepo.getAllWork(currentUserDTO, page, pageSize)
    fun getDailyWorkSummaries(currentUserDTO: CurrentUserDTO,
                              page: Int,pageSize: Int, filters: WorkDataFiltersDTO) =
        workRepo.getDailyWorkSummaries(currentUserDTO, page, pageSize, filters)

    fun getDailyWorkSummariesBetweenDates(
        currentUserDTO: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<WorkDaySummaryDTO> = workRepo.getDailyWorkSummariesBetweenDates(
        currentUserDTO,
        page,
        pageSize,
        filters
    )

    fun getDailyWorkSummariesByUser(
        currentUserDTO: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ) = workRepo.getDailyWorkSummariesByUser(
        currentUserDTO,
        page,
        pageSize,
        filters)


}