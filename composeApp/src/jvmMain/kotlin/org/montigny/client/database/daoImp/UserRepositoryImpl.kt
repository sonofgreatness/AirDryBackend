package org.montigny.client.database.daoImp

import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import org.com.client.database.User_type
import org.montigny.client.API_URL_USER_LOGIN
import org.montigny.client.App
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.PasswordHasher
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.Auth.AuthResponse
import org.montigny.client.dto.User.NamedEntityResponse
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.Constants.Companion.FAILED_LOGIN
import org.montigny.client.util.stateClasses.ApiResponse
import org.slf4j.Logger
import java.util.UUID
import kotlin.jvm.Throws

class UserRepositoryImpl(private val db: AppDatabase,
                         private val passwordHasher: PasswordHasher,
private val offlineSessionManager: OfflineSessionManager,
                         private val logger : Logger): UserRepository {

    private val queries = db.appDatabaseQueries
    override suspend fun deleteAllUsers() {
        queries.deleteAllUsers()
    }

    override suspend fun insertAppUser(user: UserRegisterRequest) {

        try {
            queries.transaction {
                queries.insertAppUser(
                    id = user.id.toString(),
                    name = user.name,
                    email = user.email,
                    surname = user.surname,
                    phone_number = user.phoneNumber,
                    secondary_phone_number = user.secondaryPhoneNumber,
                    is_approved = convertBoolToLong(user.isApproved),
                    is_active = convertBoolToLong(user.isActive),
                    user_type_id = user.userTypeId?.toString(),
                    organization_id = user.organizationId?.toString(),
                    department_id = user.departmentId?.toString(),
                    section_id = user.sectionId?.toString(),
                    password_hash =user.password,
                    is_pending_update = 0L,
                    is_pending_insert =  0L)
            }
        }
        catch (e:Exception){
            println("Error during inserting ${e.localizedMessage}")
        }
    }

    override suspend fun insertAppUserToCacheForLaterUpload(user: UserRegisterRequest) {
        try {
            queries.transaction {
                queries.insertAppUser(
                    id = user.id.toString(),
                    name = user.name,
                    email = user.email,
                    surname = user.surname,
                    phone_number = user.phoneNumber,
                    secondary_phone_number = user.secondaryPhoneNumber,
                    is_approved = convertBoolToLong(user.isApproved),
                    is_active = convertBoolToLong(user.isActive),
                    user_type_id = user.userTypeId?.toString(),
                    organization_id = user.organizationId?.toString(),
                    department_id = user.departmentId?.toString(),
                    section_id = user.sectionId?.toString(),
                    password_hash =user.password,
                    is_pending_update = 1L,
                    is_pending_insert =  1L)
            }
        }
        catch (e:Exception){
            println("Error during inserting [cache]${e.localizedMessage}")
        }
    }
    override suspend fun insertMinimalAppUser(user: UserRegisterRequest) {
        return
    }

    override suspend fun selectAllUsers(): List<UserResponse>? {

    return queries.selectAllUsers().executeAsList().map {it ->
        UserResponse(
            id = UUID.fromString(it.id),
            name = it.name,
            email = it.email,
            surname = it.surname,
            phoneNumber = it.phone_number,
            secondaryPhoneNumber = it.secondary_phone_number,
            isActive =convertLongToBool(it.is_approved) ,
            isApproved = convertLongToBool(it.is_active) ,
            userType = it.user_type_id?.let { uid ->
                NamedEntityResponse(
                    id = UUID.fromString(uid),
                    displayName = it.user_type_name ?: "NONE USERTYPE"
                )
            },
            organization = it.organization_id?.let { oid ->
                NamedEntityResponse(
                    id = UUID.fromString(oid),
                    displayName = it.organization_name ?: "NONE ORGANIZATION"
                )
            },
            department = it.department_id?.let { did ->
                NamedEntityResponse(
                    id = UUID.fromString(did),
                    displayName = it.department_name ?: "NONE DEPARTMENT"
                )
            },
            section = it.section_id?.let { sid ->
                NamedEntityResponse(
                    id = UUID.fromString(sid),
                    displayName = it.section_name ?: "NONE SECTION"
                )
            },
        )
    }

    }

    override suspend fun selectUserById(id: String): UserRegisterRequest? {
        return null
    }

    override suspend fun selectUserByEmail(email: String): UserRegisterRequest? {
        return null
    }




    override suspend fun insert(userType: User_type) {

        queries.transaction{
            queries.insertUserType(
                id = userType.id,
                displayname = userType.displayname
                ,0L,0L
            )
        }
    }




    override suspend fun getAll(): List<User_type> =
        queries.selectAllUserTypes().executeAsList()

    override suspend fun getById(id: String): User_type? =
        queries.selectUserTypeById(id).executeAsOneOrNull()

    override suspend fun delete(id: String) {
        queries.deleteUserTypeById(id)
    }


    override suspend fun deleteAllUserType() = queries.deleteAllUserType()




    override suspend fun authenticate(email: String, password: String): ApiResponse {
        val row = queries.selectUserByEmail(email).executeAsOneOrNull()
            ?: return ApiResponse.Error(403, "No user found with email: $email")

        val storedHash = row.password_hash
            ?: return ApiResponse.Error(403, "No password hash stored for $email")

        logger.info("🔍 Offline auth: email=$email storedHash=$storedHash")

        return if (passwordHasher.verify(password, storedHash)) {
            ApiResponse.Created(row.id)
        } else {
            logger.warn("❌ Password verification failed for user=$email hash=$storedHash")
            ApiResponse.Error(403, "Password verification failed")
        }
    }



    override suspend fun offlineLogin(username: String, password: String): ApiResponse {
        val auth = authenticate(username, password)
        return when (auth) {
            is ApiResponse.Created -> {
                offlineSessionManager.login(auth.id)
                auth
            }
            else -> auth // propagate the ApiResponse.Error with details
        }
    }
    override fun offlineLogout() {
        offlineSessionManager.logout()
    }

    private  fun convertBoolToLong(value: Boolean) = if (value) 1L else 0L

    @Throws(IllegalArgumentException::class)
    private  fun convertLongToBool(value: Long) : Boolean{
        if (value == 1L)
            return true
        if(value ==0L)
            return false
        throw IllegalArgumentException("expecting 0  or 1 but got $value")
    }
}