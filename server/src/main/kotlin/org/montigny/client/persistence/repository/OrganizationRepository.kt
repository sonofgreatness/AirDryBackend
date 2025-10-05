package org.montigny.client.persistence.repository

import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.User.CurrentUserDTO
import java.util.UUID

interface OrganizationRepository {

    fun createOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO,logo: ByteArray?): UUID
    fun getOrgById(orgId: UUID): GetOrgDTO?
    fun updateOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO,logo: ByteArray?): Int
    fun deleteOrgById(currentUser: CurrentUserDTO,orgId: UUID): Int
    fun getAllOrg (currentUserDTO: CurrentUserDTO):List<CreateOrgDTO>
    fun getAllDept(currentUserDTO: CurrentUserDTO):List<CreateOrgDTO>
    fun getAllSect(currentUserDTO: CurrentUserDTO): List<CreateSectDTO>

    fun createDept(currentUser: CurrentUserDTO, req: CreateDptDTO) : UUID
    fun getDeptById(dptId: UUID) :CreateDptDTO?
    fun updateDept(currentUser: CurrentUserDTO, req: CreateDptDTO) : Int
    fun deleteDeptById(currentUser: CurrentUserDTO,dptId: UUID): Int

    fun createSect(currentUser: CurrentUserDTO,req: CreateSectDTO) : UUID
    fun updateSect(currentUser: CurrentUserDTO,req: CreateSectDTO) : Int
    fun deleteSectById(currentUser: CurrentUserDTO,sectId: UUID): Int

    fun getSectionByDepartmentId(currentUserDTO: CurrentUserDTO, dptId : UUID): List<CreateSectDTO>?

    /*
    fun getOrgs(currentUserDTO: CurrentUserDTO)
    fun getDepts(currentUserDTO: CurrentUserDTO)
    fun getSects(currentUserDTO: CurrentUserDTO)*/
}