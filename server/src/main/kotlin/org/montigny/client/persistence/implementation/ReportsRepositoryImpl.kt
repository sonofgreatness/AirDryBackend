package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.count
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.Report.AllReportData
import org.montigny.client.Report.DepartmentCountDTO
import org.montigny.client.Report.DimDepartmentDTO
import org.montigny.client.Report.DimOrganizationDTO
import org.montigny.client.Report.DimSectionDTO
import org.montigny.client.Report.DimStackDTO
import org.montigny.client.Report.DimUserDTO
import org.montigny.client.Report.FactWorkDoneDTO
import org.montigny.client.Report.FactWorkDoneForSectionDTO
import org.montigny.client.Report.GetMonthlyReportDTO
import org.montigny.client.Report.WorkDoneUserCountDTO
import org.montigny.client.Report.WorkDoneVolumeDTO
import org.montigny.client.dto.Common.PaginatedResponse
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.persistence.AppUsers
import org.montigny.client.persistence.Departments
import org.montigny.client.persistence.MonthlyReport
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.Sections
import org.montigny.client.persistence.StackCodes
import org.montigny.client.persistence.Stacks
import org.montigny.client.persistence.UserTypes
import org.montigny.client.persistence.WorkDone
import org.montigny.client.persistence.WorkDoneStacks
import org.montigny.client.persistence.WorkDoneUsers
import org.montigny.client.persistence.repository.ReportsRepository
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.util.Date
import java.util.UUID

class ReportsRepositoryImpl() : ReportsRepository {



    // --- Master Report Function ---

   override fun generateAllReportData(
        startDate: String,
        endDate: String,
    ): AllReportData {
        return transaction {
            // 1. Get Intermediate Data
            val cubesPerWorkDoneList = getCubesPerWorkDone()
            val usersPerWorkDoneList = getUsersPerWorkDone()
            val departmentCountList = getDepartmentCountPerWorkDone()

            val cubesPerWorkDoneMap = cubesPerWorkDoneList.associateBy { it.workDoneId }
            val usersPerWorkDoneMap = usersPerWorkDoneList.associateBy { it.workDoneId }

            // 2. Get Fact Data
            val factWorkDone = getFactWorkDone(cubesPerWorkDoneMap, usersPerWorkDoneMap, departmentCountList)
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

    private fun getDepartmentCountPerWorkDone(): List<DepartmentCountDTO> {
        val departmentCount = AppUsers.id.countDistinct().alias("DepartmentCount")

        return WorkDoneUsers
            .join(AppUsers, JoinType.INNER, WorkDoneUsers.appUserId, AppUsers.id)
            .slice(WorkDoneUsers.workDoneId, AppUsers.departmentId, departmentCount)
            .selectAll()
            .groupBy(WorkDoneUsers.workDoneId, AppUsers.departmentId)
            .map { row ->
                DepartmentCountDTO(
                    workDoneId = row[WorkDoneUsers.workDoneId].value,
                    departmentId = row[AppUsers.departmentId]!!.value,
                    departmentCount = row[departmentCount]?.toInt() ?: 0
                )
            }
    }
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
                    it[AppUsers.secondaryPhone], it[AppUsers.isApproved], it[AppUsers.isActive],
                    it[AppUsers.userTypeId]?.value, it[AppUsers.organizationId]?.value, it[AppUsers.departmentId]?.value,
                    it[AppUsers.sectionId]?.value, it[AppUsers.email],
                    it.getOrNull(Organizations.displayname), // OrganizationName
                    it.getOrNull(UserTypes.displayname)      // UserType
                )
            }
    }

    // --- Fact Queries ---

    private fun getFactWorkDone(
        cubesPerWorkDone: Map<UUID, WorkDoneVolumeDTO>,
        usersPerWorkDone: Map<UUID, WorkDoneUserCountDTO>,
        departmentCounts: List<DepartmentCountDTO>
    ): List<FactWorkDoneDTO> {
        val deptCountMap = departmentCounts.groupBy { it.workDoneId }

        return WorkDone.selectAll()
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