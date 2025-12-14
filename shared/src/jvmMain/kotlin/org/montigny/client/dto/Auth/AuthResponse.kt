package org.montigny.client.dto.Auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String)
