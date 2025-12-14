package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.montigny.client.*
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.util.stateClasses.ApiResponse
import org.slf4j.Logger
import java.util.*

class StackApiImpl(
    private val  logger: Logger,
    private val client : HttpClient
): StackApi {
    override suspend fun createStackCode(req: CreateStackCodeDTO): ApiResponse {
        try {
        val resp  = client.post("$API_BASE_URL$API_URL_WORK_STACK_CODE_REGISTER"){
            contentType(ContentType.Application.Json)
            setBody(req)
        }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to create stack code ${resp.bodyAsText()}")
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to create stack code ${e.localizedMessage}")
        }
    }
    override suspend fun createStack(req: CreateStackDTO): ApiResponse {
        try {
            val resp  = client.post("$API_BASE_URL$API_URL_WORK_STACK_REGISTER"){
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to create stack  ${resp.bodyAsText()}")
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to create stack  ${e.localizedMessage}")
        }
    }
    override suspend fun updateStackCode(req: CreateStackCodeDTO): ApiResponse {
        try {
            val resp  = client.post("$API_BASE_URL$API_URL_WORK_STACK_CODE_UPDATE"){
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to update stack code ${resp.bodyAsText()}")
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to update stack code ${e.localizedMessage}")
        }
    }
    override suspend fun updateStack(req: CreateStackDTO): ApiResponse {
        try {
            val resp  = client.post("$API_BASE_URL$API_URL_WORK_STACK_UPDATE"){
                contentType(ContentType.Application.Json)
                setBody(req)
            }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to update stack ${resp.bodyAsText()}")
        } catch (e: Exception) {
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to update stack ${e.localizedMessage}")
        }
    }
    override suspend fun deleteStackById(stackId: UUID): ApiResponse {
        try {
            val resp  = client.delete("$API_BASE_URL/work/stack/delete/${stackId}"){
                contentType(ContentType.Application.Json)
            }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to delete stack ${resp.bodyAsText()}")
        }catch (e:Exception){
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to delete stack  ${e.localizedMessage}")
        }
    }
    override suspend  fun deleteStackCodeById(stackCodeId: UUID): ApiResponse {
        try {
            val resp  = client.delete("$API_BASE_URL/work/stack/stack_code/delete/${stackCodeId}"){
                contentType(ContentType.Application.Json)
            }
            return if (resp.status.isSuccess())
                ApiResponse.Success
            else
                ApiResponse.Error(501, "failed to delete stack ${resp.bodyAsText()}")
        }catch (e:Exception){
            logger.error(e.localizedMessage)
            return ApiResponse.Error(500, "failed to delete stack  ${e.localizedMessage}")
        }
    }
    override suspend  fun getStackById(stackId: UUID): GetStackDTO? {
        val response: GetStackDTO?  = client.get ("$API_BASE_URL/work/stack/stack/${stackId}"){
            contentType(ContentType.Application.Json)
        }.body()
      return  response
    }
    override suspend fun getStackCodeByID(stackCodeId: UUID): CreateStackCodeDTO? {
        val response: CreateStackCodeDTO?  = client.get ("$API_BASE_URL/work/stack/stack_code/get/${stackCodeId}"){
            contentType(ContentType.Application.Json)
        }.body()
        return  response
    }
    override suspend fun getAllStackCodes(): List<CreateStackCodeDTO> {
        val response: List<CreateStackCodeDTO>  = client.get ("$API_BASE_URL$API_URL_WORK_STACK_CODE_GET_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return  response
    }
    override  suspend  fun getAllStacks(): List<GetStackDTO> {
        val response: List<GetStackDTO> = client.get ("$API_BASE_URL$API_URL_WORK_STACK_GET_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return  response
    }
    override  suspend  fun getPaginatedStacks(page: Int,pageSize: Int,query:String?): PaginatedResponse<GetStackDTO>?{
        val resp :PaginatedResponse<GetStackDTO>? = client.get("$API_BASE_URL$API_URL_WORK_STACK_GET_ALL_PAGINATED"){
            url {
                parameters.append(QUERY_PAGE, page.toString())
                parameters.append(QUERY_PAGE_SIZE, pageSize.toString())
                query?.let { parameters.append(QUERY_STRING, it) }
            }
        }.body()
        return  resp
    }
}