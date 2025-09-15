package org.montigny.client.domain.api.dto.Auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String)
