package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.domain.api.route.auth.UserRole
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.*
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.persistence.*
import org.montigny.client.persistence.repository.PasswordHasher
import org.montigny.client.persistence.repository.UserRepository
import org.montigny.client.util.Constants
import org.montigny.client.util.UserUtils.isSuperUser
import java.time.Instant
import java.util.*

class UserRepositoryImpl(private val passwordHasher: PasswordHasher) : UserRepository {

    override fun createUser(currentUserDTO: CurrentUserDTO, req: UserRegisterRequest): UUID = transaction {

        val superUserTypeId = UserTypes
            .select { UserTypes.displayname eq UserRole.SUPER_USER.displayName }
            .map { it[UserTypes.id].value }
            .firstOrNull()
         if(superUserTypeId!=null)
             if (superUserTypeId == req.userTypeId)
                  throw IllegalArgumentException("Can no create super user, 1 already exists")

        val id = AppUsers.insertAndGetId {
            it[name] = req.name
            it[surname] = req.surname
            it[phoneNumber] = req.phoneNumber
            it[secondaryPhone] = req.secondaryPhoneNumber
            it[userTypeId] = req.userTypeId
            it[organizationId] = currentUserDTO.organizationId
            it[departmentId] = req.departmentId
            it[sectionId] = req.sectionId
            it[email] = req.email
            it[passwordHash] = req.password?.let { p -> passwordHasher.hash(p)}
            it[isActive] =req.isActive
            it[isApproved] =req.isApproved
            it[createdBy] = currentUserDTO.id

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
                secondaryPhoneNumber = row[AppUsers.secondaryPhone],
                isApproved = row[AppUsers.isApproved],
                isActive = row[AppUsers.isActive],
                userTypeId = row[AppUsers.userTypeId]!!.value,
                organizationId = row[AppUsers.organizationId]?.value,
                sectionId = row[AppUsers.sectionId]?.value,
                email = row[AppUsers.email],
                password = row[AppUsers.passwordHash].toString()
            )
        }.singleOrNull()
    }

   override fun  findUserResponseById(id: UUID): UserResponse?= transaction {
       val baseQuery = AppUsers
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
           .select{
               AppUsers.id  eq id
           }.firstOrNull()
       baseQuery?.let {
           row ->
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
            sectionId = row[AppUsers.sectionId]?.value,
            email = row[AppUsers.email],
        )
    }
    override fun getUsers(currentUser: CurrentUserDTO,
                          page: Int,
                          pageSize: Int): PaginatedResponse<UserResponse> = transaction {
        val currentOrgId = currentUser.organizationId
        val isSuperUser = isSuperUser(currentUser)

        val baseQuery = AppUsers
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

        if (!isSuperUser && currentOrgId != null) {
            baseQuery.andWhere { AppUsers.organizationId eq currentOrgId }
        }

        val totalCount = baseQuery.count()
        val paginatedQuery = baseQuery.limit(pageSize, offset = ((page - 1) * pageSize).toLong())

        val users = paginatedQuery.map { row ->
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
        PaginatedResponse(
            items = users,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = (totalCount / pageSize + if (totalCount % pageSize > 0) 1 else 0).toInt()
        )
    }


   override fun getCasualUsers(currentUser: CurrentUserDTO,page: Int,pageSize: Int,
                               queryString : String?)
   :PaginatedResponse<UserResponse> = transaction {
       val currentOrgId = currentUser.organizationId
       val isSuperUser = isSuperUser(currentUser)

       val baseQuery = AppUsers
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

       if (!isSuperUser && currentOrgId != null) {
           baseQuery.andWhere { AppUsers.organizationId eq currentOrgId }
       }
       // --- Filter to only "Casual Worker" user type ---
       val casualUserType = UserTypes
           .select { UserTypes.displayname like "%${UserRole.CASUAL_WORKER.displayName}%" }
           .limit(1)
           .firstOrNull()

       casualUserType?.let {
           baseQuery.andWhere { AppUsers.userTypeId eq it[UserTypes.id] }
       }
       // --- Optional search filter ---
       queryString?.takeIf { it.isNotBlank() }?.let { q ->
           val pattern = "%${q.lowercase()}%"
           baseQuery.andWhere {
               (AppUsers.name.lowerCase() like pattern) or
                       (AppUsers.surname.lowerCase() like pattern) or
                       (AppUsers.phoneNumber.lowerCase() like pattern) or
                       (AppUsers.secondaryPhone.lowerCase() like pattern) or
                       (AppUsers.email.lowerCase() like pattern)
           }
       }
       val totalCount = baseQuery.count()
       val paginatedQuery = baseQuery.limit(pageSize, offset = ((page - 1) * pageSize).toLong())

       val users = paginatedQuery.map { row ->
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
       PaginatedResponse(
           items = users,
           page = page,
           pageSize = pageSize,
           totalCount = totalCount,
           totalPages = (totalCount / pageSize + if (totalCount % pageSize > 0) 1 else 0).toInt()
       )
   }




    override  fun getUserTypes():List<UserTypesDTO> = transaction {
         UserTypes.select{
             UserTypes.displayname neq(UserRole.SUPER_USER.displayName)
         }.map {
             UserTypesDTO(
                 id = it[UserTypes.id].value,
                 displayName = it[UserTypes.displayname]
             )
         }
    }

    override fun updateUser(id: UUID, req: UpdateUserDTO) = transaction {
        // 1. Load the current user row
        val currentUser = AppUsers.select { AppUsers.id eq id }.firstOrNull()
            ?: throw IllegalArgumentException("User with id $id not found")
        val currentOrgId = currentUser[AppUsers.organizationId]?.value
        val currentDeptId = currentUser[AppUsers.departmentId]?.value

        // 2. Validation: Department must belong to current user's organization (if provided)
        if (req.updated_departmentId != null) {
            val validDepartments = getPossibleDepartmentIdsForUser(id)
            if (req.updated_departmentId !in validDepartments) {
                throw IllegalArgumentException(Constants.DEPARTMENTS_VALIDATION_ERROR)
            }
        }
        // 3. Validation: Section must belong to the selected (or existing) department
        if (req.updated_sectionId != null) {
            val deptToCheck = req.updated_departmentId ?: currentDeptId
            val validSections = getPossibleSectionsForDepartment(deptToCheck, currentOrgId)
            if (req.updated_sectionId !in validSections) {
                throw IllegalArgumentException(Constants.SECTION_VALIDATION_ERROR)
            }
        }
        // 4. Validation: User type must not be SUPER_USER (privilege escalation prevention)
        if (req.updated_userTypeId != null) {
            val newUserType = UserTypes.select { UserTypes.id eq req.updated_userTypeId }.firstOrNull()
                ?: throw IllegalArgumentException(Constants.USER_TYPE_VALIDATION_ERROR)

            if (newUserType[UserTypes.displayname].equals(UserRole.SUPER_USER.displayName, ignoreCase = true)) {
                throw IllegalArgumentException("Cannot assign SUPER_USER role")
            }
        }

        // 5. Apply partial updates (only non-null fields)
        AppUsers.update({ AppUsers.id eq id })
           {
            it[updatedBy]=currentUser[AppUsers.id].value
             it[updatedAt] = Instant.now()
            req.updated_name?.let { n -> it[name] = n }
            req.updated_surname?.let { s -> it[surname] = s }
            req.updated_phoneNumber?.let { p -> it[phoneNumber] = p }
            req.updated_secondaryPhoneNumber?.let { sp -> it[secondaryPhone] = sp }
            req.updated_userTypeId?.let { ut -> it[userTypeId] = ut }
            req.updated_departmentId?.let { dept -> it[departmentId] = dept }
            req.updated_sectionId?.let { sec -> it[sectionId] = sec }
            req.updated_email?.let { e -> it[email] = e }
            req.updated_password?.let {
                p ->
                if(p.length> 4)
                { println("RESETTING PASSSWORD")
                    it[passwordHash] = passwordHasher.hash(p)}
            }
            req.isActive?.let { active -> it[isActive] = active }
            req.isApproved?.let { approved -> it[isApproved] = approved }
        }
    }

    override fun deleteUserId(userId: UUID): Int = transaction {
        AppUsers.deleteWhere { AppUsers.id eq userId }
    }

    private  fun getPossibleDepartmentIdsForUser(userId: UUID): List<UUID> = transaction {
        // Get the user's current organization
        val userRow = AppUsers
            .slice(AppUsers.organizationId)
            .select { AppUsers.id eq userId }
            .firstOrNull() ?: return@transaction emptyList()

        val orgId = userRow[AppUsers.organizationId]?.value ?: return@transaction emptyList()
        Departments
            .slice(Departments.id)
            .select { Departments.organizationId eq orgId }
            .map { it[Departments.id].value }
    }
    fun getPossibleSectionsForDepartment(departmentId: UUID?, organizationId: UUID?): List<UUID> = transaction {
        if (departmentId == null || organizationId == null) return@transaction emptyList()

        // Verify that the department belongs to the given organization first
        val deptOrgId = Departments
            .slice(Departments.organizationId)
            .select { Departments.id eq departmentId }
            .map { it[Departments.organizationId].value }
            .firstOrNull() ?: return@transaction emptyList()

        if (deptOrgId != organizationId) return@transaction emptyList()
        // Now fetch sections under this department
        Sections
            .slice(Sections.id)
            .select { Sections.departmentId eq departmentId }
            .map { it[Sections.id].value }
    }
    override fun authenticateByPhoneAndPassword(email: String, password: String): UserRegisterRequest? = transaction {
        val row = AppUsers.select { AppUsers.email eq email }.singleOrNull() ?: return@transaction null
        val storedHash = row[AppUsers.passwordHash] ?: return@transaction null
        if (!passwordHasher.verify(password, storedHash)) return@transaction null
        UserRegisterRequest(
            id = row[AppUsers.id].value,
            name = row[AppUsers.name],
            surname = row[AppUsers.surname],
            phoneNumber = row[AppUsers.phoneNumber],
            isApproved = row[AppUsers.isApproved],
            isActive = row[AppUsers.isActive],
            userTypeId = row[AppUsers.userTypeId]?.value,
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
    //================== SERVICE IMPLEMENTATION ==================
    override fun getUsersFiltered(
        currentUser: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: MutableMap<String, String>
    ): PaginatedResponse<UserResponse> = transaction {

        val currentOrgId = currentUser.organizationId
        val isSuperUser = isSuperUser(currentUser)

        // Base query with joins
        val baseQuery = AppUsers
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

        // Restrict to current organization if not superuser
        if (!isSuperUser && currentOrgId != null) {
            baseQuery.andWhere { AppUsers.organizationId eq currentOrgId }
        }

        // Apply filters dynamically (case-insensitive for strings, eq for UUIDs)
        filters["name"]?.takeIf { it.isNotBlank() }?.let {
            val query = it.lowercase().trim()
            baseQuery.andWhere { AppUsers.name.lowerCase() like "%$query%" }
        }
        filters["surname"]?.takeIf { it.isNotBlank() }?.let {
            val query = it.lowercase().trim()
            baseQuery.andWhere { AppUsers.surname.lowerCase() like "%$query%" }
        }
        filters["email"]?.takeIf { it.isNotBlank() }?.let {
            val query = it.lowercase().trim()
            baseQuery.andWhere { AppUsers.email.lowerCase() like "%$query%" }
        }
        filters["phoneNumber"]?.takeIf { it.isNotBlank() }?.let {
            val query = it.lowercase().trim()
            baseQuery.andWhere { AppUsers.phoneNumber.lowerCase() like "%$query%" }
        }

        // UUID-based filters (use eq, not like)
        filters[Constants.ORGANIZATION_ID]?.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrNull()?.let { uuid ->
                baseQuery.andWhere { AppUsers.organizationId eq uuid }
            }
        }
        filters[Constants.DEPARTMENT_ID]?.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrNull()?.let { uuid ->
                baseQuery.andWhere { AppUsers.departmentId eq uuid }
            }
        }
        filters[Constants.SECTION_ID]?.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrNull()?.let { uuid ->
                baseQuery.andWhere { AppUsers.sectionId eq uuid }
            }
        }
        filters[Constants.USER_TYPE_ID]?.takeIf { it.isNotBlank() }?.let {
            runCatching { UUID.fromString(it) }.getOrNull()?.let { uuid ->
                baseQuery.andWhere { AppUsers.userTypeId eq uuid }
            }
        }

        val totalCount = baseQuery.count()

        val paginatedQuery = baseQuery.limit(pageSize, offset = ((page - 1) * pageSize).toLong())

        val users = paginatedQuery.map { row ->
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

        PaginatedResponse(
            items = users,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = (totalCount / pageSize + if (totalCount % pageSize > 0) 1 else 0).toInt()
        )
    }


    override fun getUsersFilteredWithDTO(
        currentUser: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<UserResponse> = transaction {

        val currentOrgId = currentUser.organizationId
        val isSuperUser = isSuperUser(currentUser)

        // Base query with joins (identical to the first function)
        val baseQuery = AppUsers
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

        // Restrict to current organization if not superuser
        if (!isSuperUser && currentOrgId != null) {
            baseQuery.andWhere { AppUsers.organizationId eq currentOrgId }
        }
        filters.filterDepartmentId?.let { uuid ->
            baseQuery.andWhere { AppUsers.departmentId eq uuid }
        }
        filters.filterSectionId?.let { uuid ->
            baseQuery.andWhere { AppUsers.sectionId eq uuid }
        }

        // 2. Name, Surname, Phone (String-based filters, case-insensitive, LIKE)
        filters.filterName?.takeIf { it.isNotBlank() }?.let { query ->
            baseQuery.andWhere { AppUsers.name.lowerCase() like "%${query.lowercase()}%" }
        }
        filters.filterSurname?.takeIf { it.isNotBlank() }?.let { query ->
            baseQuery.andWhere { AppUsers.surname.lowerCase() like "%${query.lowercase()}%" }
        }
        filters.filterPhoneNumber?.takeIf { it.isNotBlank() }?.let { query ->
            // Assuming partial match for phone numbers is acceptable
            baseQuery.andWhere { AppUsers.phoneNumber like "%$query%" }
        }
        filters.filterSecondaryPhoneNumber?.takeIf { it.isNotBlank() }?.let { query ->
            // Assuming partial match for secondary phone numbers is acceptable
            baseQuery.andWhere { AppUsers.secondaryPhone like "%$query%" }
        }
        if (!filters.includeInactiveWorkers) {
            // If we DON'T include inactive workers, filter for only active workers
            baseQuery.andWhere { AppUsers.isActive eq true or AppUsers.isApproved}
        }
        val totalCount = baseQuery.count()

        val paginatedQuery = baseQuery.limit(pageSize, offset = ((page - 1) * pageSize).toLong())

        val users = paginatedQuery.map { row ->
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
        // --- Return Paginated Response ---
        PaginatedResponse(
            items = users,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = (totalCount / pageSize + if (totalCount % pageSize > 0) 1 else 0).toInt()
        )
    }
    override fun getUserUnPaginated(currentUserDTO: CurrentUserDTO):List<UserRegisterRequest> =transaction{
        val currentOrgId = currentUserDTO.organizationId
        AppUsers.select {
            AppUsers.organizationId eq currentOrgId
        }.map {
            UserRegisterRequest(
                id = it[AppUsers.id].value, name = it[AppUsers.name],
                surname = it[AppUsers.surname], phoneNumber = it[AppUsers.phoneNumber],
                secondaryPhoneNumber = it[AppUsers.secondaryPhone], departmentId = it[AppUsers.departmentId]?.value,
                email = it[AppUsers.email], sectionId = it[AppUsers.sectionId]?.value,isActive = it[AppUsers.isActive],
                isApproved = it[AppUsers.isApproved], userTypeId = it[AppUsers.userTypeId]?.value,
                organizationId = it[AppUsers.organizationId]?.value, password = it[AppUsers.passwordHash]!!)
        }
    }

}
