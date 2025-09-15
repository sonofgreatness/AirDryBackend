package org.montigny.client.domain.api.dto.Auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequest(val username: String,
                       val password: String)
