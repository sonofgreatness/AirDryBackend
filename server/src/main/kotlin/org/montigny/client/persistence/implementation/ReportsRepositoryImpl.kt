package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import org.jetbrains.exposed.sql.SqlExpressionBuilder.coalesce
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.Report.*
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.persistence.*
import org.montigny.client.persistence.repository.ReportsRepository
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*


class ReportsRepositoryImpl() : ReportsRepository {
    override fun getFactWorkDoneByUser(startDate: String, endDate: String): List<FactWorkDoneByUserDTO> =transaction{
        val intermediateData = getIntermediateReportData()
        val cubesPerWorkDoneMap = intermediateData.cubesPerWorkDoneList.associateBy { it.workDoneId }
        val usersPerWorkDoneMap = intermediateData.usersPerWorkDoneList.associateBy { it.workDoneId }

        val startDateInstant = startDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }

        val endDateInstant = endDate?.takeIf { it.isNotBlank() }
            ?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() }

        if (startDateInstant != null && endDateInstant != null && startDateInstant > endDateInstant) {
            throw IllegalStateException("Start date cannot be after end date.")
        }
        val dateFilter: Op<Boolean> = when {
            startDateInstant != null && endDateInstant != null ->
                WorkDone.workDate.between(startDateInstant, endDateInstant)
            startDateInstant != null ->
                WorkDone.workDate greaterEq startDateInstant
            endDateInstant != null ->
                WorkDone.workDate less endDateInstant
            else -> Op.TRUE
        }
         WorkDoneUsers
            // 1. Join to AppUsers for user details (UserID, UserName, etc.)
            .join(AppUsers, JoinType.INNER, WorkDoneUsers.appUserId, AppUsers.id)
            // 2. Join to WorkDone for the work date and date filtering
            .join(WorkDone, JoinType.INNER, WorkDoneUsers.workDoneId, WorkDone.id)
            .select { dateFilter }
            .mapNotNull { row ->
                val workDoneId = row[WorkDoneUsers.workDoneId].value
                val volumeData = cubesPerWorkDoneMap[workDoneId]
                val userData = usersPerWorkDoneMap[workDoneId]
                // Data must exist in the aggregated lists
                if (volumeData == null || userData == null) return@mapNotNull null
                FactWorkDoneByUserDTO(
                    workDoneId = workDoneId,
                    userId = row[AppUsers.id].value,
                    userName = row[AppUsers.name],
                    userSurname = row[AppUsers.surname],
                    userPhoneNumber = row[AppUsers.phoneNumber],
                    cubesPerWorkDone = volumeData.totalVolumePerWorkDone,
                    contributingUsers = userData.contributingUsers,
                    workDate = row[WorkDone.workDate]
                )
            }
    }
    private data class IntermediateReportData(
        val cubesPerWorkDoneList: List<WorkDoneVolumeDTO>,
        val usersPerWorkDoneList: List<WorkDoneUserCountDTO>,
        val departmentCountList: List<DepartmentCountDTO>
    )

    private fun getIntermediateReportData(): IntermediateReportData {
        return IntermediateReportData(
            cubesPerWorkDoneList = getCubesPerWorkDone(),
            usersPerWorkDoneList = getUsersPerWorkDone(),
            departmentCountList = getDepartmentCountPerWorkDone()
        )
    }

    // --- Master Report Function ---
   override fun generateAllReportData(
        startDate: String,
        endDate: String,
    ): AllReportData {
        return transaction {
            val startDateInstant = startDate?.takeIf { it.isNotBlank() }
                ?.let { LocalDate.parse(it).atStartOfDay(ZoneOffset.UTC).toInstant() }

            val endDateInstant = endDate?.takeIf { it.isNotBlank() }
                ?.let { LocalDate.parse(it).plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant() }

            if (startDateInstant != null && endDateInstant != null && startDateInstant > endDateInstant) {
                throw IllegalStateException("Start date cannot be after end date.")
            }
            // 1. Get Intermediate Data
            val intermediateData = getIntermediateReportData()
            val cubesPerWorkDoneList = intermediateData.cubesPerWorkDoneList
            val usersPerWorkDoneList = intermediateData.usersPerWorkDoneList
            val departmentCountList = intermediateData.departmentCountList

            val cubesPerWorkDoneMap = cubesPerWorkDoneList.associateBy { it.workDoneId }
            val usersPerWorkDoneMap = usersPerWorkDoneList.associateBy { it.workDoneId }

            // 2. Get Fact Data
            val factWorkDone = getFactWorkDone(startDateInstant,endDateInstant,cubesPerWorkDoneMap, usersPerWorkDoneMap, departmentCountList)
            val factWorkDoneForSection = getFactWorkDoneForSection(cubesPerWorkDoneMap, usersPerWorkDoneMap)

            // 3. Get Dimension Data
            val dimDepartment = getDimDepartment()
            val dimOrganization = getDimOrganization()
            val dimSection = getDimSection()
            val dimStack = getDimStack()
            val dimUser = getDimUser()

            // 4. Return all data
            AllReportData(
                cubesPerWorkDone = cubesPerWorkDoneList,
                usersPerWorkDone = usersPerWorkDoneList,
                departmentCountPerWordDone = departmentCountList,
                dimDepartment = dimDepartment,
                dimOrganization = dimOrganization,
                dimSection = dimSection,
                dimStack = dimStack,
                dimUser = dimUser,
                factWorkDone = factWorkDone,
                factWorkDoneForSection = factWorkDoneForSection
            )
        }
    }

    private fun getFactWorkDone(
        startDate: Instant?,
        endDate: Instant?,
        cubesPerWorkDone: Map<UUID, WorkDoneVolumeDTO>,
        usersPerWorkDone: Map<UUID, WorkDoneUserCountDTO>,
        departmentCounts: List<DepartmentCountDTO>
    ): List<FactWorkDoneDTO> {

        val dateFilter: Op<Boolean> = when {
            startDate != null && endDate != null ->
                WorkDone.workDate.between(startDate, endDate)
            startDate != null ->
                WorkDone.workDate greaterEq startDate
            endDate != null ->
                WorkDone.workDate less endDate
            else -> Op.TRUE
        }
        val deptCountMap = departmentCounts.groupBy { it.workDoneId }

        return WorkDone.select{
            dateFilter}
            .mapNotNull { row ->
                val workDoneId = row[WorkDone.id].value
                val volumeData = cubesPerWorkDone[workDoneId]
                val userData = usersPerWorkDone[workDoneId]

                if (volumeData == null || userData == null) return@mapNotNull null

                val totalVolume = volumeData.totalVolumePerWorkDone
                val totalUsers = userData.contributingUsers
                val avgCubesPerPerson = if (totalUsers > 0) totalVolume / totalUsers else 0.0

                deptCountMap[workDoneId]?.map { deptCount ->
                    val departmentCount = deptCount.departmentCount
                    // DepartmentVolume = (DepartmentCount / UsersPerWorkDone) * Volume
                    val departmentVolume = (departmentCount.toDouble() / totalUsers) * totalVolume

                    FactWorkDoneDTO(
                        workDoneId, row[WorkDone.workDate], totalVolume, totalUsers,
                        deptCount.departmentId, departmentCount, departmentVolume, avgCubesPerPerson
                    )
                }
            }.flatten()
    }

    private fun getFactWorkDoneForSection(
        cubesPerWorkDone: Map<UUID, WorkDoneVolumeDTO>,
        usersPerWorkDone: Map<UUID, WorkDoneUserCountDTO>
    ): List<FactWorkDoneForSectionDTO> {

        // Step 1: Count users per WorkDone per Section
        val sectionCounts: Map<Pair<UUID, UUID>, Long> = WorkDoneUsers
            .join(AppUsers, JoinType.INNER, WorkDoneUsers.appUserId, AppUsers.id)
            .slice(WorkDoneUsers.workDoneId, AppUsers.sectionId)
            .selectAll()
            .groupBy(WorkDoneUsers.workDoneId, AppUsers.sectionId).associate { row ->
                val workDoneId = row[WorkDoneUsers.workDoneId].value
                val sectionId = row[AppUsers.sectionId]?.value
                val count = WorkDoneUsers
                    .select {
                        (WorkDoneUsers.workDoneId eq workDoneId) and (WorkDoneUsers.appUserId inSubQuery
                                AppUsers.slice(AppUsers.id).select { AppUsers.sectionId eq sectionId })
                    }
                    .count()
                (workDoneId to sectionId!!) to count
            }

        // Step 2: Build FactWorkDoneForSectionDTO
        return WorkDone.selectAll()
            .mapNotNull { row ->
                val workDoneId = row[WorkDone.id].value
                val volumeData = cubesPerWorkDone[workDoneId]
                val userData = usersPerWorkDone[workDoneId]

                if (volumeData == null || userData == null) return@mapNotNull null

                val totalVolume = volumeData.totalVolumePerWorkDone
                val totalUsers = userData.contributingUsers

                sectionCounts.filterKeys { it.first == workDoneId }
                    .map { (key, sectionCount) ->
                        val sectionId = key.second
                        val sectionVolume = if (totalUsers > 0)
                            (sectionCount.toDouble() / totalUsers) * totalVolume
                        else 0.0

                        FactWorkDoneForSectionDTO(
                            workDoneId = workDoneId,
                            workDate = row[WorkDone.workDate],
                            volume = totalVolume,
                            usersPerWorkDone = totalUsers,
                            sectionId = sectionId,
                            sectionCount = sectionCount.toInt(),
                            sectionVolume = sectionVolume
                        )
                    }
            }.flatten()
    }
    override  fun  createMonthlyReport(user: CurrentUserDTO,
                             fileName:String,
                             fileData:ByteArray) : UUID = transaction{
        MonthlyReport.insertAndGetId {
            it[organizationId] =user.organizationId
            it[this.fileData] = fileData
            it[uploadTime] = Instant.now()
            it[this.fileName] = fileName
        }.value
    }
    private fun getCubesPerWorkDone(): List<WorkDoneVolumeDTO> {
        val totalVolume = Stacks.volume.sum().alias("TotalVolumePerWorkDone")
        return WorkDoneStacks
            .join(Stacks, JoinType.INNER, WorkDoneStacks.stackId, Stacks.id)
            .slice(WorkDoneStacks.workDoneId, totalVolume)
            .selectAll()
            .groupBy(WorkDoneStacks.workDoneId)
            .map { row ->
                WorkDoneVolumeDTO(
                    workDoneId = row[WorkDoneStacks.workDoneId].value,
                    totalVolumePerWorkDone = row[totalVolume] ?: 0.0
                )
            }
    }
    private fun getUsersPerWorkDone(): List<WorkDoneUserCountDTO> {
        val contributingUsers = WorkDoneUsers.appUserId.countDistinct().alias("ContributingUsers")
        return WorkDoneUsers
            .slice(WorkDoneUsers.workDoneId, contributingUsers)
            .selectAll()
            .groupBy(WorkDoneUsers.workDoneId)
            .map { row ->
                WorkDoneUserCountDTO(
                    workDoneId = row[WorkDoneUsers.workDoneId].value,
                    contributingUsers = row[contributingUsers]?.toInt() ?: 0
                )
            }
    }



   private fun getDepartmentCountPerWorkDone(): List<DepartmentCountDTO> = transaction {
        val defaultUuid = "00000000-0000-0000-0000-000000000000"

        val sql = """
        SELECT wdu.work_done_id,
               COALESCE(au.department_id::text, '$defaultUuid') as department_id,
               COUNT(DISTINCT au.id) as department_count
        FROM work_done_user wdu
        INNER JOIN app_user au ON wdu.app_user_id = au.id
        GROUP BY wdu.work_done_id, department_id
    """.trimIndent()

        exec(sql) { rs ->
            val result = mutableListOf<DepartmentCountDTO>()
            while (rs.next()) {
                result.add(
                    DepartmentCountDTO(
                        workDoneId = UUID.fromString(rs.getString("work_done_id")),
                        departmentId = UUID.fromString(rs.getString("department_id")),
                        departmentCount = rs.getInt("department_count")
                    )
                )
            }
            result
        } ?: emptyList()
    }
    /*private fun getDepartmentCountPerWorkDone(): List<DepartmentCountDTO> {

        // 1. Default UUID for missing departmentIds
        val defaultDepartmentId = UUID.fromString("00000000-0000-0000-0000-000000000000")

        // 2. Wrap it in an EntityID so Exposed can handle it in expressions
        val defaultEntityId = EntityID(defaultDepartmentId, Departments)

        // 3. Coalesce the departmentId column with the default EntityID
        val coalescedDepartmentId = coalesce(AppUsers.departmentId, literal(defaultEntityId))
            .alias("CoalescedDepartmentId")

        // 4. Count distinct AppUsers.id per group
        val departmentCount = AppUsers.id.countDistinct().alias("DepartmentCount")

        // 5. Build the query
        return WorkDoneUsers
            .join(AppUsers, JoinType.INNER, WorkDoneUsers.appUserId, AppUsers.id)
            .slice(WorkDoneUsers.workDoneId, coalescedDepartmentId, departmentCount)
            .selectAll()
            .groupBy(WorkDoneUsers.workDoneId, coalescedDepartmentId)
            .map { row ->
                DepartmentCountDTO(
                    workDoneId = row[WorkDoneUsers.workDoneId].value,
                    departmentId = row[coalescedDepartmentId].value, // unwrap EntityID<UUID>
                    departmentCount = row[departmentCount]?.toInt() ?: 0
                )
            }
    }*/
    // --- Dimension Queries ---

    private fun getDimDepartment(): List<DimDepartmentDTO> {
        return Departments.selectAll().map {
            DimDepartmentDTO(
                it[Departments.id].value, it[Departments.displayname], it[Departments.organizationId].value,
                it[Departments.createdAt], it[Departments.updatedAt], it[Departments.createdBy], it[Departments.updatedBy]
            )
        }
    }

    private fun getDimOrganization(): List<DimOrganizationDTO> {
        return Organizations.selectAll().map {
            DimOrganizationDTO(
                it[Organizations.id].value, it[Organizations.displayname],
                it[Organizations.createdAt], it[Organizations.updatedAt], it[Organizations.createdBy], it[Organizations.updatedBy]
            )
        }
    }

    private fun getDimSection(): List<DimSectionDTO> {
        return Sections.selectAll().map {
            DimSectionDTO(
                it[Sections.id].value, it[Sections.displayname], it[Sections.departmentId].value,
                it[Sections.createdAt], it[Sections.updatedAt], it[Sections.createdBy], it[Sections.updatedBy]
            )
        }
    }

    private fun getDimStack(): List<DimStackDTO> {
        return Stacks.join(StackCodes, JoinType.LEFT, Stacks.stackCodeId, StackCodes.id)
            .selectAll()
            .map {
                DimStackDTO(
                    it[Stacks.id].value, it[Stacks.length], it[Stacks.height], it[Stacks.thickness],
                    it[Stacks.displayName], it[Stacks.stackCodeId].value, it[Stacks.numberOfPieces],
                    it[Stacks.volume], it[StackCodes.displayCode], it[StackCodes.displayName]
                )
            }
    }

    private fun getDimUser(): List<DimUserDTO> {
        // Simulating the joins to Organizations and UserTypes
        return AppUsers.join(Organizations, JoinType.LEFT, AppUsers.organizationId, Organizations.id)
            .join(UserTypes, JoinType.LEFT, AppUsers.userTypeId, UserTypes.id)
            .selectAll()
            .map {
                DimUserDTO(
                    it[AppUsers.id].value, it[AppUsers.name], it[AppUsers.surname], it[AppUsers.phoneNumber],
                    it[AppUsers.nationalId], it[AppUsers.isApproved], it[AppUsers.isActive],
                    it[AppUsers.userTypeId]?.value, it[AppUsers.organizationId]?.value, it[AppUsers.departmentId]?.value,
                    it[AppUsers.sectionId]?.value, it[AppUsers.email],
                    it.getOrNull(Organizations.displayname), // OrganizationName
                    it.getOrNull(UserTypes.displayname)      // UserType
                )
            }
    }

    // --- Fact Queries ---



    override  fun  getMonthlyReportById(user: CurrentUserDTO, id : UUID): GetMonthlyReportDTO?
    = transaction{
        MonthlyReport.select {
            MonthlyReport.id eq id
        }.map {
            val dateInstant = it[MonthlyReport.uploadTime]
            val dateStr = SimpleDateFormat("EEEE dd/MM/yyyy").format(Date.from(dateInstant))
            GetMonthlyReportDTO(
                id  = it[MonthlyReport.id].value,
                fileData = it[MonthlyReport.fileData],
                fileName = it[MonthlyReport.fileName],
                uploadTime = dateStr
            )
        }.singleOrNull()
  }
    override  fun getAllMonthlyReports(user: CurrentUserDTO, page :Int , pageSize :Int): PaginatedResponse<GetMonthlyReportDTO>
    = transaction{
      val filteredQuery = MonthlyReport
          .select { MonthlyReport.organizationId eq user.organizationId  }
          .orderBy(MonthlyReport.uploadTime to SortOrder.DESC)

      val totalCount = filteredQuery.count()
      val totalPages = if (totalCount == 0L) 1 else ((totalCount + pageSize - 1) / pageSize).toInt()
      val pagedRows = filteredQuery
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .toList()
       val  items =
           pagedRows.map {
               val dateInstant = it[MonthlyReport.uploadTime]
               val dateStr = SimpleDateFormat("EEEE dd/MM/yyyy").format(Date.from(dateInstant))

               GetMonthlyReportDTO(
                   id  = it[MonthlyReport.id].value,
                   fileData = it[MonthlyReport.fileData],
                   fileName = it[MonthlyReport.fileName],
                   uploadTime = dateStr

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