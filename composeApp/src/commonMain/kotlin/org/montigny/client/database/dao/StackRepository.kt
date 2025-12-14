package org.montigny.client.database.dao

import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

interface StackRepository {

     suspend fun createStackCode(req: CreateStackCodeDTO, isCached : Boolean = false)
     suspend fun createStack(req: CreateStackDTO, isCached : Boolean = false)
    suspend fun getAllStackCodes(): List<CreateStackCodeDTO>
    suspend fun getAllStacks():List<GetStackDTO>

    suspend fun getStacksCachedForInsert():List<CreateStackDTO>
    suspend fun getStackCodesCachedForInsert():List<CreateStackCodeDTO>
    suspend fun unCacheStackInsert(id: UUID)
    suspend fun  unCacheStackCodeInsert(id: UUID)
    suspend fun  deleteUnCachedStacks()
    suspend fun deleteUnCachedStackCodes()

    suspend fun  getStackById(id: UUID): GetStackDTO
    suspend fun updateStack(createStackDTO: CreateStackDTO)
    suspend fun  updateStackCode(createStackCodeDTO: CreateStackCodeDTO)

    suspend fun getStacksCachedForUpdate():List<CreateStackDTO>
    suspend fun getStackCodesCachedForUpdate():List<CreateStackCodeDTO>
    suspend fun unCacheStackUpdate(id: UUID)
    suspend fun  unCacheStackCodeUpdate(id: UUID)
}