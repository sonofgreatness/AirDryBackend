package org.montigny.client.data.api.repository

import org.montigny.client.dto.User.UserTypesDTO

interface UserApi {

    suspend fun login(username: String, password: String): Boolean
    fun logout()

   suspend fun getUserTypes():List<UserTypesDTO>
}