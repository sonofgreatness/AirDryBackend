package org.montigny.client.service

import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.montigny.client.Report.AllReportData
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.persistence.repository.ReportsRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class ReportsService(
    private val reportsRepo: ReportsRepository,
) {

    fun generateExcelReport(startDate: String,endDate: String) : ByteArray
  {
        val data : AllReportData = reportsRepo.generateAllReportData(startDate, endDate)
        val workbook = XSSFWorkbook()
        // Helper function to create sheets for different data types
        fun <T> createSheet(sheetName: String, list: List<T>, headers: List<String>, rowMapper: (T, Row, Workbook) -> Unit) {
            val sheet = workbook.createSheet(sheetName)

            // 1. Create Header Row
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, title ->
                headerRow.createCell(index).setCellValue(title)
            }

            // 2. Populate Data Rows
            list.forEachIndexed { rowIndex, item ->
                val row = sheet.createRow(rowIndex + 1)
                rowMapper(item, row, workbook)
            }

            // 3. Auto-size columns
            headers.indices.forEach { sheet.autoSizeColumn(it) }
        }

        // --- Workbook Creation Logic for ALL Tables ---

        // Intermediate Tables
        createSheet("CubesPerWorkDone", data.cubesPerWorkDone, listOf("WorkDoneID", "TotalVolumePerWorkDone")) { dto, row, _ ->
            row.createCell(0).setCellValue(dto.workDoneId.toString())
            row.createCell(1).setCellValue(dto.totalVolumePerWorkDone)
        }

        createSheet("UsersPerWorkDone", data.usersPerWorkDone, listOf("WorkDoneID", "ContributingUsers")) { dto, row, _ ->
            row.createCell(0).setCellValue(dto.workDoneId.toString())
            row.createCell(1).setCellValue(dto.contributingUsers.toDouble())
        }

        createSheet("DepartmentCountPerWordDone", data.departmentCountPerWordDone, listOf("WorkDoneID", "DepartmentID", "DepartmentCount")) { dto, row, _ ->
            row.createCell(0).setCellValue(dto.workDoneId.toString())
            row.createCell(1).setCellValue(dto.departmentId.toString())
            row.createCell(2).setCellValue(dto.departmentCount.toDouble())
        }

        // Dimension Tables
        createSheet("DimOrganization", data.dimOrganization, listOf("id", "displayname", "created_at", "updated_at", "created_by", "updated_by")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.id.toString())
            row.createCell(1).setCellValue(dto.displayname)
            createDateCell(row, 2, dto.createdAt, wb)
            createDateCell(row, 3, dto.updatedAt, wb)
            row.createCell(4).setCellValue(dto.createdBy?.toString() ?: "")
            row.createCell(5).setCellValue(dto.updatedBy?.toString() ?: "")
        }

        createSheet("DimDepartment", data.dimDepartment, listOf("id", "displayname", "organization_id", "created_at", "updated_at", "created_by", "updated_by")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.id.toString())
            row.createCell(1).setCellValue(dto.displayname)
            row.createCell(2).setCellValue(dto.organizationId.toString())
            createDateCell(row, 3, dto.createdAt, wb)
            createDateCell(row, 4, dto.updatedAt, wb)
            row.createCell(5).setCellValue(dto.createdBy?.toString() ?: "")
            row.createCell(6).setCellValue(dto.updatedBy?.toString() ?: "")
        }

        createSheet("DimSection", data.dimSection, listOf("id", "displayname", "department_id", "created_at", "updated_at", "created_by", "updated_by")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.id.toString())
            row.createCell(1).setCellValue(dto.displayname)
            row.createCell(2).setCellValue(dto.departmentId.toString())
            createDateCell(row, 3, dto.createdAt, wb)
            createDateCell(row, 4, dto.updatedAt, wb)
            row.createCell(5).setCellValue(dto.createdBy?.toString() ?: "")
            row.createCell(6).setCellValue(dto.updatedBy?.toString() ?: "")
        }

        createSheet("DimStack", data.dimStack, listOf("id", "length", "height", "thickness", "display_name", "stack_code_id", "number_of_pieces", "volume", "display_code", "stack_code_display_name")) { dto, row, _ ->
            row.createCell(0).setCellValue(dto.id.toString())
            row.createCell(1).setCellValue(dto.length.toDouble())
            row.createCell(2).setCellValue(dto.height.toDouble())
            row.createCell(3).setCellValue(dto.thickness.toDouble())
            row.createCell(4).setCellValue(dto.displayName)
            row.createCell(5).setCellValue(dto.stackCodeId.toString())
            row.createCell(6).setCellValue(dto.numberOfPieces.toDouble())
            row.createCell(7).setCellValue(dto.volume)
            row.createCell(8).setCellValue(dto.displayCode)
            row.createCell(9).setCellValue(dto.stackCodeDisplayName)
        }

        createSheet("DimUser", data.dimUser, listOf("id", "name", "surname", "phone_number", "secondary_phone_number", "is_approved", "is_active", "user_type_id", "organization_id", "department_id", "section_id", "email", "OrganizationName", "UserType")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.id.toString())
            row.createCell(1).setCellValue(dto.name)
            row.createCell(2).setCellValue(dto.surname)
            row.createCell(3).setCellValue(dto.phoneNumber)
            row.createCell(4).setCellValue(dto.secondaryPhoneNumber ?: "")
            row.createCell(5).setCellValue(dto.isApproved)
            row.createCell(6).setCellValue(dto.isActive)
            row.createCell(7).setCellValue(dto.userTypeId?.toString() ?: "")
            row.createCell(8).setCellValue(dto.organizationId?.toString() ?: "")
            row.createCell(9).setCellValue(dto.departmentId?.toString() ?: "")
            row.createCell(10).setCellValue(dto.sectionId?.toString() ?: "")
            row.createCell(11).setCellValue(dto.email ?: "")
            row.createCell(12).setCellValue(dto.organizationName ?: "")
            row.createCell(13).setCellValue(dto.userType ?: "")
        }

        // Fact Tables
        createSheet("FactWorkDone", data.factWorkDone, listOf("WorkDoneID", "WorkDate", "Volume", "UsersPerWorkDone", "DepartmentID", "DepartmentCount", "DepartmentVolume", "AvgCubesPerPerson")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.workDoneId.toString())
            createDateCell(row, 1, dto.workDate, wb)
            row.createCell(2).setCellValue(dto.volume)
            row.createCell(3).setCellValue(dto.usersPerWorkDone.toDouble())
            row.createCell(4).setCellValue(dto.departmentId.toString())
            row.createCell(5).setCellValue(dto.departmentCount.toDouble())
            row.createCell(6).setCellValue(dto.departmentVolume)
            row.createCell(7).setCellValue(dto.avgCubesPerPerson)
        }

        createSheet("FactWorkDoneForSection", data.factWorkDoneForSection, listOf("WorkDoneID", "WorkDate", "Volume", "UsersPerWorkDone", "SectionID", "SectionCount", "SectionVolume")) { dto, row, wb ->
            row.createCell(0).setCellValue(dto.workDoneId.toString())
            createDateCell(row, 1, dto.workDate, wb)
            row.createCell(2).setCellValue(dto.volume)
            row.createCell(3).setCellValue(dto.usersPerWorkDone.toDouble())
            row.createCell(4).setCellValue(dto.sectionId.toString())
            row.createCell(5).setCellValue(dto.sectionCount.toDouble())
            row.createCell(6).setCellValue(dto.sectionVolume)
        }

        // Write the workbook to a ByteArray
        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()
        return outputStream.toByteArray()
    }

    // Helper for formatting Instant to an Excel date/time cell
    private fun createDateCell(row: Row, columnIndex: Int, instant: Instant, workbook: Workbook) {
        val dateValue = Date.from(instant.atZone(ZoneId.systemDefault()).toInstant())
        val dateCell = row.createCell(columnIndex)
        dateCell.setCellValue(dateValue)

        val creationHelper = workbook.creationHelper
        val dateCellStyle = workbook.createCellStyle()
        // Use the desired Excel date format
        dateCellStyle.dataFormat = creationHelper.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss")
        dateCell.cellStyle = dateCellStyle
    }

    // Helper for formatting LocalDate to an Excel date cell
    private fun createDateCell(row: Row, columnIndex: Int, localDate: LocalDate, workbook: Workbook) {
        val dateValue = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val dateCell = row.createCell(columnIndex)
        dateCell.setCellValue(dateValue)

        val creationHelper = workbook.creationHelper
        val dateCellStyle = workbook.createCellStyle()
        // Use the desired Excel date format
        dateCellStyle.dataFormat = creationHelper.createDataFormat().getFormat("yyyy-mm-dd")
        dateCell.cellStyle = dateCellStyle
    }
}