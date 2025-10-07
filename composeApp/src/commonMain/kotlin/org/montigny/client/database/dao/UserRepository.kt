package org.montigny.client.database.dao

import org.com.client.database.App_user
import org.com.client.database.SelectAllUsers
import org.com.client.database.User_type
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.stateClasses.ApiResponse

interface UserRepository {
    suspend fun deleteAllUsers()
    suspend fun insertAppUser(user: UserRegisterRequest)
    suspend fun insertAppUserToCacheForLaterUpload(user: UserRegisterRequest)


    suspend fun insertMinimalAppUser(user: UserRegisterRequest)
    suspend fun selectAllUsers() : List<UserResponse>?
    suspend fun selectUserById(id: String): UserRegisterRequest?
    suspend fun selectUserByEmail(email: String): UserRegisterRequest?

    suspend fun insert(userType: User_type)
    suspend fun getAll(): List<User_type>
    suspend fun getById(id: String): User_type?
    suspend fun delete(id: String)
    suspend fun deleteAllUserType()

    suspend fun authenticate(email: String, password: String): ApiResponse
    suspend fun offlineLogin(username: String, password: String): ApiResponse
     fun offlineLogout()
    suspend fun  deleteUnCachedUsers()
    suspend fun selectCachedForInsertUsers() : List<UserRegisterRequest>
    suspend fun  selectCachedForUpdateUsers() : List<UserRegisterRequest>
    suspend fun  unCacheUserForInsert(id : String)
    suspend fun  unCacheUserForUpdate(id : String)
    suspend fun  updateUserIncludingPassword(user:UserRegisterRequest)
    suspend fun  updateUserNoPassword(user:UserRegisterRequest)



}