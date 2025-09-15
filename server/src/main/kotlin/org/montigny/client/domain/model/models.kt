package org.montigny.client.domain.model

import java.util.UUID

data class OrganizationDto(val id: UUID?, val displayName: String,
                           val logo: ByteArray? = null)

data class DepartmentDto(val id: UUID?, val displayName: String,
                         val organizationId: UUID)

data class SectionDto(val id: UUID?,
                      val displayName: String,
                      val departmentId: UUID)
