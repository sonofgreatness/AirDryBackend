package org.montigny.client.database.daoImp

import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.DepartmentResponse
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.util.UtilityFunctions.Companion.convertBoolToLong
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.slf4j.Logger
import java.util.UUID

class OrganizationRepositoryImpl(
    private val db: AppDatabase,
    private val logger : Logger
): OrganizationRepository
{

  private val queries = db.appDatabaseQueries
  override  suspend fun getAllDepartments(): List<CreateOrgDTO>{
      return queries.getAllDeparts().executeAsList().map {

          CreateOrgDTO(
              id = it.id.toUUIDOrNull(),
              displayName =  it.displayname
          )
      }
  }
  override suspend fun getAllSections(): List<CreateSectDTO>{
        return queries.selectAllSections().executeAsList().map{
            CreateSectDTO(
                id = it.id.toUUIDOrNull(),
                displayName = it.displayname,
                departmentId = it.department_id.toUUIDOrNull()
            )
        }
    }
    override  suspend fun insertDepartment(createDptDTO: CreateDptDTO){
        queries.transaction{
            queries.insertMinimalDepartment(
                id = createDptDTO.id.toString(),
                displayname = createDptDTO.displayName,
                organization_id = createDptDTO.organizationId.toString(),
                is_pending_insert = 0L,
                is_pending_update = 0L)
        }
    }
    override  suspend fun insertSection(createSectDTO: CreateSectDTO){
        queries.transaction{
            queries.insertMinimalSection(
                id = createSectDTO.id.toString(),
                displayname = createSectDTO.displayName,
                department_id = createSectDTO.departmentId.toString(),
                is_pending_insert = 0L,
                is_pending_update = 0L,)
        }
    }
   override suspend fun deleteUnCachedSections(){
       queries.deleteUnCachedSections()
   }
    override suspend fun deleteUnCachedDepartments(){
        queries.deleteUnCachedDepartments()
    }
    override suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO> {
        return queries.selectSectionsByDepartmentId(departmentId.toString())
            .executeAsList().map {
            CreateSectDTO(
                id = it.id.toUUIDOrNull(),
                displayName = it.displayname,
                departmentId = it.department_id.toUUIDOrNull(),) }}

    override suspend fun selectOrganizationHierarchyByUserId(id: UUID): List<OrganizationResponse> {
        val rows = queries.selectOrganizationHierarchyByUserId(id.toString()).executeAsList()
        if (rows.isEmpty()) return emptyList()
        val orgMap = rows.groupBy { it.org_id }
        return orgMap.map { (orgId, orgRows) ->
            val deptMap = orgRows
                .filter { it.dept_id != null }
                .groupBy { it.dept_id }

            val departments = deptMap.map { (deptId, deptRows) ->
                val sections = deptRows
                    .filter { it.section_id != null }
                    .map { sectionRow ->
                        CreateSectDTO(
                            id = sectionRow.section_id?.toUUIDOrNull(),
                            displayName = sectionRow.section_name ?: "",
                            departmentId = deptId?.toUUIDOrNull()
                        )
                    }
                DepartmentResponse(
                    id = deptId?.toUUIDOrNull(),
                    displayName = deptRows.firstOrNull()?.dept_name ?: "",
                    sections = sections
                )
            }
            OrganizationResponse(
                id = orgId?.toUUIDOrNull(),
                name = orgRows.firstOrNull()?.org_name ?: "",
                logo = orgRows.firstOrNull()?.org_logo,
                departments = departments
            )
        }
    }
    override suspend fun insertOrganization(organization: GetOrgDTO, isCacheInsert:Boolean) {
        queries.transaction{
            queries.insertOrganization(
                id = organization.id.toString(), displayname = organization.displayName.toString(),
                logo = organization.logo, is_pending_insert = convertBoolToLong(isCacheInsert),
                is_pending_update = 0L)
        }
    }
    override suspend fun  selectCachedForInsertOrganization() : List<GetOrgDTO> =
        queries.selectCachedForInsertOrganizationInsert().executeAsList().map {
            GetOrgDTO(
                id = it.id.toUUIDOrNull(),
                displayName = it.displayname,
                logo = it.logo,
            )
        }
    override suspend fun  unCacheForInsertOrganization(id :String){
        queries.transaction{queries.unCacheUserForInsert(id)}
    }
    override suspend fun deleteUncachedOrganizations()=queries.transaction{queries.deleteUnCachedOrganization()}
    override  suspend  fun  updateOrganization(organization: GetOrgDTO){
       queries.transaction{
           queries.updateOrganization(
               id = organization.id.toString(),
               displayname = organization.displayName.toString(),
               logo = organization.logo)
       }
    }
    override suspend fun getOrgById(orgId: UUID): GetOrgDTO{
               return  queries.selectOrganizationById(orgId.toString()).executeAsOne().let {
                   GetOrgDTO(id = it.id.toUUIDOrNull(),
                       displayName = it.displayname,
                       logo = it.logo)
               }
    }

}