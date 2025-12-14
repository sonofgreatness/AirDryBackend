package org.montigny.client.ui.settings.stacks.useCase

import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.StackRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.dto.Work.StackEntityResponse
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.stateClasses.UiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PrintStacksToExcelUseCase(
    private val stackApi: StackApi,
    private val stackRepository: StackRepository,
    private val organizationApi: OrganizationApi,
    private val organizationRepository: OrganizationRepository,
    private val offlineSessionManager: OfflineSessionManager
) {

    operator fun invoke(isConnectedToServer: Boolean): Flow<UiState> = flow {
        try {
            emit(UiState.Loading)

            val stacks = if (isConnectedToServer)
                stackApi.getAllStacks()
            else
                stackRepository.getAllStacks()

            val stackCodes: List<StackEntityResponse> = stacks.map { it.stackCode }
                .distinctBy { it.id }

            val organization = if (isConnectedToServer)
                organizationApi.getOrganizationWithNestedDepartments()
            else
                organizationRepository.selectOrganizationHierarchyByUserId(
                    offlineSessionManager.userId?.toUUID()!!
                )

            val orgName = organization.firstOrNull()?.name ?: "Organization"

            generateExcel(stacks, stackCodes, orgName)
            emit(UiState.Success)

        } catch (e: Exception) {
            e.printStackTrace()
            emit(UiState.Error("Failed to export stacks to Excel: ${e.message}"))
        }
    }

    private fun generateExcel(
        stacks: List<GetStackDTO>,
        stackCodes: List<StackEntityResponse>,
        organizationName: String
    ) {
        val workbook = XSSFWorkbook()

        // ---------- STYLES ----------
        val myheaderFont = workbook.createFont().apply {
            bold = true
            color = IndexedColors.WHITE.getIndex()
        }

        val myheaderStyle = workbook.createCellStyle().apply {
            setFont(myheaderFont)
            fillForegroundColor = IndexedColors.DARK_BLUE.getIndex()
            fillPattern = FillPatternType.SOLID_FOREGROUND
            alignment = HorizontalAlignment.CENTER
            verticalAlignment = VerticalAlignment.CENTER
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        val mycellStyle = workbook.createCellStyle().apply {
            wrapText = true
            verticalAlignment = VerticalAlignment.TOP
            borderBottom = BorderStyle.THIN
            borderTop = BorderStyle.THIN
            borderLeft = BorderStyle.THIN
            borderRight = BorderStyle.THIN
        }

        // ---------- SHEET 1: STACK CODES ----------
        val sheetCodes = workbook.createSheet("Stack Codes")
        var rowIndex = 0

        val titleRow = sheetCodes.createRow(rowIndex++)
        val titleCell = titleRow.createCell(0)
        titleCell.setCellValue("$organizationName - Stack Codes Report (${SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())})")
        sheetCodes.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 1))

        rowIndex++ // blank row

        val headerRowCodes = sheetCodes.createRow(rowIndex++)
        val headersCodes = listOf("Code", "Display Name")
        headersCodes.forEachIndexed { i, h ->
            val cell = headerRowCodes.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = myheaderStyle
        }

        stackCodes.forEach { code ->
            val row = sheetCodes.createRow(rowIndex++)
            row.createCell(0).apply {
                setCellValue(code.displayCode ?: "-")
                cellStyle = cellStyle
            }
            row.createCell(1).apply {
                setCellValue(code.displayName ?: "-")
                cellStyle = cellStyle
            }
        }

        sheetCodes.autoSizeColumn(0)
        sheetCodes.autoSizeColumn(1)

        // ---------- SHEET 2: STACKS ----------
        val sheetStacks = workbook.createSheet("Stacks")
        rowIndex = 0

        val titleRow2 = sheetStacks.createRow(rowIndex++)
        val titleCell2 = titleRow2.createCell(0)
        val dateStr =generateDayDateForNowSpaced()
        titleCell2.setCellValue("$organizationName - Stacks Report (${SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())})")
        sheetStacks.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 7))

        rowIndex++

        val headerRowStacks = sheetStacks.createRow(rowIndex++)
        val headersStacks = listOf(
            "Stack Name", "Description", "Code", "Length",
            "Height", "Thickness", "Pieces", "Volume"
        )
        headersStacks.forEachIndexed { i, h ->
            val cell = headerRowStacks.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = myheaderStyle
        }

        stacks.forEach { stack ->
            val row = sheetStacks.createRow(rowIndex++)
            row.createCell(0).apply {
                setCellValue(stack.displayName ?: "-")
                cellStyle = cellStyle
            }
            row.createCell(1).apply {
                setCellValue(stack.stackCode.displayName ?: "-")
                cellStyle = cellStyle
            }
            row.createCell(2).apply {
                setCellValue(stack.stackCode.displayCode ?: "-")
                cellStyle = cellStyle
            }
            row.createCell(3).apply {
                setCellValue(stack.length.toDouble() ?: 0.0)
                cellStyle = cellStyle
            }
            row.createCell(4).apply {
                setCellValue(stack.height.toDouble() ?: 0.0)
                cellStyle = cellStyle
            }
            row.createCell(5).apply {
                setCellValue(stack.thickness.toDouble() ?: 0.0)
                cellStyle = cellStyle
            }
            row.createCell(6).apply {
                setCellValue(stack.numberOfPieces?.toDouble() ?: 0.0)
                cellStyle = cellStyle
            }
            row.createCell(7).apply {
                setCellValue(stack.volume ?: 0.0)
                cellStyle = cellStyle
            }
        }
        for (i in headersStacks.indices) sheetStacks.autoSizeColumn(i)

        // ---------- SAVE FILE ----------
        val outputDir = File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/StackReports")
        if (!outputDir.exists()) outputDir.mkdirs()

        val excelFile = File(outputDir, "StackReport_${dateStr}.xlsx")
        FileOutputStream(excelFile).use { workbook.write(it) }

        workbook.close()
        println("✅ Excel export completed: ${excelFile.absolutePath}")
    }
}