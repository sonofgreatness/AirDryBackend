package org.montigny.client.data.api.repository

interface UserApi {

    suspend fun login(username: String, password: String): Boolean
    fun logout()

}