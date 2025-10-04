package org.montigny.client.data.api.implementation

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.util.logging.*
import org.montigny.client.API_BASE_URL
import org.montigny.client.API_URL_ORG_DEPT_ALL
import org.montigny.client.API_URL_ORG_SECT_ALL
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO

class OrganizationApiImpl(private val client: HttpClient,
                          private val logger: Logger): OrganizationApi {
    override suspend fun getAllDept(): List<CreateOrgDTO>
    {
        val response: List<CreateOrgDTO> = client.get("$API_BASE_URL$API_URL_ORG_DEPT_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }

    override suspend  fun getAllSect(): List<CreateSectDTO> {
        val response: List<CreateSectDTO> = client.get("$API_BASE_URL$API_URL_ORG_SECT_ALL"){
            contentType(ContentType.Application.Json)
        }.body()
        return response
    }
}