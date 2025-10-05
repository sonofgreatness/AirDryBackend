package org.montigny.client.database.dao


import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import java.util.UUID

interface OrganizationRepository {

    suspend fun getAllDepartments(): List<CreateOrgDTO>
    suspend fun getAllSections(): List<CreateSectDTO>
    suspend fun insertDepartment(createDptDTO: CreateDptDTO)
    suspend fun insertSection(createSectDTO: CreateSectDTO)
    suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO>?


}