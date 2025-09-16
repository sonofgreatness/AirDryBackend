package org.montigny.client.service

import org.montigny.client.domain.api.dto.Organization.CreateDptDTO
import org.montigny.client.domain.api.dto.Organization.CreateOrgDTO
import org.montigny.client.domain.api.dto.Organization.GetOrgDTO
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.persistence.repository.OrganizationRepository
import java.util.UUID

class OrganizationService(private val orgRepo: OrganizationRepository) {
    fun  createOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO, logo : ByteArray?): UUID{
        return orgRepo.createOrg(currentUser, req, logo)
    }
    fun getOrgById(orgId: UUID): GetOrgDTO?{
        return orgRepo.getOrgById(orgId)
    }
    fun updateOrg(currentUser: CurrentUserDTO,req: CreateOrgDTO,logo: ByteArray?){
        orgRepo.updateOrg(currentUser, req, logo)
    }
    fun deleteOrgById(currentUser: CurrentUserDTO, orgId: UUID): Int{
        return orgRepo.deleteOrgById(currentUser,orgId)
    }
    fun createDept(currentUser: CurrentUserDTO, req: CreateDptDTO) : UUID{
        return orgRepo.createDept(currentUser, req)
    }
    fun getDeptById(dptId: UUID):CreateDptDTO?{
        return orgRepo.getDeptById(dptId)
    }
}