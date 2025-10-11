package org.montigny.client.data.api.repository

import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID

interface OrganizationApi {
    suspend fun getAllDept():List<CreateOrgDTO>
    suspend fun getAllSect(): List<CreateSectDTO>
    suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO>?
    suspend  fun getOrganizationWithNestedDepartments():List<OrganizationResponse>
    suspend fun createOrg(req: CreateOrgDTO,logo: ByteArray?): ApiResponse
    suspend fun getOrgById(orgId: UUID): GetOrgDTO
    suspend fun updateOrg(req: CreateOrgDTO,logo: ByteArray?):ApiResponse
    suspend fun  createSect(req: CreateSectDTO): ApiResponse
    suspend fun  createDepartment(req: CreateDptDTO) : ApiResponse
    suspend fun  updateDepartment(req:CreateDptDTO): ApiResponse
    suspend fun  createSection(req: CreateSectDTO): ApiResponse
    suspend fun  updateSection(req: CreateSectDTO): ApiResponse
}