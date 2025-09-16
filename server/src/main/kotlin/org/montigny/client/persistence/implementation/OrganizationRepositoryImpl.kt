package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.domain.api.dto.Organization.CreateDptDTO
import org.montigny.client.domain.api.dto.Organization.CreateOrgDTO
import org.montigny.client.domain.api.dto.Organization.GetOrgDTO
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.Departments
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.repository.OrganizationRepository
import org.montigny.client.util.Constants
import java.util.UUID

class OrganizationRepositoryImpl(): OrganizationRepository {
    override fun createOrg(
        currentUser: CurrentUserDTO,
        req: CreateOrgDTO,
        logo: ByteArray?
    ): UUID = transaction{
        Organizations.insertAndGetId {
            it[id] = req.id
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
        checkIfBelongsToOrganization(currentUser, req.id)
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
        checkIfBelongsToOrganization(currentUser,req.organizationId)
        Departments.insertAndGetId {
            it[createdBy] = currentUser.id
            it[displayname] = req.displayName
            it[id] = req.id
            it[organizationId] = req.organizationId
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



