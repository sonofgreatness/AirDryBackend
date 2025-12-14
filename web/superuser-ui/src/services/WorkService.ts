import axiosInstance from "../util/axios";
import { type GetStackDTO, type PaginatedResponse, type WeeklyApprovalDTO, type WorkDaySummaryDTO } from "../util/types/common";
import {DEFAULT_PAGE_SIZE} from "../util/constants"
 export const  WorkService={ 

    async getWork(
  page: number = 1,
  pageSize: number = DEFAULT_PAGE_SIZE,
  startDate?: string | null,
  endDate?: string | null
): Promise<PaginatedResponse<WorkDaySummaryDTO>> {
  
  const params: Record<string, any> = { page, pageSize };

  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  console.log("parameters",JSON.stringify(params))
  const { data } = await axiosInstance.get<
    PaginatedResponse<WorkDaySummaryDTO>
  >(
    "/work/work/daily_summary/between_dates/by_user/approved",
    { params }
  );

  return data;
}, 
       async getLatestApprovedWeek(): Promise<WeeklyApprovalDTO|undefined> { 
          const { data } = await axiosInstance.get<WeeklyApprovalDTO|undefined>(
      "/work/weekly/latest/approved");
           return data;
     },
     async getAllStacks(): Promise<GetStackDTO[]> {
         const { data } = await axiosInstance.get<GetStackDTO[]>("/work/stack/get_all")
         return data;
      }   
 }