package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.andWhere
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.domain.api.dto.User.CurrentUserDTO
import org.montigny.client.domain.api.dto.User.NamedEntityResponse
import org.montigny.client.domain.api.dto.User.UserRegisterRequest
import org.montigny.client.domain.api.dto.User.UserResponse
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.AppUsers.userTypeId
import org.montigny.client.persistence.Departments
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.Sections
import org.montigny.client.persistence.UserTypes
import org.montigny.client.persistence.repository.PasswordHasher
import org.montigny.client.persistence.repository.UserRepository
import org.montigny.client.util.UserUtils
import org.montigny.client.util.UserUtils.isSuperUser
import java.util.UUID

class UserRepositoryImpl(private val passwordHasher: PasswordHasher) : UserRepository {

    override fun createUser(req: UserRegisterRequest): UUID = transaction {
        val id = AppUsers.insertAndGetId {
            it[name] = req.name
            it[surname] = req.surname
            it[phoneNumber] = req.phoneNumber
            it[secondaryPhone] = req.secondaryPhoneNumber
            it[userTypeId] = req.userTypeId
            it[organizationId] = req.organizationId
            it[departmentId] = req.departmentId
            it[sectionId] = req.sectionId
            it[email] = req.email
            it[passwordHash] = req.password?.let { p -> passwordHasher.hash(p) }
        }.value

        id
    }


    override  fun createAdminUser(req: UserRegisterRequest): UUID = transaction {
        val superUserTypeId = UserTypes
            .select { UserTypes.displayname eq UserRole.ADMIN.displayName }
            .map { it[UserTypes.id].value }
            .firstOrNull()
            ?: throw IllegalStateException("ADMIN_USER type not found in UserTypes table")

        val adminId = AppUsers.insertAndGetId {
            it[name] = req.name
            it[surname] = req.surname
            it[phoneNumber] = req.phoneNumber
            it[secondaryPhone] = req.secondaryPhoneNumber
            it[userTypeId] = superUserTypeId  // Force SUPER_USER type
            it[organizationId] = req.organizationId
            it[departmentId] = req.departmentId
            it[sectionId] = req.sectionId
            it[email] = req.email
            it[passwordHash] = req.password?.let { p -> passwordHasher.hash(p) }
        }.value

        adminId
    }

    override fun findById(id: UUID): UserRegisterRequest? = transaction {
        AppUsers.select { AppUsers.id eq id }.mapNotNull { row ->
            UserRegisterRequest(
                id = row[AppUsers.id].value,
                name = row[AppUsers.name],
                surname = row[AppUsers.surname],
                phoneNumber = row[AppUsers.phoneNumber],
                isApproved = row[AppUsers.isApproved],
                isActive = row[AppUsers.isActive],
                userTypeId = row[AppUsers.userTypeId]!!.value,
                organizationId = row[AppUsers.organizationId]?.value,
                sectionId = row[AppUsers.sectionId]?.value
            )
        }.singleOrNull()
    }

    override fun findByPhone(phone: String): UserRegisterRequest? = transaction {
        val row = AppUsers.select { AppUsers.phoneNumber eq phone }.singleOrNull() ?: return@transaction null
        UserRegisterRequest(
            id = row[AppUsers.id].value,
            name = row[AppUsers.name],
            surname = row[AppUsers.surname],
            phoneNumber = row[AppUsers.phoneNumber],
            isApproved = row[AppUsers.isApproved],
            isActive = row[AppUsers.isActive],
            userTypeId = row[AppUsers.userTypeId]!!.value,
            organizationId = row[AppUsers.organizationId]?.value,
            sectionId = row[AppUsers.sectionId]?.value
        )
    }
    override fun getUsers(currentUser: CurrentUserDTO): List<UserResponse> = transaction {
        // Get logged-in user's organization ID
        val currentOrgId  = currentUser.organizationId
        val isSuperUser = isSuperUser(currentUser)

        // Build base query
        val query = AppUsers
            .join(UserTypes, JoinType.LEFT, additionalConstraint = { AppUsers.userTypeId eq UserTypes.id })
            .join(Organizations, JoinType.LEFT, additionalConstraint = { AppUsers.organizationId eq Organizations.id })
            .join(Departments, JoinType.LEFT, additionalConstraint = { AppUsers.departmentId eq Departments.id })
            .join(Sections, JoinType.LEFT, additionalConstraint = { AppUsers.sectionId eq Sections.id })
            .slice(
                AppUsers.id,
                AppUsers.name,
                AppUsers.surname,
                AppUsers.phoneNumber,
                AppUsers.secondaryPhone,
                AppUsers.email,
                AppUsers.isActive,
                AppUsers.isApproved,
                AppUsers.userTypeId,
                UserTypes.displayname,
                AppUsers.organizationId,
                Organizations.displayname,
                AppUsers.departmentId,
                Departments.displayname,
                AppUsers.sectionId,
                Sections.displayname
            )
            .selectAll()
        println("SELECT ALL")
        if (!isSuperUser && currentOrgId != null) {
            println("SELECT ALL NoT SUper")
            query.andWhere { AppUsers.organizationId eq currentOrgId }
        }

        query.map { row ->
            UserResponse(
                id = row[AppUsers.id].value,
                name = row[AppUsers.name],
                surname = row[AppUsers.surname],
                phoneNumber = row[AppUsers.phoneNumber],
                secondaryPhoneNumber = row[AppUsers.secondaryPhone],
                email = row[AppUsers.email],
                isActive = row[AppUsers.isActive],
                isApproved = row[AppUsers.isApproved],
                userType = row[AppUsers.userTypeId]?.value?.let {
                    NamedEntityResponse(it, row[UserTypes.displayname])
                },
                organization = row[AppUsers.organizationId]?.value?.let {
                    NamedEntityResponse(it, row[Organizations.displayname])
                },
                department = row[AppUsers.departmentId]?.value?.let {
                    NamedEntityResponse(it, row[Departments.displayname])
                },
                section = row[AppUsers.sectionId]?.value?.let {
                    NamedEntityResponse(it, row[Sections.displayname])
                }
            )
        }
    }

    override fun approveUser(id: UUID) = transaction {
        AppUsers.update({ AppUsers.id eq id }) { it[isApproved] = true }
    }

    override fun updateUser(id: UUID, req: UserRegisterRequest) = transaction {
        AppUsers.update({ AppUsers.id eq id }) {
            it[name] = req.name
            it[surname] = req.surname
            it[phoneNumber] = req.phoneNumber
            it[secondaryPhone] = req.secondaryPhoneNumber
            it[userTypeId] = req.userTypeId
            it[organizationId] = req.organizationId
            it[sectionId] = req.sectionId
            it[email] = req.email
            req.password.let { p -> it[passwordHash] = passwordHasher.hash(p) }
        }
    }

    override fun authenticateByPhoneAndPassword(email: String, password: String): UserRegisterRequest? = transaction {
        val row = AppUsers.select { AppUsers.email eq email }.singleOrNull() ?: return@transaction null
        val storedHash = row[AppUsers.passwordHash] ?: return@transaction null
        println(" User row ==> $row     hash = $storedHash")
        if (!passwordHasher.verify(password, storedHash)) return@transaction null
        UserRegisterRequest(
            id = row[AppUsers.id].value,
            name = row[AppUsers.name],
            surname = row[AppUsers.surname],
            phoneNumber = row[AppUsers.phoneNumber],
            isApproved = row[AppUsers.isApproved],
            isActive = row[AppUsers.isActive],
            userTypeId =row[AppUsers.userTypeId]?.value,
            organizationId = row[AppUsers.organizationId]?.value,
            sectionId = row[AppUsers.sectionId]?.value
        )
    }

    override fun getUserTypeDisplayName(userTypeId: UUID): String? = transaction {
        UserTypes
            .select { UserTypes.id eq userTypeId }
            .map { it[UserTypes.displayname] }
            .singleOrNull()

    }




}
