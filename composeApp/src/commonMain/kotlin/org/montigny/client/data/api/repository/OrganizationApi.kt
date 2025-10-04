package org.montigny.client.data.api.repository

import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.User.CurrentUserDTO

interface OrganizationApi {
    suspend fun getAllDept():List<CreateOrgDTO>
    suspend fun getAllSect(): List<CreateSectDTO>
}