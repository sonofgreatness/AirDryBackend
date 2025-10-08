package org.montigny.client.database.daoImp

import org.com.client.database.User_type
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.PasswordHasher
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.User.NamedEntityResponse
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.UtilityFunctions.Companion.convertBoolToLong
import org.montigny.client.util.UtilityFunctions.Companion.convertLongToBool
import org.montigny.client.util.UtilityFunctions.Companion.toStringCustom
import org.montigny.client.util.UtilityFunctions.Companion.toStringOrNull
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.montigny.client.util.stateClasses.ApiResponse
import org.slf4j.Logger
import java.util.*

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
                    is_pending_update = 0L,
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
        val row = queries.selectUserById(id).executeAsOneOrNull() ?: throw Exception("User with id $id not found")

        return UserRegisterRequest(
            name = row.name, surname = row.surname,
            phoneNumber = row.phone_number,
            secondaryPhoneNumber = row.secondary_phone_number,
            email = row.email,
            id = row.id.toUUIDOrNull(),
            departmentId=row.department_id.toUUIDOrNull(),
            sectionId=row.section_id.toUUIDOrNull(),
            userTypeId = row.user_type_id.toUUIDOrNull(),
            organizationId = row.organization_id?.toUUIDOrNull(),
            isActive = convertLongToBool(row.is_active),
            isApproved = convertLongToBool(row.is_approved),
            password = row.password_hash.toString(),
        )

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

    override suspend fun deleteUnCachedUsers() {
        queries.deleteUnCachedUsers()
    }

    override suspend fun selectCachedForInsertUsers(): List<UserRegisterRequest> {
      return queries.selectCachedForInsertUser().executeAsList().map{
            UserRegisterRequest(
                id = UUID.fromString(it.id),
                name = it.name, email = it.email, surname = it.surname,
                phoneNumber = it.phone_number, secondaryPhoneNumber = it.secondary_phone_number,
                sectionId = it.section_id.toUUIDOrNull(),
                departmentId = it.department_id.toUUIDOrNull(),
                organizationId =it.organization_id.toUUIDOrNull(),
                isActive = convertLongToBool(it.is_active),
                isApproved = convertLongToBool(it.is_approved),
                userTypeId = UUID.fromString(it.user_type_id),
                password = it.password_hash.toString())
        }

    }

    override suspend fun selectCachedForUpdateUsers(): List<UserRegisterRequest> {
      return  queries.selectCachedForUpdateUser().executeAsList().map {
            UserRegisterRequest(
                id = UUID.fromString(it.id),
                name = it.name, email = it.email, surname = it.surname,
                phoneNumber = it.phone_number, secondaryPhoneNumber = it.secondary_phone_number,
                sectionId = UUID.fromString(it.section_id),
                departmentId = UUID.fromString(it.department_id),
                organizationId = UUID.fromString(it.organization_id),
                isActive = convertLongToBool(it.is_active),
                isApproved = convertLongToBool(it.is_approved),
                userTypeId = UUID.fromString(it.user_type_id),
                password = it.password_hash.toString())

        }

    }

    override suspend fun  unCacheUserForInsert(id : String)=  queries.unCacheUserForInsert(id)

    override suspend  fun  unCacheUserForUpdate(id : String)= queries.unCacheUserForUpdate(id)


   override suspend fun  updateUserIncludingPassword(user:UserRegisterRequest){
       queries.updateUserIncludingPassword(
           name = user.name, email = user.email,
           id = user.id.toStringCustom(),
           surname = user.surname,
           phone_number = user.phoneNumber,
           secondary_phone_number = user.secondaryPhoneNumber,
           is_approved = convertBoolToLong(user.isApproved),
           is_active = convertBoolToLong(user.isActive),
           user_type_id = user.userTypeId.toStringOrNull(),
           organization_id = user.organizationId.toStringOrNull(),
           department_id = user.departmentId.toStringOrNull(),
           section_id = user.sectionId.toStringOrNull(),
           password_hash = user.password
       )
   }
    override suspend fun  updateUserNoPassword(user:UserRegisterRequest){
       queries.transaction{
        queries.updateUserNoPassword(
            name = user.name, email = user.email,
            id = user.id.toStringCustom(),
            surname = user.surname,
            phone_number = user.phoneNumber,
            secondary_phone_number = user.secondaryPhoneNumber,
            is_approved = convertBoolToLong(user.isApproved),
            is_active = convertBoolToLong(user.isActive),
            user_type_id = user.userTypeId.toStringOrNull(),
            organization_id = user.organizationId.toStringOrNull(),
            department_id = user.departmentId.toStringOrNull(),
            section_id = user.sectionId.toStringOrNull())
       }
    }
}