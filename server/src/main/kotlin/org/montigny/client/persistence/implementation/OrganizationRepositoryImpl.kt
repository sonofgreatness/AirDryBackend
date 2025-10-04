package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.Departments
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.Sections
import org.montigny.client.persistence.Sections.departmentId
import org.montigny.client.persistence.repository.OrganizationRepository
import org.montigny.client.util.Constants
import org.montigny.client.util.UserUtils.isSuperUser
import java.util.UUID

class OrganizationRepositoryImpl(): OrganizationRepository {
    override fun createOrg(
        currentUser: CurrentUserDTO,
        req: CreateOrgDTO,
        logo: ByteArray?
    ): UUID = transaction{
        Organizations.insertAndGetId {
            it[id] = req.id!!
            it[createdBy] = currentUser.id
            it[displayname] = req.displayName
            it[this.logo] = logo
        }.value
    }

    override  fun getOrgById(orgId: UUID): GetOrgDTO? =transaction{
        Organizations.select { Organizations.id eq orgId }
            .map { row ->
                GetOrgDTO(
                    id = row[Organizations.id].value,
                    displayName = row[Organizations.displayname],
                    logo = row[Organizations.logo]
                )
            }
            .singleOrNull() }

    override fun updateOrg(
        currentUser: CurrentUserDTO,
        req: CreateOrgDTO,
        logo: ByteArray?
    ): Int =transaction{
        checkIfBelongsToOrganization(currentUser, req.id!!)
        val currentOrg = Organizations.select { Organizations.id eq req.id }.firstOrNull()
            ?: throw IllegalArgumentException("Organization with id $id not found")
        Organizations.update({ Organizations.id eq currentOrg[Organizations.id].value }) {
            it[updatedBy] = currentUser.id
            it[displayname] = req.displayName
            logo?.let { logo -> it[this.logo] = logo }
        }
    }

    override fun deleteOrgById(currentUser: CurrentUserDTO, orgId: UUID): Int = transaction {
        checkIfBelongsToOrganization(currentUser, orgId)
        val hasDepartments = Departments.select { Departments.organizationId eq orgId }.any()
        val hasUsers = AppUsers.select { AppUsers.organizationId eq orgId }.any()
        if (hasDepartments)
            throw  IllegalArgumentException("Organization has dependant departments")
        if(hasUsers)
            throw  IllegalArgumentException("Organization has dependant users")
        Organizations.deleteWhere { Organizations.id eq orgId }
    }

    override fun createDept(currentUser:CurrentUserDTO, req: CreateDptDTO) : UUID =transaction{
        checkIfBelongsToOrganization(currentUser,req.organizationId!!)
        Departments.insertAndGetId {
            val orgId : UUID? =  req.organizationId
            it[createdBy] = currentUser.id
            it[displayname] = req.displayName
            it[id] = req.id!!
            it[organizationId] = EntityID(req.organizationId as UUID, Organizations)
        }.value
    }
    override fun getDeptById(dptId: UUID) :CreateDptDTO?= transaction{
        Departments.select {
            Departments.id eq dptId
        }.map{row ->
            CreateDptDTO(
                id = row[Departments.id].value,
                displayName = row[Departments.displayname],
                organizationId = row[Departments.organizationId].value)
        }.singleOrNull()
    }
    override fun updateDept(currentUser: CurrentUserDTO, req: CreateDptDTO): Int = transaction{
        checkIfBelongsToOrganization(currentUser, req.organizationId!!)
        val currentDept = Departments.select {Departments.id eq req.id}.firstOrNull() ?:
        throw IllegalArgumentException("Department with id ${req.id} not found")
        Departments.update(where = { Departments.id eq currentDept[Departments.id].value }) {
            it[updatedBy] = currentUser.id
            it[displayname] = req.displayName}
    }


    override fun deleteDeptById(currentUser: CurrentUserDTO, dptId: UUID) = transaction{

        Departments.select { Departments.id eq dptId }.firstOrNull()
            ?:throw IllegalArgumentException("Department with id $id not found")

        val hasSections = Sections.select { departmentId eq dptId }.any()
        if (hasSections)
               throw  IllegalArgumentException("Department has dependant sections")
        Departments.deleteWhere { Departments.id eq dptId }

    }
    override  fun createSect(currentUser: CurrentUserDTO,req: CreateSectDTO) : UUID =
    transaction{
        val departmentOrganization = Departments.select { Departments.id eq req.departmentId }.firstOrNull() ?:
        throw  IllegalArgumentException("Department with id ${req.departmentId} not found")

        val orgId = departmentOrganization[Departments.organizationId].value
        checkIfBelongsToOrganization(currentUser,orgId)
        Sections.insertAndGetId {
            it[updatedBy] = currentUser.id
            it[displayname] = req.displayName
            it[id] = req.id!!
            it[departmentId] = req.departmentId!!
        }.value

    }
   override fun updateSect(currentUser: CurrentUserDTO,req: CreateSectDTO) : Int =transaction{
        Sections.select {Sections.id eq req.id }.firstOrNull() ?: throw IllegalArgumentException("Section with id ${req.departmentId} not found")
       val departmentOrganization = Departments.select { Departments.id eq req.departmentId }.firstOrNull() ?:
       throw  IllegalArgumentException("Department-Section with id ${req.departmentId} not found")

       val orgId = departmentOrganization[Departments.organizationId].value
       checkIfBelongsToOrganization(currentUser,orgId)
       Sections.update({ Sections.id eq req.id }) {
           it[updatedBy] = currentUser.id
           it[displayname] = req.displayName
       }
   }
   override fun deleteSectById(currentUser: CurrentUserDTO,sectId: UUID) = transaction{

    val section = Sections.select { Sections.id eq sectId }.firstOrNull()
        ?:throw IllegalArgumentException("Section with id ${sectId} not found")

   val department  = Departments.select { Departments.id eq section[departmentId].value }.firstOrNull()
        ?:throw  IllegalArgumentException("Section Department not found")

    checkIfBelongsToOrganization(currentUser,department[Departments.organizationId].value)

    Sections.deleteWhere {
        Sections.id  eq sectId
    }

}
    override fun getAllOrg (currentUserDTO: CurrentUserDTO):List<CreateOrgDTO>
    =transaction {
        val isSuperUser = isSuperUser(currentUserDTO)
        if (isSuperUser){

            Organizations.selectAll().map {
                CreateOrgDTO(id = it[Organizations.id].value,
                    displayName = it[Organizations.displayname]
                )
            }
        }
            else{
            Organizations.select{
                Organizations.id eq currentUserDTO.organizationId
            }.map {
                CreateOrgDTO(id = it[Organizations.id].value,
                    displayName = it[Organizations.displayname]
                )
            }
            }
    }



    override fun getAllDept(currentUserDTO: CurrentUserDTO): List<CreateOrgDTO> =transaction {
       val isSuperUser = isSuperUser(currentUserDTO)
        println("Departments  id being used ==${currentUserDTO.organizationId}")
        if (isSuperUser){
            Departments.selectAll().map{
                CreateOrgDTO(id = it[Departments.id].value,
                    displayName = it[Departments.displayname])}
        }else{
            Departments.select{
                Departments.organizationId eq currentUserDTO.organizationId
            }.map{
                CreateOrgDTO(id = it[Departments.id].value,
                    displayName = it[Departments.displayname])
            }
        }

    }

    override fun getAllSect(currentUserDTO: CurrentUserDTO): List<CreateSectDTO> = transaction {
        val isSuperUser = isSuperUser(currentUserDTO)

        if (isSuperUser) {
            Sections
                .selectAll()
                .map {
                    CreateSectDTO(
                        id = it[Sections.id].value,
                        displayName = it[Sections.displayname],
                        departmentId =it[Sections.departmentId].value
                    )
                }
        } else {
            val orgId = currentUserDTO.organizationId
            (Sections innerJoin Departments innerJoin Organizations)
                .slice(Sections.id, Sections.displayname)
                .select { Organizations.id eq orgId }
                .map {
                    CreateSectDTO(
                        id = it[Sections.id].value,
                        displayName = it[Sections.displayname],
                        departmentId =it[Sections.departmentId].value
                    )
                }
        }
    }

    private fun checkIfBelongsToOrganization(
        currentUser: CurrentUserDTO,
        orgId: UUID
    ) {
        val currentUserOrgId = currentUser.organizationId
        if (currentUserOrgId != orgId) {
            throw IllegalArgumentException(Constants.ORGANIZATION_MISMATCH_VALIDATION_ERROR)
        }
    }

}



