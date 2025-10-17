package org.montigny.client.persistence.repository

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import java.util.UUID

interface WorkRepository {
    fun  createWork(user: CurrentUserDTO, req : CreateWorkDoneDTO): UUID
    fun updateWork (user: CurrentUserDTO,  req:CreateWorkDoneDTO): Int
    fun deleteWorkById (user: CurrentUserDTO, id: UUID): Int
    fun  getWorkByUserId(user: CurrentUserDTO, id: UUID, page: Int,pageSize: Int): PaginatedResponse<CreateWorkDoneDTO>
    fun getWorkById(currentUserDTO: CurrentUserDTO,id : UUID): CreateWorkDoneDTO?
    fun  getAllWork(currentUserDTO: CurrentUserDTO, page: Int,pageSize: Int): PaginatedResponse<CreateWorkDoneDTO>
}