package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.montigny.client.*
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.CreateWeeklyApprovalDTO
import org.montigny.client.dto.UpdateWeeklyApprovalDTO
import org.montigny.client.dto.WeeklyApprovalDTO
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
           val  resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_GET_ALL_DAILY_SUMMARY_BETWEEN_DATES_BY_USER"){
               contentType(ContentType.Application.Json)
               url {
                   parameters.append(PAGE, page.toString())
                   parameters.append(PAGE_SIZE, pageSize.toString())
               }
               setBody(filters)
           }
           return resp.body()
       }catch(e: Exception){
           e.printStackTrace()
           return  null
       }
    }
   override suspend fun createWeeklyApproval(req: CreateWeeklyApprovalDTO): ApiResponse{
       val resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_WEEKLY_APPROVAL_REGISTER"){
           contentType(ContentType.Application.Json)
            setBody(req)
       }
       return if (resp.status.isSuccess())
           ApiResponse.Success
       else
           ApiResponse.Error(HttpStatusCode.InternalServerError.value,
               "Failed to create weekly approval: ${resp.bodyAsText()}")
   }
    override suspend  fun  updateWeeklyApproval(req: UpdateWeeklyApprovalDTO): ApiResponse{
        val resp  = client.post("$API_BASE_URL$API_URL_WORK_WORK_WEEKLY_APPROVAL_UPDATE"){
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        return if (resp.status.isSuccess())
            ApiResponse.Success
        else
            ApiResponse.Error(HttpStatusCode.InternalServerError.value,
                "Failed to update weekly approval: ${resp.bodyAsText()}")
    }

  override  suspend fun getWeeklyApprovalById(id: UUID): WeeklyApprovalDTO?{
      val resp = client.get("$API_BASE_URL/work/weekly/get/${id}"){
          contentType(ContentType.Application.Json)
      }.body<WeeklyApprovalDTO?>()
      return resp
    }
   override suspend fun getWeeklyApprovals(page: Int, pageSize: Int): PaginatedResponse<WeeklyApprovalDTO>?{
       try{
           val  resp  = client.post("$API_BASE_URL$API_URL_WORK_WEEKLY_GET_ALL"){
               contentType(ContentType.Application.Json)
               url {
                   parameters.append(PAGE, page.toString())
                   parameters.append(PAGE_SIZE, pageSize.toString())
               }
           }
           return resp.body()
       }catch(e: Exception){
           e.printStackTrace()
           return  null
       }
    }
   override  suspend fun getLatestWeeklyApproval(): WeeklyApprovalDTO?{
       val resp = client.get("$API_BASE_URL$API_URL_WORK_WEEKLY_GET_LATEST"){
           contentType(ContentType.Application.Json)
       }.body<WeeklyApprovalDTO?>()
       return resp
   }

}