// src/services/UserService.ts
import axiosInstance from "../util/axios";
import { type PaginatedResponse } from "../util/types/common";
import {
  type UserResponse,
  type UserRegisterRequest,
  type UpdateUserDTO,
  type UserTypesDTO
} from "../util/types/user";

export const UserService = {
  // Default get all users
  async getUsers(page: number, pageSize: number): Promise<PaginatedResponse<UserResponse>> {
    const { data } = await axiosInstance.get<PaginatedResponse<UserResponse>>(
      "/users/get_all_users",
      { params: { page, pageSize } }
    );
    return data;
  },

  // New function: get users with optional filters
  async getUsersWithFilters(
    page: number,
    pageSize: number,
    filters?: Partial<Record<"name" | "surname" | "email" | "phoneNumber" | "organizationId" | "departmentId"
      |"sectionId"|"userTypeId"
      , string>>
  ): Promise<PaginatedResponse<UserResponse>> {
    const hasFilters = filters && Object.values(filters).some((v) => v && v.trim() !== "");
    
    if (!hasFilters) {
      // fallback to get_all_users if no filters
      return this.getUsers(page, pageSize);
    }

    const params: any = { page, pageSize };
    if (filters?.name) params.name = filters.name;
    if (filters?.surname) params.surname = filters.surname;
    if (filters?.email) params.email = filters.email;
    if (filters?.phoneNumber) params.phoneNumber = filters.phoneNumber;
   if(filters?.organizationId) params.organizationId =filters.organizationId
    if (filters?.departmentId) params.departmentId = filters.departmentId
    if (filters?.sectionId) params.sectionId = filters.sectionId
    if (filters?.userTypeId) params.userTypeId = filters.userTypeId
  


    const { data } = await axiosInstance.get<PaginatedResponse<UserResponse>>(
      "/users/search_users", // NEW Ktor endpoint for filtered search
      { params }
    );
    return data;
  },

  async registerUser(req: UserRegisterRequest) {
    const { data } = await axiosInstance.post("/users/register", req);
    return data;
  },

  async updateUser(id: string, req: UpdateUserDTO) {
    const { data } = await axiosInstance.put(`/users/${id}/update`, req);
    return data;
  },

  async deleteUser(id: string) {
    const { data } = await axiosInstance.delete(`/users/${id}/delete`);
    return data;
  },

  async getUserTypes(): Promise<UserTypesDTO[]> {
    const { data } = await axiosInstance.get("/users/get_all_user_types");
    return data;
  },
  //New method use to preload Edit data 
  async getUser(id :string): Promise<UserRegisterRequest> { 
    const { data } = await axiosInstance.get(`/user/${id}/get_by_id`);
    return data; 
  },
};

