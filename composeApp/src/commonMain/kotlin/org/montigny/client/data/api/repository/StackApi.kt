package org.montigny.client.data.api.repository

import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

interface StackApi {
   suspend fun createStackCode(req: CreateStackCodeDTO): ApiResponse
    suspend fun  createStack(req: CreateStackDTO): ApiResponse

    suspend fun updateStackCode(req: CreateStackCodeDTO): ApiResponse
    suspend fun  updateStack(req: CreateStackDTO): ApiResponse

    suspend fun deleteStackById(stackId: UUID): ApiResponse
    suspend fun deleteStackCodeById(stackCodeId: UUID): ApiResponse

    suspend fun getStackById(stackId: UUID): GetStackDTO?
    suspend fun  getStackCodeByID(stackCodeId: UUID): CreateStackCodeDTO?

    suspend fun  getAllStackCodes(): List<CreateStackCodeDTO>
    suspend fun  getAllStacks():List<GetStackDTO>
    suspend  fun getPaginatedStacks(page: Int,pageSize: Int,query:String? =null): PaginatedResponse<GetStackDTO>?
}