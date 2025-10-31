package org.montigny.client.Report

import java.time.Instant
import java.util.*

data class WorkDoneVolumeDTO(val workDoneId: UUID, val totalVolumePerWorkDone: Double)
data class WorkDoneUserCountDTO(val workDoneId: UUID, val contributingUsers: Int)
data class DepartmentCountDTO(val workDoneId: UUID, val departmentId: UUID, val departmentCount: Int)

data class FactWorkDoneDTO(
    val workDoneId: UUID, val workDate: Instant, val volume: Double, val usersPerWorkDone: Int,
    val departmentId: UUID, val departmentCount: Int, val departmentVolume: Double, val avgCubesPerPerson: Double
)
data class FactWorkDoneForSectionDTO(
    val workDoneId: UUID, val workDate: Instant, val volume: Double, val usersPerWorkDone: Int,
    val sectionId: UUID, val sectionCount: Int, val sectionVolume: Double
)

// --- Dimension DTOs ---

data class DimDepartmentDTO(
    val id: UUID, val displayname: String, val organizationId: UUID,
    val createdAt: Instant, val updatedAt: Instant, val createdBy: UUID?, val updatedBy: UUID?
)

data class DimOrganizationDTO(
    val id: UUID, val displayname: String,
    val createdAt: Instant, val updatedAt: Instant, val createdBy: UUID?, val updatedBy: UUID?
)

data class DimSectionDTO(
    val id: UUID, val displayname: String, val departmentId: UUID,
    val createdAt: Instant, val updatedAt: Instant, val createdBy: UUID?, val updatedBy: UUID?
)

data class DimStackDTO(
    val id: UUID, val length: Float, val height: Float, val thickness: Float,
    val displayName: String, val stackCodeId: UUID, val numberOfPieces: Int,
    val volume: Double, val displayCode: String, val stackCodeDisplayName: String
)

data class DimUserDTO(
    val id: UUID, val name: String, val surname: String, val phoneNumber: String,
    val secondaryPhoneNumber: String?, val isApproved: Boolean, val isActive: Boolean,
    val userTypeId: UUID?, val organizationId: UUID?, val departmentId: UUID?,
    val sectionId: UUID?, val email: String?, val organizationName: String?, val userType: String?
)



data class AllReportData(
    val cubesPerWorkDone: List<WorkDoneVolumeDTO>,
    val usersPerWorkDone: List<WorkDoneUserCountDTO>,
    val departmentCountPerWordDone: List<DepartmentCountDTO>,
    val dimDepartment: List<DimDepartmentDTO>,
    val dimOrganization: List<DimOrganizationDTO>,
    val dimSection: List<DimSectionDTO>,
    val dimStack: List<DimStackDTO>,
    val dimUser: List<DimUserDTO>,
    val factWorkDone: List<FactWorkDoneDTO>,
    val factWorkDoneForSection: List<FactWorkDoneForSectionDTO>
)


data class FactWorkDoneByUserDTO(
    val workDoneId: UUID,
    val userId: UUID,
    val userName: String,
    val userSurname: String,
    val userPhoneNumber: String,
    val cubesPerWorkDone: Double, // This is the total volume for the workDone
    val contributingUsers: Int,    // This is the total user count for the workDone
    val workDate: Instant
)