package org.montigny.client.database.daoImp

import org.com.client.database.User_type
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.User.NamedEntityResponse
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import java.util.UUID
import kotlin.jvm.Throws

class UserRepositoryImpl(private val db: AppDatabase): UserRepository {

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
                    password_hash = user.password,
                )
            }
        }
        catch (e:Exception){
            println("Error during inserting ${e.localizedMessage}")
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