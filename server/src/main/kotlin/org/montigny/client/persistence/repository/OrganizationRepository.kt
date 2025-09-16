package org.montigny.client.persistence.repository

import org.montigny.client.domain.api.dto.Organization.CreateOrgDTO
import org.montigny.client.domain.api.dto.Organization.CreateDptDTO
import org.montigny.client.domain.api.dto.Organization.CreateSectDTO
import org.montigny.client.domain.api.dto.Organization.GetOrgDTO
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import java.util.UUID

interface OrganizationRepository {

    fun createOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO,logo: ByteArray?): UUID
    fun getOrgById(orgId: UUID): GetOrgDTO?
    fun updateOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO,logo: ByteArray?): Int
    fun deleteOrgById(currentUser: CurrentUserDTO,orgId: UUID): Int
    fun createDept(currentUser: CurrentUserDTO, req: CreateDptDTO) : UUID
    fun getDeptById(dptId: UUID) :CreateDptDTO?
 /*
    fun createSect(currentUser: CurrentUserDTO)

    fun updateOrg(currentUser: CurrentUserDTO)
    fun updateDept(currentUser: CurrentUserDTO)
    fun updateSect(currentUser: CurrentUserDTO)

    fun deleteOrg(currentUser: CurrentUserDTO)
    fun deleteDept(currentUser: CurrentUserDTO)
    fun deleteSect(currentUser: CurrentUserDTO)

    fun getOrgs(currentUserDTO: CurrentUserDTO)
    fun getDepts(currentUserDTO: CurrentUserDTO)
    fun getSects(currentUserDTO: CurrentUserDTO)
*/
}