package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.montigny.client.*
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.*

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

   override suspend  fun getDailyWorkSummariesBetweenDates(page: Int, pageSize: Int,filters: WorkDataFiltersDTO): PaginatedResponse<WorkDaySummaryDTO>?
    {
        try{
            println("Get DailyWorkSummaries between dates")
            val  resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY_BETWEEN_DATES"){
                contentType(ContentType.Application.Json)
                url {
                    parameters.append(PAGE, page.toString())
                    parameters.append(PAGE_SIZE, pageSize.toString())
                }
                setBody(filters)
            }
            println("resp: ${resp.bodyAsText()}")
            return resp.body()
        }catch(e: Exception){
            e.printStackTrace()
            println("failed to get workSummaries  between dates${e.message}")
            return  null
        }
    }
   override suspend fun  getDailyWorkSummariesByUser(page: Int,
                                             pageSize: Int,filters: WorkDataFiltersDTO)
            : PaginatedResponse<WorkDaySummaryDTO>?{
       try{
           println("Get DailyWorkSummaries between dates for  user")
           val  resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY_BETWEEN_DATES_BY_USER"){
               contentType(ContentType.Application.Json)
               url {
                   parameters.append(PAGE, page.toString())
                   parameters.append(PAGE_SIZE, pageSize.toString())
               }
               setBody(filters)
           }
           println("resp: ${resp.bodyAsText()}")
           return resp.body()
       }catch(e: Exception){
           e.printStackTrace()
           println("failed to get workSummaries  between dates for user ${e.message}")
           return  null
       }
    }
}