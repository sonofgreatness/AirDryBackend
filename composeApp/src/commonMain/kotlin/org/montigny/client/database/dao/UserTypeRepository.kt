package org.montigny.client.database.dao

import org.com.client.database.User_type

interface UserTypeRepository {
    suspend fun insert(userType: User_type)
    suspend fun getAll(): List<User_type>
    suspend fun getById(id: String): User_type?
    suspend fun delete(id: String)
    suspend fun deleteAllUserType()
}