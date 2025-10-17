package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.Stacks
import org.montigny.client.persistence.WorkDone
import org.montigny.client.persistence.WorkDoneStacks
import org.montigny.client.persistence.WorkDoneUsers
import org.montigny.client.persistence.repository.WorkRepository
import org.montigny.client.util.UserUtils
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.UUID

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

        // --- 2️⃣ Validate user IDs ---
        val allUserIds = req.workersList.flatMap { it.userIds }
        val missingUsers = allUserIds.filterNot { uid ->
            AppUsers.select { AppUsers.id eq uid }.any()
        }

        if (missingUsers.isNotEmpty()) {
            error("Some users not found: ${missingUsers.joinToString()}")
        }

        // --- 3️⃣ Validate stack IDs ---
        val allStackIds = req.stacksList.flatMap { it.stackId }
        val missingStacks = allStackIds.filterNot { sid ->
            Stacks.select { Stacks.id eq sid }.any()
        }

        if (missingStacks.isNotEmpty()) {
            error("Some stacks not found: ${missingStacks.joinToString()}")
        }
        // --- 4️⃣ Insert work_done_user links ---
        allUserIds.distinct().forEach { userId ->
            WorkDoneUsers.insert {
                it[this.workDoneId] = EntityID(workDoneId, WorkDone)
                it[appUserId] = EntityID(userId, AppUsers)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        // --- 5️⃣ Insert work_done_stack links ---
        allStackIds.distinct().forEach { stackId ->
            WorkDoneStacks.insert {
                it[this.workDoneId] = EntityID(workDoneId, WorkDone)
                it[this.stackId] = EntityID(stackId, Stacks)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        // --- 6️⃣ Return the new WorkDone ID ---
         workDoneId
    }
    override fun updateWork(user: CurrentUserDTO, req: CreateWorkDoneDTO): Int = transaction {

        // --- 1️⃣ Check existence of WorkDone record ---
        val existing = WorkDone.select { WorkDone.id eq req.id }.singleOrNull()
            ?: error("Work record not found for ID: ${req.id}")

        val myCreatedAt = existing[WorkDone.createdAt]
        val now = Instant.now()

        if (UserUtils.isAdminUser(user) &&
            myCreatedAt.isBefore(now.minus(7, ChronoUnit.DAYS))) {
            error("Admin cannot update work older than 7 days")
        }

        if (UserUtils.isCapturerUser(user) &&
            myCreatedAt.isBefore(now.minus(2, ChronoUnit.DAYS))) {
            error("Capturer cannot update work older than 2 days")
        }

        // --- 2️⃣ Update main record ---
        val updatedRows = WorkDone.update({ WorkDone.id eq req.id }) {
            it[workDate] = req.workDate.toInstant()
            it[updatedBy] = user.id
            it[updatedAt] = Instant.now()
        }
        // --- 3️⃣ Validate user IDs --
        val allUserIds = req.workersList.flatMap { it.userIds }
        val missingUsers = allUserIds.filterNot { uid ->
            AppUsers.select { AppUsers.id eq uid }.any()
        }
        if (missingUsers.isNotEmpty()) {
            error("Some users not found: ${missingUsers.joinToString()}")
        }

        // --- 4️⃣ Validate stack IDs ---
        val allStackIds = req.stacksList.flatMap { it.stackId }
        val missingStacks = allStackIds.filterNot { sid ->
            Stacks.select { Stacks.id eq sid }.any()
        }
        if (missingStacks.isNotEmpty()) {
            error("Some stacks not found: ${missingStacks.joinToString()}")
        }
        // --- 5️⃣ Clear existing relations ---
        WorkDoneUsers.deleteWhere { WorkDoneUsers.workDoneId eq EntityID(req.id, WorkDone) }
        WorkDoneStacks.deleteWhere { WorkDoneStacks.workDoneId eq EntityID(req.id, WorkDone) }

        // --- 6️⃣ Re-insert updated work_done_user relations ---
        allUserIds.distinct().forEach { userId ->
            WorkDoneUsers.insert {
                it[workDoneId] = EntityID(req.id, WorkDone)
                it[appUserId] = EntityID(userId, AppUsers)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        // --- 7️⃣ Re-insert updated work_done_stack relations ---
        allStackIds.distinct().forEach { stackId ->
            WorkDoneStacks.insert {
                it[workDoneId] = EntityID(req.id, WorkDone)
                it[this.stackId] = EntityID(stackId, Stacks)
                it[createdBy] = user.id
                it[createdAt] = Instant.now()
            }
        }
        // --- 8️⃣ Return number of updated rows ---
        updatedRows
    }
    override  fun  deleteWorkById (user: CurrentUserDTO, id: UUID): Int =transaction{
        // --- 1️⃣ Check existence of WorkDone record ---
        val existing = WorkDone.select { WorkDone.id eq id }.singleOrNull()
            ?: error("Work record not found for ID: ${id}")

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
                stackId = stackIds
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

}