package org.montigny.client.database.dao


import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import java.util.UUID

interface OrganizationRepository {

    suspend fun getAllDepartments(): List<CreateOrgDTO>
    suspend fun getAllSections(): List<CreateSectDTO>
    suspend fun insertDepartment(createDptDTO: CreateDptDTO, isCacheInsert:Boolean = false)
    suspend fun insertSection(createSectDTO: CreateSectDTO,isCacheInsert:Boolean = false)

    suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO>?

    suspend fun deleteUnCachedSections()
    suspend fun deleteUnCachedDepartments()
    suspend fun  selectOrganizationHierarchyByUserId(id : UUID): List<OrganizationResponse>
    suspend fun insertOrganization(organization: GetOrgDTO,isCacheInsert:Boolean = false)
    suspend fun  selectCachedForInsertOrganization() : List<GetOrgDTO>
    suspend fun  selectCachedForUpdateOrganization() : List<GetOrgDTO>

    suspend fun  unCacheForInsertOrganization(id :String)
    suspend fun  unCacheForUpdateOrganization(id :String)
    suspend fun deleteUncachedOrganizations()
    suspend fun  updateOrganization(organization: GetOrgDTO)
    suspend fun getOrgById(orgId: UUID): GetOrgDTO

   suspend fun  selectCachedForInsertDepartment() : List<CreateDptDTO>
   suspend fun   unCacheForInsertDepartment(id: String)
   suspend fun  updateDepartment(createDptDTO: CreateDptDTO)
   suspend fun  unCacheForUpdateDepartment(id: String)
   suspend fun  selectCachedForUpdateDepartment() : List<CreateDptDTO>
   suspend fun  selectCachedForInsertSection() : List<CreateSectDTO>
   suspend fun  selectCachedForUpdateSect() : List<CreateSectDTO>
   suspend fun  unCacheForInsertSection(id: String)
   suspend fun  unCacheForUpdateSection(id: String)

   suspend fun  updateSection(createSectDTO: CreateSectDTO)

}