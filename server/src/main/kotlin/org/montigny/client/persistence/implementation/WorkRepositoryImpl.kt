package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.WORK_DONE_EDIT_GRACE_PERIOD
import org.montigny.client.WORK_DONE_EDIT_GRACE_PERIOD_CAPTURER
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.CreateWeeklyApprovalDTO
import org.montigny.client.dto.UpdateWeeklyApprovalDTO
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.WeeklyApprovalDTO
import org.montigny.client.dto.Work.*
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.persistence.*
import org.montigny.client.persistence.repository.WorkRepository
import org.montigny.client.util.UserUtils
import org.montigny.client.util.UserUtils.checkIfBelongsToOrganization
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import java.util.*

class WorkRepositoryImpl(): WorkRepository {
    override fun createWork(
        user: CurrentUserDTO,
        req: CreateWorkDoneDTO
    ): UUID = transaction {
        // --- 1️⃣ Insert into work_done ---
        val workDoneId = WorkDone.insertAndGetId {
            it[id] = req.id
            it[workDate] = req.workDate.toInstant()
            it[createdBy] = user.id
            it[createdAt] = Instant.now()
        }.value

        // --- 2️⃣ Collect and validate user IDs ---
        val allUserIds = req.workersList.flatMap { it.userIds }.distinct() // enforce uniqueness
        val missingUsers = allUserIds.filterNot { uid ->
            AppUsers.select { AppUsers.id eq uid }.any()
        }
        if (missingUsers.isNotEmpty()) {
            error("Some users not found: ${missingUsers.joinToString()}")
        }

        // --- 3️⃣ Collect and validate stack IDs (duplicates allowed) ---
        val allStackIds = req.stacksList.flatMap { it.stackIds } // do NOT distinct here
        val missingStacks = allStackIds.distinct().filterNot { sid ->
            Stacks.select { Stacks.id eq sid }.any()
        }
        if (missingStacks.isNotEmpty()) {
            error("Some stacks not found: ${missingStacks.joinToString()}")
        }

        // --- 4️⃣ Insert work_done_user links (unique per user) ---
        allUserIds.forEach { userId ->
            WorkDoneUsers.insert {
                it[this.workDoneId] = EntityID(workDoneId, WorkDone)
                it[appUserId] = EntityID(userId, AppUsers)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }

        // --- 5️⃣ Insert work_done_stack links (duplicates allowed) ---
        allStackIds.forEach { stackId ->
            WorkDoneStacks.insert {
                it[this.workDoneId] = EntityID(workDoneId, WorkDone)
                it[this.stackId] = EntityID(stackId, Stacks)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        workDoneId
    }

    override fun updateWork(user: CurrentUserDTO, req: CreateWorkDoneDTO): Int = transaction {
        // --- 1️⃣ Check existence of WorkDone record ---
        val existing = WorkDone.select { WorkDone.id eq req.id }.singleOrNull()
            ?: error("Work record not found for ID: ${req.id}")

        val myCreatedAt = existing[WorkDone.createdAt]
        val now = Instant.now()

        // --- 2️⃣ Enforce time update rules ---
        if (UserUtils.isAdminUser(user) &&
            myCreatedAt.isBefore(now.minus(WORK_DONE_EDIT_GRACE_PERIOD.toLong(), ChronoUnit.DAYS))) {
            error("Admin cannot update work older than 7 days")
        }
        if (UserUtils.isCapturerUser(user) &&
            myCreatedAt.isBefore(now.minus(WORK_DONE_EDIT_GRACE_PERIOD_CAPTURER.toLong(), ChronoUnit.DAYS))) {
            error("Capturer cannot update work older than 2 days")
        }
        // --- 3️⃣ Update main record ---
        val updatedRows = WorkDone.update({ WorkDone.id eq req.id }) {
            //g
            // it[workDate] = req.workDate.toInstant()
            it[updatedBy] = user.id
            it[updatedAt] = Instant.now()
        }
        // --- 4️⃣ Validate users ---
        val allUserIds = req.workersList.flatMap { it.userIds }.distinct()
        val missingUsers = allUserIds.filterNot { uid ->
            AppUsers.select { AppUsers.id eq uid }.any()
        }
        if (missingUsers.isNotEmpty()) {
            error("Some users not found: ${missingUsers.joinToString()}")
        }
        // --- 5️⃣ Validate stacks ---
        val allStackIds = req.stacksList.flatMap { it.stackIds } // duplicates allowed
        val missingStacks = allStackIds.distinct().filterNot { sid ->
            Stacks.select { Stacks.id eq sid }.any()
        }
        if (missingStacks.isNotEmpty()) {
            error("Some stacks not found: ${missingStacks.joinToString()}")
        }

        // --- 6️⃣ Clear existing relations ---
        WorkDoneUsers.deleteWhere { WorkDoneUsers.workDoneId eq EntityID(req.id, WorkDone) }
        WorkDoneStacks.deleteWhere { WorkDoneStacks.workDoneId eq EntityID(req.id, WorkDone) }
        // --- 7️⃣ Re-insert user relations (unique) ---
        allUserIds.forEach { userId ->
            WorkDoneUsers.insert {
                it[workDoneId] = EntityID(req.id, WorkDone)
                it[appUserId] = EntityID(userId, AppUsers)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        // --- 8️⃣ Re-insert stack relations (duplicates allowed) ---
        allStackIds.forEach { stackId ->
            WorkDoneStacks.insert {
                it[workDoneId] = EntityID(req.id, WorkDone)
                it[this.stackId] = EntityID(stackId, Stacks)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        updatedRows
    }
    override  fun  deleteWorkById (user: CurrentUserDTO, id: UUID): Int =transaction{
        // --- 1️⃣ Check existence of WorkDone record ---
        val existing = WorkDone.select { WorkDone.id eq id }.singleOrNull()
            ?: error("Work record not found for ID: $id")

        val createdAt = existing[WorkDone.createdAt]
        val now = Instant.now()

        if (UserUtils.isAdminUser(user) &&
            createdAt.isBefore(now.minus(7, ChronoUnit.DAYS))) {
            error("Admin cannot delete work older than 7 days")
        }
        if (UserUtils.isCapturerUser(user) &&
            createdAt.isBefore(now.minus(2, ChronoUnit.DAYS))) {
            error("Capturer cannot delete work older than 2 days")
        }
        WorkDone.deleteWhere { WorkDone.id eq id }
    }
    override fun getWorkByUserId(
        user: CurrentUserDTO,
        id: UUID,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<CreateWorkDoneDTO> = transaction {
        // 1️⃣ Get all WorkDone IDs for this user
        val query = WorkDoneUsers
            .slice(WorkDoneUsers.workDoneId)
            .select { WorkDoneUsers.appUserId eq id }

        val totalCount = query.count()
        val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()

        val workIds = query
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { it[WorkDoneUsers.workDoneId].value }

        val items = workIds.mapNotNull { getWorkById(user, it) }

        PaginatedResponse(
            items = items,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }
    override fun getWorkById(
        currentUserDTO: CurrentUserDTO,
        id: UUID
    ): CreateWorkDoneDTO? = transaction {
        val workRow = WorkDone.select { WorkDone.id eq id }.singleOrNull() ?: return@transaction null

        // --- Get related users ---
        val userIds = WorkDoneUsers
            .slice(WorkDoneUsers.appUserId)
            .select { WorkDoneUsers.workDoneId eq EntityID(id, WorkDone) }
            .map { it[WorkDoneUsers.appUserId].value }

        val usersDTO = listOf(
            CreateWorkDoneDTO.WorkDoneUsersDTO(
                id = UUID.randomUUID(),
                userIds = userIds
            )
        )

        // --- Get related stacks ---
        val stackIds = WorkDoneStacks
            .slice(WorkDoneStacks.stackId)
            .select { WorkDoneStacks.workDoneId eq EntityID(id, WorkDone) }
            .map { it[WorkDoneStacks.stackId].value }

        val stacksDTO = listOf(
            CreateWorkDoneDTO.WorkDoneStacksDTO(
                id = UUID.randomUUID(),
                stackIds = stackIds
            )
        )

        CreateWorkDoneDTO(
            id = id,
            workDate = Date.from(workRow[WorkDone.workDate]),
            workersList = usersDTO,
            stacksList = stacksDTO
        )
    }
    override fun getAllWork(
        currentUserDTO: CurrentUserDTO,
        page: Int,
        pageSize: Int
    ): PaginatedResponse<CreateWorkDoneDTO> = transaction {
        val query = WorkDone.selectAll()
        val totalCount = query.count()
        val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()

        val workRows = query
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { it }

        val items = workRows.mapNotNull { workRow ->
            val workId = workRow[WorkDone.id].value

            val userIds = WorkDoneUsers
                .slice(WorkDoneUsers.appUserId)
                .select { WorkDoneUsers.workDoneId eq EntityID(workId, WorkDone) }
                .map { it[WorkDoneUsers.appUserId].value }

            val stackIds = WorkDoneStacks
                .slice(WorkDoneStacks.stackId)
                .select { WorkDoneStacks.workDoneId eq EntityID(workId, WorkDone) }
                .map { it[WorkDoneStacks.stackId].value }

            CreateWorkDoneDTO(
                id = workId,
                workDate = Date.from(workRow[WorkDone.workDate]),
                workersList = listOf(CreateWorkDoneDTO.WorkDoneUsersDTO(UUID.randomUUID(), userIds)),
                stacksList = listOf(CreateWorkDoneDTO.WorkDoneStacksDTO(UUID.randomUUID(), stackIds))
            )
        }

        PaginatedResponse(
            items = items,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }

    override fun getDailyWorkSummaries(
        currentUserDTO: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<WorkDaySummaryDTO> = transaction {

        // --------------------------------------------------------
        // STEP 1 — Build user filter conditions
        // --------------------------------------------------------
        val userConditions = mutableListOf<Op<Boolean>>()

        if (!filters.includeInactiveWorkers) {
            userConditions += (AppUsers.isActive eq true) and (AppUsers.isApproved eq true)
        }

        filters.filterDepartmentId?.let { userConditions += (AppUsers.departmentId eq it) }
        filters.filterSectionId?.let { userConditions += (AppUsers.sectionId eq it) }
        filters.filterName?.takeIf { it.isNotBlank() }?.let { userConditions += AppUsers.name.lowerCase() like "%${it.lowercase()}%" }
        filters.filterSurname?.takeIf { it.isNotBlank() }?.let { userConditions += AppUsers.surname.lowerCase() like "%${it.lowercase()}%" }
        filters.filterPhoneNumber?.takeIf { it.isNotBlank() }?.let { userConditions += AppUsers.phoneNumber like "%$it%" }
        filters.filterSecondaryPhoneNumber?.takeIf { it.isNotBlank() }?.let { userConditions += AppUsers.nationalId like "%$it%" }

        val combinedUserCondition = userConditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE

        // --------------------------------------------------------
        // STEP 2 — Find WorkDone IDs linked to users matching filters
        // --------------------------------------------------------
        val filteredWorkDoneIds = (WorkDoneUsers innerJoin AppUsers)
            .slice(WorkDoneUsers.workDoneId)
            .select(combinedUserCondition)
            .map { it[WorkDoneUsers.workDoneId].value }
            .toSet()

        if (filteredWorkDoneIds.isEmpty()) {
            return@transaction PaginatedResponse(emptyList(), page, pageSize, 0, 1)
        }

        // --------------------------------------------------------
// STEP 3 — Base query with optional date filter
// --------------------------------------------------------
        val filteredQuery = filters.filterDate
            ?.takeIf { it.isNotBlank() }
            ?.let { dateStr ->
                val date = LocalDate.parse(dateStr)
                val startOfDay = date.atStartOfDay(ZoneOffset.UTC).toInstant()
                val endOfDay = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()
                WorkDone
                    .select {
                        (WorkDone.id inList filteredWorkDoneIds) and
                                (WorkDone.workDate.between(startOfDay, endOfDay))
                    }
                    .orderBy(WorkDone.workDate to SortOrder.DESC)
            }
            ?: WorkDone
                .select { WorkDone.id inList filteredWorkDoneIds }
                .orderBy(WorkDone.workDate to SortOrder.DESC)

        // --------------------------------------------------------
        // STEP 4 — Pagination
        // --------------------------------------------------------
        val totalCount = filteredQuery.count()
        val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()
        val pagedRows = filteredQuery
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .toList()

        // --------------------------------------------------------
        // STEP 5 — Transform each WorkDone into WorkSummaryDTO
        // --------------------------------------------------------
        val summaries = pagedRows.map { workRow ->
            val workId = workRow[WorkDone.id].value
            val workDateInstant = workRow[WorkDone.workDate]
            val date = Date.from(workDateInstant)
            val dateFmt = SimpleDateFormat("EEEE dd/MM/yyyy")
            val dayLabel = dateFmt.format(date)

            // --- Get stacks ---
            val stacks = (WorkDoneStacks innerJoin Stacks innerJoin StackCodes)
                .select { WorkDoneStacks.workDoneId eq workId }
                .map { row ->
                    StackInfoDTO(
                        id = row[Stacks.id].value,
                        displayName = row[Stacks.displayName],
                        stackCodeDisplayName = row[StackCodes.displayName],
                        volume = row[Stacks.volume],
                        stackCode = row[StackCodes.displayCode]
                    )
                }

            val totalVolume = stacks.sumOf { it.volume }
            val totalStacks = stacks.size

            // --- Get users ---
            val users = (WorkDoneUsers innerJoin AppUsers)
                .select { WorkDoneUsers.workDoneId eq workId }
                .map { row ->
                    UserInfoDTO(
                        id = row[AppUsers.id].value,
                        name = row[AppUsers.name],
                        surname = row[AppUsers.surname],
                        phoneNumber = row[AppUsers.phoneNumber],
                        isActive = row[AppUsers.isActive],
                        isApproved = row[AppUsers.isApproved],
                        personalCubes = 0.0
                    )
                }

            val totalUsers = users.size
            val perUserVolume = if (totalUsers > 0) totalVolume / totalUsers else 0.0
            val usersWithCubes = users.map { it.copy(personalCubes = perUserVolume) }

            // --- Format created/updated time ---
            val timeFormat =  SimpleDateFormat("EEEE dd/MM/yyyy HH:mm")
            val createdAtStr = workRow[WorkDone.createdAt]?.let { timeFormat.format(Date.from(it)) }
            val updatedAtStr = workRow[WorkDone.updatedAt]?.let { timeFormat.format(Date.from(it)) }

            // --- Resolve createdBy/updatedBy names ---
            fun resolveUserName(userId: UUID?) =
                userId?.let {
                    AppUsers.select { AppUsers.id eq it }
                        .map { r -> "${r[AppUsers.name]} ${r[AppUsers.surname]}" }
                        .firstOrNull()
                }

            val createdByName = resolveUserName(workRow[WorkDone.createdBy])
            val updatedByName = resolveUserName(workRow[WorkDone.updatedBy])

            WorkSummaryDTO(
                id = workId,
                workDate = dayLabel,
                totalVolume = totalVolume,
                totalStacks = totalStacks,
                totalUsers = totalUsers,
                stacks = stacks,
                users = usersWithCubes,
                capturedAt = createdAtStr,
                updatedAt = updatedAtStr,
                capturedBy = createdByName,
                updatedBy = updatedByName
            )
        }

        // --------------------------------------------------------
        // STEP 6 — Group by day label
        // --------------------------------------------------------
        val groupedByDay = summaries.groupBy { it.workDate }.map { entry ->
            val day = entry.key
            val works = entry.value
            WorkDaySummaryDTO(
                dayLabel = day,
                totalVolume = works.sumOf { it.totalVolume },
                totalStacks = works.sumOf { it.totalStacks },
                totalUsers = works.flatMap { it.users }.distinctBy { it.id }.size,
                workList = works
            )
        }

        // --------------------------------------------------------
        // STEP 7 — Return paginated response
        // --------------------------------------------------------
        PaginatedResponse(
            items = groupedByDay,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }
    override fun getDailyWorkSummariesBetweenDates(
        currentUserDTO: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        filters: WorkDataFiltersDTO
    ): PaginatedResponse<WorkDaySummaryDTO> = transaction {

        // --------------------------------------------------------
        // STEP 1 — Validate and parse date range
        // --------------------------------------------------------
        val startDate = filters.filterDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }

        val endDate = filters.filterDateEndDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() }

        if (startDate != null && endDate != null && startDate > endDate) {
            throw IllegalStateException("Start date cannot be after end date.")
        }

        // --------------------------------------------------------
        // STEP 2 — Build user filter conditions (same as sister function)
        // --------------------------------------------------------
        val userConditions = mutableListOf<Op<Boolean>>()

        if (!filters.includeInactiveWorkers) {
            userConditions += (AppUsers.isActive eq true) and (AppUsers.isApproved eq true)
        }

        filters.filterDepartmentId?.let { userConditions += (AppUsers.departmentId eq it) }
        filters.filterSectionId?.let { userConditions += (AppUsers.sectionId eq it) }
        filters.filterName?.takeIf { it.isNotBlank() }?.let {
            userConditions += AppUsers.name.lowerCase() like "%${it.lowercase()}%"
        }
        filters.filterSurname?.takeIf { it.isNotBlank() }?.let {
            userConditions += AppUsers.surname.lowerCase() like "%${it.lowercase()}%"
        }
        filters.filterPhoneNumber?.takeIf { it.isNotBlank() }
            ?.let { userConditions += AppUsers.phoneNumber like "%$it%" }
        filters.filterSecondaryPhoneNumber?.takeIf { it.isNotBlank() }
            ?.let { userConditions += AppUsers.nationalId like "%$it%" }

        val combinedUserCondition = userConditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE

        // --------------------------------------------------------
        // STEP 3 — Find WorkDone IDs matching user filters
        // --------------------------------------------------------
        val filteredWorkDoneIds = (WorkDoneUsers innerJoin AppUsers)
            .slice(WorkDoneUsers.workDoneId)
            .select(combinedUserCondition)
            .map { it[WorkDoneUsers.workDoneId].value }
            .toSet()

        if (filteredWorkDoneIds.isEmpty()) {
            return@transaction PaginatedResponse(emptyList(), page, pageSize, 0, 1)
        }

        // --------------------------------------------------------
        // STEP 4 — Date range filter logic
        // --------------------------------------------------------
        val dateFilter: Op<Boolean> = when {
            startDate != null && endDate != null ->
                WorkDone.workDate.between(startDate, endDate)
            startDate != null ->
                WorkDone.workDate greaterEq startDate
            endDate != null ->
                WorkDone.workDate less endDate
            else -> Op.TRUE
        }

        val filteredQuery = WorkDone
            .select { (WorkDone.id inList filteredWorkDoneIds) and dateFilter }
            .orderBy(WorkDone.workDate to SortOrder.DESC)

        // --------------------------------------------------------
        // STEP 5 — Pagination (same as sister function)
        // --------------------------------------------------------
        val totalCount = filteredQuery.count()
        val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()
        val pagedRows = filteredQuery
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .toList()

        // --------------------------------------------------------
        // STEP 6 — Transform into WorkSummaryDTO (copy-paste of existing logic)
        // --------------------------------------------------------
        val summaries = pagedRows.map { workRow ->
            val workId = workRow[WorkDone.id].value
            val workDateInstant = workRow[WorkDone.workDate]
            val dateStr = SimpleDateFormat("EEEE dd/MM/yyyy").format(Date.from(workDateInstant))

            val stacks = (WorkDoneStacks innerJoin Stacks innerJoin StackCodes)
                .select { WorkDoneStacks.workDoneId eq workId }
                .map { row ->
                    StackInfoDTO(
                        id = row[Stacks.id].value,
                        displayName = row[Stacks.displayName],
                        stackCodeDisplayName = row[StackCodes.displayName],
                        volume = row[Stacks.volume],
                        stackCode = row[StackCodes.displayCode]
                    )
                }

            val totalVolume = stacks.sumOf { it.volume }
            val totalStacks = stacks.size

            val users = (WorkDoneUsers innerJoin AppUsers)
                .select { WorkDoneUsers.workDoneId eq workId }
                .map { row ->
                    UserInfoDTO(
                        id = row[AppUsers.id].value,
                        name = row[AppUsers.name],
                        surname = row[AppUsers.surname],
                        phoneNumber = row[AppUsers.phoneNumber],
                        isActive = row[AppUsers.isActive],
                        isApproved = row[AppUsers.isApproved],
                        personalCubes = 0.0
                    )
                }

            val totalUsers = users.size
            val perUserVolume = if (totalUsers > 0) totalVolume / totalUsers else 0.0
            val usersWithCubes = users.map { it.copy(personalCubes = perUserVolume) }

            WorkSummaryDTO(
                id = workId,
                workDate = dateStr,
                totalVolume = totalVolume,
                totalStacks = totalStacks,
                totalUsers = totalUsers,
                stacks = stacks,
                users = usersWithCubes,
                capturedAt = null,
                updatedAt = null,
                capturedBy = null,
                updatedBy = null
            )
        }

        // --------------------------------------------------------
        // STEP 7 — Group into WorkDaySummaryDTO
        // --------------------------------------------------------
        val groupedByDay = summaries.groupBy { it.workDate }.map { (day, works) ->
            WorkDaySummaryDTO(
                dayLabel = day,
                totalVolume = works.sumOf { it.totalVolume },
                totalStacks = works.sumOf { it.totalStacks },
                totalUsers = works.flatMap { it.users }.distinctBy { it.id }.size,
                workList = works
            )
        }

        PaginatedResponse(
            items = groupedByDay,
            page = page,
            pageSize = pageSize,
            totalCount = totalCount,
            totalPages = totalPages
        )
    }
   override fun  getDailyWorkSummariesByUser(currentUserDTO: CurrentUserDTO, page: Int,
                                     pageSize: Int,filters: WorkDataFiltersDTO)
            : PaginatedResponse<WorkDaySummaryDTO>

           = transaction {
       val startDate = filters.filterDate?.takeIf { it.isNotBlank() }
           ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }

       val endDate = filters.filterDateEndDate?.takeIf { it.isNotBlank() }
           ?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() }

       if (startDate != null && endDate != null && startDate > endDate) {
           throw IllegalStateException("Start date cannot be after end date.")
       }
       val userConditions = mutableListOf<Op<Boolean>>()
       filters.filterUserId?.let { userConditions += (AppUsers.id eq it) }
       val combinedUserCondition = userConditions.reduceOrNull { acc, op -> acc and op } ?: Op.TRUE
       val filteredWorkDoneIds = (WorkDoneUsers innerJoin AppUsers)
           .slice(WorkDoneUsers.workDoneId)
           .select(combinedUserCondition)
           .map { it[WorkDoneUsers.workDoneId].value }
           .toSet()

       if (filteredWorkDoneIds.isEmpty()) {
           return@transaction PaginatedResponse(emptyList(), page, pageSize, 0, 1)
       }

       // --------------------------------------------------------
       // STEP 4 — Date range filter logic
       // --------------------------------------------------------
       val dateFilter: Op<Boolean> = when {
           startDate != null && endDate != null ->
               WorkDone.workDate.between(startDate, endDate)
           startDate != null ->
               WorkDone.workDate greaterEq startDate
           endDate != null ->
               WorkDone.workDate less endDate
           else -> Op.TRUE
       }

       val filteredQuery = WorkDone
           .select { (WorkDone.id inList filteredWorkDoneIds) and dateFilter }
           .orderBy(WorkDone.workDate to SortOrder.DESC)

       // --------------------------------------------------------
       // STEP 5 — Pagination (same as sister function)
       // --------------------------------------------------------
       val totalCount = filteredQuery.count()
       val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()
       val pagedRows = filteredQuery
           .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
           .toList()

       // --------------------------------------------------------
       // STEP 6 — Transform into WorkSummaryDTO (copy-paste of existing logic)
       // --------------------------------------------------------
       val summaries = pagedRows.map { workRow ->
           val workId = workRow[WorkDone.id].value
           val workDateInstant = workRow[WorkDone.workDate]
           val dateStr = SimpleDateFormat("EEEE dd/MM/yyyy").format(Date.from(workDateInstant))

           val stacks = (WorkDoneStacks innerJoin Stacks innerJoin StackCodes)
               .select { WorkDoneStacks.workDoneId eq workId }
               .map { row ->
                   StackInfoDTO(
                       id = row[Stacks.id].value,
                       displayName = row[Stacks.displayName],
                       stackCodeDisplayName = row[StackCodes.displayName],
                       volume = row[Stacks.volume],
                       stackCode = row[StackCodes.displayCode])
               }

           val totalVolume = stacks.sumOf { it.volume }
           val totalStacks = stacks.size

           val users = (WorkDoneUsers innerJoin AppUsers)
               .select { WorkDoneUsers.workDoneId eq workId }
               .map { row ->
                   UserInfoDTO(
                       id = row[AppUsers.id].value,
                       name = row[AppUsers.name],
                       surname = row[AppUsers.surname],
                       phoneNumber = row[AppUsers.phoneNumber],
                       isActive = row[AppUsers.isActive],
                       isApproved = row[AppUsers.isApproved],
                       personalCubes = 0.0
                   )
               }

           val totalUsers = users.size
           val perUserVolume = if (totalUsers > 0) totalVolume / totalUsers else 0.0
           val usersWithCubes = users.map { it.copy(personalCubes = perUserVolume) }

           WorkSummaryDTO(
               id = workId,
               workDate = dateStr,
               totalVolume = totalVolume,
               totalStacks = totalStacks,
               totalUsers = totalUsers,
               stacks = stacks,
               users = usersWithCubes,
               capturedAt = null,
               updatedAt = null,
               capturedBy = null,
               updatedBy = null
           )
       }

       // --------------------------------------------------------
       // STEP 7 — Group into WorkDaySummaryDTO
       // --------------------------------------------------------
       val groupedByDay = summaries.groupBy { it.workDate }.map { (day, works) ->
           WorkDaySummaryDTO(
               dayLabel = day,
               totalVolume = works.sumOf { it.totalVolume },
               totalStacks = works.sumOf { it.totalStacks },
               totalUsers = works.flatMap { it.users }.distinctBy { it.id }.size,
               workList = works
           )
       }

       PaginatedResponse(
           items = groupedByDay,
           page = page,
           pageSize = pageSize,
           totalCount = totalCount,
           totalPages = totalPages
       )
   }
    // --------------------------------------------
    // 🆕 Weekly Approvals Implementation
    // --------------------------------------------
    override fun createWeeklyApproval(user: CurrentUserDTO, req: CreateWeeklyApprovalDTO): UUID {
        return transaction {
             checkIfBelongsToOrganization(user,req.organizationId)
            val existing = WeeklyApprovals.select {
                (WeeklyApprovals.organizationId eq req.organizationId) and
                        (WeeklyApprovals.weekStartDate eq LocalDate.parse(req.weekStartDate))
            }.firstOrNull()

            if (existing != null) {
                error("Weekly approval for this organization and week already exists.")
            }

            val newId = UUID.randomUUID()
            WeeklyApprovals.insert {
                it[id] = newId
                if (user.organizationId==null){
                     it[organizationId] = req.organizationId}
                else{
                  it[organizationId] = user.organizationId!!
                }
                it[weekStartDate] = LocalDate.parse(req.weekStartDate)
                it[weekEndDate] = LocalDate.parse(req.weekEndDate)
                it[status] = ApprovalStatus.PENDING
                it[notes] = req.notes
                it[createdAt] = Instant.now()
                it[updatedAt] = Instant.now()
                it[createdBy] = user.id
            }
            newId
        }
    }

    override fun updateWeeklyApproval(user: CurrentUserDTO, req: UpdateWeeklyApprovalDTO): Int {
        return transaction {
            WeeklyApprovals.update({ WeeklyApprovals.id eq req.id }) {
                it[status] = ApprovalStatus.valueOf(req.status.uppercase())
                it[approvedById] = user.id
                it[approvedAt] = Instant.now()
                it[notes] = req.notes
                it[updatedAt] = Instant.now()
                it[updatedBy] = user.id
            }
        }
    }
    override fun getWeeklyApprovalById(user: CurrentUserDTO, id: UUID): WeeklyApprovalDTO? {
        return transaction {
            val ApprovedByUser = AppUsers.alias("approved_by_user")

            // Alias for the user who last UPDATED the record (as in your original code)
            val UpdatedByUser = AppUsers.alias("updated_by_user")

            WeeklyApprovals
                // 1. Join for the ApprovedBy user (LEFT join because approvedById is nullable)
                .join(
                    ApprovedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.approvedById,
                    ApprovedByUser[AppUsers.id]
                )
                // 2. Join for the UpdatedBy user (already in your original code)
                .join(
                    UpdatedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.updatedBy,
                    UpdatedByUser[AppUsers.id]
                )
                .select { WeeklyApprovals.id eq id }
                .map { row ->
                    // The name and surname fields for the approved user are retrieved from the ApprovedByUser alias
                    val approvedByName = row.getOrNull(ApprovedByUser[AppUsers.name])
                    val approvedBySurname = row.getOrNull(ApprovedByUser[AppUsers.surname])

                    WeeklyApprovalDTO(
                        id = row[WeeklyApprovals.id].value,
                        organizationId = row[WeeklyApprovals.organizationId].value,
                        // Use LocalDate directly for cleaner DTO mapping if possible, otherwise toString() is fine
                        weekStartDate = row[WeeklyApprovals.weekStartDate].toString(),
                        weekEndDate = row[WeeklyApprovals.weekEndDate].toString(),
                        approvedById = row[WeeklyApprovals.approvedById]?.value,
                        approvedAt = row[WeeklyApprovals.approvedAt]?.toString(),
                        status = row[WeeklyApprovals.status].name,
                        // **FIXED: Combine name/surname from the ApprovedByUser alias**
                        approvedByFullName = if (approvedByName != null && approvedBySurname != null) {
                            "$approvedByName $approvedBySurname"
                        } else {
                            null
                        },
                        notes = row[WeeklyApprovals.notes]
                    )
                }.firstOrNull()
        }
    }

    override fun getWeeklyApprovals(
        user: CurrentUserDTO,
        page: Int,
        pageSize: Int,
        organizationId: UUID?
    ): PaginatedResponse<WeeklyApprovalDTO> {
        return transaction {
            val ApprovedByUser = AppUsers.alias("approved_by_user")

            // Alias for the user who last UPDATED the record (as in your original code)
            val UpdatedByUser = AppUsers.alias("updated_by_user")
            val baseQuery = WeeklyApprovals.join(
                    ApprovedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.approvedById,
                    ApprovedByUser[AppUsers.id]
                )
                // 2. Join for the UpdatedBy user (already in your original code)
                .join(
                    UpdatedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.updatedBy,
                    UpdatedByUser[AppUsers.id]
                ).selectAll()
                .apply {
                    if (organizationId != null) this.andWhere { WeeklyApprovals.organizationId eq organizationId }
                    else
                        this.andWhere { WeeklyApprovals.organizationId eq user.organizationId }
                }
            val totalCount = baseQuery.count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pageSize + 1).toInt()

            val items = baseQuery
                .orderBy(WeeklyApprovals.weekStartDate, SortOrder.DESC)
                .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
                .map { row ->
                    val approvedByName = row.getOrNull(ApprovedByUser[AppUsers.name])
                    val approvedBySurname = row.getOrNull(ApprovedByUser[AppUsers.surname])
                    WeeklyApprovalDTO(
                        id = row[WeeklyApprovals.id].value,
                        organizationId = row[WeeklyApprovals.organizationId].value,
                        weekStartDate = row[WeeklyApprovals.weekStartDate].toString(),
                        weekEndDate = row[WeeklyApprovals.weekEndDate].toString(),
                        approvedById = row[WeeklyApprovals.approvedById]?.value,
                        approvedAt = row[WeeklyApprovals.approvedAt]?.toString(),
                        status = row[WeeklyApprovals.status].name,
                        approvedByFullName = if (approvedByName != null && approvedBySurname != null) {
                            "$approvedByName $approvedBySurname"
                        } else {
                            // Return null or empty string if no approver is set (due to LEFT JOIN)
                            null
                        },
                        notes = row[WeeklyApprovals.notes]
                    )
                }

            PaginatedResponse(
                items = items,
                page = page,
                pageSize = pageSize,
                totalCount = totalCount,
                totalPages = totalPages
            )
        }
    }

    override fun getLatestWeeklyApproval(user: CurrentUserDTO): WeeklyApprovalDTO? {
        return transaction {
            val ApprovedByUser = AppUsers.alias("approved_by_user")
            val UpdatedByUser = AppUsers.alias("updated_by_user")

            WeeklyApprovals.join(
                    ApprovedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.approvedById,
                    ApprovedByUser[AppUsers.id]
                )
                // 2. Join for the UpdatedBy user (already in your original code)
                .join(
                    UpdatedByUser,
                    JoinType.LEFT,
                    WeeklyApprovals.updatedBy,
                    UpdatedByUser[AppUsers.id]
                ).
            select { WeeklyApprovals.organizationId eq user.organizationId }
                .orderBy(WeeklyApprovals.weekStartDate, SortOrder.DESC)
                .limit(1)
                .map { row ->
                    val approvedByName = row.getOrNull(ApprovedByUser[AppUsers.name])
                    val approvedBySurname = row.getOrNull(ApprovedByUser[AppUsers.surname])
                    WeeklyApprovalDTO(
                        id = row[WeeklyApprovals.id].value,
                        organizationId = row[WeeklyApprovals.organizationId].value,
                        weekStartDate = row[WeeklyApprovals.weekStartDate].toString(),
                        weekEndDate = row[WeeklyApprovals.weekEndDate].toString(),
                        approvedById = row[WeeklyApprovals.approvedById]?.value,
                        approvedAt = row[WeeklyApprovals.approvedAt]?.toString(),
                        status = row[WeeklyApprovals.status].name,
                        notes = row[WeeklyApprovals.notes],
                        approvedByFullName = if (approvedByName != null && approvedBySurname != null) {
                            "$approvedByName $approvedBySurname"
                        } else {
                            null
                        }
                    )
                }.firstOrNull()
        }
    }
}