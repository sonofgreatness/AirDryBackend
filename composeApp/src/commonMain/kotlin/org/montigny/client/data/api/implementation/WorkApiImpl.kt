package org.montigny.client.data.api.implementation

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import org.montigny.client.API_BASE_URL
import org.montigny.client.API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY
import org.montigny.client.API_URL_WORK_WORK_REGISTER
import org.montigny.client.API_URL_WORK_WORK_UPDATE
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

class WorkApiImpl (
    private  val client: HttpClient
): WorkApi {
    override suspend fun getDailyWorkSummaries(
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<WorkDaySummaryDTO>? {
        try{
            println("Get DailyWorkSummaries")
        val  resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY"){
            contentType(ContentType.Application.Json)
            url {
                parameters.append("page", page.toString())
                parameters.append("pageSize", pageSize.toString())
            }
            setBody(filters)
        }
        println("resp: ${resp.bodyAsText()}")
        return resp.body()
        }catch(e: Exception){
            e.printStackTrace()
            println("failed to get workSummaries ${e.message}")
            return  null
        }
    }

    override  suspend  fun  createWork(req : CreateWorkDoneDTO): ApiResponse {
        val resp = client.post("$API_BASE_URL$API_URL_WORK_WORK_REGISTER"){
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return if (resp.status.isSuccess())
            ApiResponse.Success
        else
            ApiResponse.Error(HttpStatusCode.InternalServerError.value,
                "Failed to create work ${resp.bodyAsText()}")
    }
    override suspend fun getWorkById(id : UUID): CreateWorkDoneDTO?{
        val resp = client.get("$API_BASE_URL/work/work/get_work_by_id/${id}"){
            contentType(ContentType.Application.Json)
        }.body<CreateWorkDoneDTO?>()
        return resp
    }
    override  suspend fun updateWork (  req:CreateWorkDoneDTO):ApiResponse{
        val resp = client.post("$API_BASE_URL$API_URL_WORK_WORK_UPDATE"){
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return if (resp.status.isSuccess())
            ApiResponse.Success
        else
            ApiResponse.Error(HttpStatusCode.InternalServerError.value,
                "Failed to update work ${resp.bodyAsText()}")
    }
}