package org.montigny.client.database.daoImp

import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
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

    override suspend fun getSectionByDepartmentId(departmentId: UUID): List<CreateSectDTO> {
        return queries.selectSectionsByDepartmentId(departmentId.toString())
            .executeAsList().map {
            CreateSectDTO(
                id = it.id.toUUIDOrNull(),
                displayName = it.displayname,
                departmentId = it.department_id.toUUIDOrNull(),)
            }
    }


}