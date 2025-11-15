package org.montigny.client.ui.reports.useCase

import com.lowagie.text.*
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.montigny.client.HUMAN_READABLE_FORMAT_TEXT
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.Constants.Companion.MULTIPLICATION_SYMBOL
import org.montigny.client.util.Constants.Companion.PDF_FOOTER_TEXT
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.stateClasses.FileState
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger
import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.chunked
import kotlin.math.log

class GenerateWeeklySummaryPdfUseCase(
    private val workApi: WorkApi,
    private val organizationApi: OrganizationApi,
    private val userApi: UserApi,
    private  val stackApi : StackApi,
    private  val logger : Logger
) {
    private val _progress = MutableStateFlow(0)
    private val _currentPage = MutableStateFlow(1)
    private val _totalPages = MutableStateFlow(Int.MAX_VALUE)
    private val _casualWorkers = MutableStateFlow<List<UserResponse>>(emptyList())

    // For summaries recursion
    private val _summaryCurrentPage = MutableStateFlow(1)
    private val _summaryTotalPages = MutableStateFlow(Int.MAX_VALUE)
    private val _summaries = MutableStateFlow<List<WorkDaySummaryDTO>>(emptyList())

    operator fun invoke(filters: WorkDataFiltersDTO): Flow<FileState> = flow {
        emit(FileState.Loading)
        try {
            val stacks = stackApi.getAllStacks()
            // 1. get organization info
            val organizations = organizationApi.getOrganizationWithNestedDepartments()
            val organization = organizations.firstOrNull()
                ?: throw IllegalStateException("No organization found")
            val logo = organization.logo
            val organizationName = organization.name ?: "Organization"

            // 2. fetch all casual workers recursively
            _currentPage.value = 1
            _totalPages.value = Int.MAX_VALUE
            _casualWorkers.value = emptyList()
            fetchAllCasualWorkers()

            val casualWorkers = _casualWorkers.value

            // 3. group workers by department → section
            val grouped = casualWorkers
                .groupBy { it.department?.displayName ?: "Unassigned Department" }
                .mapValues { entry ->
                    entry.value.groupBy { it.section?.displayName ?: "Unassigned Section" }
                }
            val outputDir =
                File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/WeeklyReports")
            if (!outputDir.exists()) outputDir.mkdirs()
            val dateStr = generateDayDateForNowSpaced()
            val pdfFile = File(outputDir, "WeeklyReports_${dateStr}.pdf")

            withContext(Dispatchers.IO) {
                val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
                val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
                val backPageCellStyle  = Font(Font.HELVETICA, 10f, Font.NORMAL)
                val headerStyle = Font(Font.HELVETICA, 11f, Font.BOLD)

                document.open()

                writeLetterHead(logo, organizationName, document, filters.filterDate?:"",
                    filters.filterDateEndDate?:"")

                // Iterate departments
                for ((deptName, sections) in grouped) {
                    addTitle(document, deptName, 16f)
                    document.add(Paragraph("\n"))

                    for ((sectionName, usersInSection) in sections) {
                         if (usersInSection.isNotEmpty()) {
                             addSubTitle(document, "$deptName › $sectionName", 14f)
                             addSubTitle(document, "Weekly Summaries", 12f)
                             document.add(Paragraph("\n"))

                             val sectionTotals = computeSectionTotals(usersInSection, filters)

                             val table = PdfPTable(5)
                             table.widthPercentage = 100f

                             table.setWidths(floatArrayOf(1.5f, 2f, 2.2f, 4.2f, 1.8f))
                             addHeaderCell(table, "Full name")
                             addHeaderCell(table, "Date")
                             addHeaderCell(table, "Stacks")
                             addHeaderCell(table, "Stack %")
                             addHeaderCell(table, "Total cubes")

                        for (user in usersInSection) {
                            val userSummaries = fetchAllSummariesForUser(filters.copy(filterUserId = user.id)).sortedBy { it.dayLabel }
                            var sum = 0.0
                            userSummaries.forEach {dailySummary->
                                val stacksArray = getStacksArray(dailySummary)
                                val stacksPercentagesArray = getStacksPercentagesArray(dailySummary)
                                val totalPersonalCubes = getTotalPersonalCubes(dailySummary, user.id!!)

                                val startDate = userSummaries.minOfOrNull { dailySummary.dayLabel } ?: ""

                                addCell(table, "${user.name} ${user.surname}")
                                addCell(table, startDate)
                                addCell(table, stacksArray.toString())
                                addCell(table, stacksPercentagesArray.toString())
                                addCell(table, String.format("%.2f",totalPersonalCubes))
                                sum += totalPersonalCubes
                            }
                            val totalFont = Font(Font.HELVETICA, 10f, Font.BOLD)
                            val totalLabelCell = PdfPCell(Phrase("Total", totalFont)).apply {
                                colspan = 4
                                horizontalAlignment = Element.ALIGN_RIGHT
                            }
                            val totalValueCell = PdfPCell(Phrase(String.format("%.2f", sum), totalFont)).apply {
                                horizontalAlignment = Element.ALIGN_LEFT
                            }

                            if (userSummaries.isNotEmpty()) {
                                table.addCell(totalLabelCell)
                                table.addCell(totalValueCell)
                                document.add(table)
                                document.newPage()

                                table.deleteBodyRows()
                                addHeaderCell(table, "Full name")
                                addHeaderCell(table, "Date")
                                addHeaderCell(table, "Stacks")
                                addHeaderCell(table, "Stack %")
                                addHeaderCell(table, "Total cubes")
                                document.newPage()
                                writeStacksBackPage(document, headerStyle, stacks, backPageCellStyle)
                                document.newPage()
                            }


                        }
                         }
                        document.newPage()

                    }
                    document.newPage()
                }
                document.close()
                writer.close()
            }
            emit(FileState.Success(filePath = pdfFile.absolutePath))
        } catch (t: Throwable) {
            emit(FileState.Error(t.localizedMessage ?: "Unknown error"))
        }
    }


    private   fun  getStacksArray(dailySummary : WorkDaySummaryDTO) :List<String> {
        val allStacks = dailySummary.workList.flatMap { it.stacks }
        val groupedStacks = allStacks.groupingBy { it.stackCode }.eachCount()
        return groupedStacks.map { (code, count) ->
            "$code $MULTIPLICATION_SYMBOL $count"
        }
    }

  private fun   getStacksPercentagesArray(dailySummary:WorkDaySummaryDTO): List<String>{
      val result = mutableListOf<String>()
      dailySummary.workList.forEach { work ->
          val totalUsers = work.totalUsers.takeIf { it > 0 } ?: return@forEach
          val distinctStacks = work.stacks.distinctBy { it.stackCode }
          val percentage = (1.0 / totalUsers.toDouble()) * 100
          distinctStacks.forEach { stack ->
              val entry = "${stack.stackCode} (${String.format("%.1f", percentage)}%)"
              if (entry !in result) result.add(entry) // ensure unique across all works
          }
      }
      return result
  }


  private fun getTotalPersonalCubes(dailySummary: WorkDaySummaryDTO, userId: UUID): Double {
        return dailySummary.workList.sumOf { work ->
            work.users
                .filter { it.id == userId }
                .sumOf { it.personalCubes }
        }
    }
    // ----- Recursive pagination helpers -----

    private suspend fun fetchAllCasualWorkers() {
        val page = _currentPage.value
        if (page > _totalPages.value) return
        val res = userApi.getCasualUsers(page, DEFAULT_PAGE_SIZE)
        res?.let {
            if (page <= 1) {
                _casualWorkers.value = it.items
                _totalPages.value = it.totalPages
            } else {
                _casualWorkers.value += it.items
            }
        }
        _currentPage.value++
        fetchAllCasualWorkers()
    }

    private suspend fun fetchAllSummariesForUser(filters: WorkDataFiltersDTO): List<WorkDaySummaryDTO> {
        _summaryCurrentPage.value = 1
        _summaryTotalPages.value = Int.MAX_VALUE
        _summaries.value = emptyList()
        populateSummariesList(filters)
        return _summaries.value
    }

    private suspend fun populateSummariesList(filters: WorkDataFiltersDTO) {
        val page = _summaryCurrentPage.value
        if (page > _summaryTotalPages.value) return

        val res = workApi.getDailyWorkSummariesByUser(page, DEFAULT_PAGE_SIZE, filters)
        res?.let {
            if (page <= 1) {
                _summaries.value = it.items
                _summaryTotalPages.value = it.totalPages
            } else {
                _summaries.value += it.items
            }
        }
        _summaryCurrentPage.value++
        populateSummariesList(filters)
    }

    // ----- Aggregation -----

    private data class SectionTotals(val totalStacks: Int, val totalCubes: Double)

    private suspend fun computeSectionTotals(
        users: List<UserResponse>,
        baseFilters: WorkDataFiltersDTO
    ): SectionTotals {
        var stacksSum = 0
        var cubesSum = 0.0
        for (user in users) {
            val summaries = fetchAllSummariesForUser(baseFilters.copy(filterUserId = user.id))
            stacksSum += summaries.sumOf { it.totalStacks }
            cubesSum += summaries.sumOf { it.totalVolume }
        }
        return SectionTotals(stacksSum, cubesSum)
    }

    // ----- PDF Helpers -----

    private fun writeLetterHead(logo: ByteArray?, organizationName: String,
                                document: Document,
                                startDate:String,
                                endDate:String) {
        val logoImage: Image = if (logo != null) {
            Image.getInstance(logo)
        } else {
            val resource = Thread.currentThread().contextClassLoader
                .getResource("drawable/main_logo.png")
                ?: throw IllegalArgumentException("Default logo not found")
            Image.getInstance(resource)
        }
        logoImage.scaleToFit(80f, 80f)
        logoImage.alignment = Element.ALIGN_RIGHT

        // --- use 2 columns still, but merge periodCell across both
        val headerTable = PdfPTable(2)
        headerTable.widthPercentage = 100f
        headerTable.setWidths(floatArrayOf(2f, 1f))

        val orgCell = PdfPCell(
            Phrase(
                "$organizationName\n\nDate Issued: ${
                    SimpleDateFormat(HUMAN_READABLE_FORMAT_TEXT).format(Date())
                }",
                Font(Font.HELVETICA, 12f, Font.BOLD)
            )
        ).apply {
            border = Rectangle.NO_BORDER
            verticalAlignment = Element.ALIGN_MIDDLE
        }

        val logoCell = PdfPCell(logoImage, false).apply {
            border = Rectangle.NO_BORDER
            horizontalAlignment = Element.ALIGN_RIGHT
        }

        // ✅ Period row spans both columns
        val periodCell = PdfPCell(
            Phrase(
                "Period:  from ${formatToLongDate(startDate)} to ${formatToLongDate(endDate)}",
                Font(Font.HELVETICA, 11f, Font.BOLD)
            )
        ).apply {
            border = Rectangle.NO_BORDER
            horizontalAlignment = Element.ALIGN_CENTER
            verticalAlignment = Element.ALIGN_MIDDLE
            colspan = 2
            paddingTop = 8f
        }

        // add top row
        headerTable.addCell(orgCell)
        headerTable.addCell(logoCell)
        // add second row (merged)
        headerTable.addCell(periodCell)

        document.add(headerTable)
        document.add(Paragraph("\n"))
    }

    private fun addTitle(document: Document, text: String, size: Float) {
        val font = Font(Font.HELVETICA, size, Font.BOLD)
        val p = Paragraph(text, font)
        p.alignment = Element.ALIGN_LEFT
        document.add(p)
    }

    private fun addSubTitle(document: Document, text: String, size: Float) {
        val font = Font(Font.HELVETICA, size, Font.NORMAL)
        val p = Paragraph(text, font)
        p.alignment = Element.ALIGN_LEFT
        document.add(p)
    }
    private fun addHeaderCell(table: PdfPTable, text: String) {
        val font = Font(Font.HELVETICA, 10f, Font.BOLD)
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        table.addCell(cell)
    }
    private fun addCell(table: PdfPTable, text: String) {
        val font = Font(Font.HELVETICA, 10f, Font.NORMAL)
        val cell = PdfPCell(Phrase(text, font))
        cell.horizontalAlignment = Element.ALIGN_LEFT
        cell.verticalAlignment = Element.ALIGN_MIDDLE
        table.addCell(cell)
    }
    private fun writeStacksBackPage(
        document: Document,
        headerStyle: Font,
        stacks: List<GetStackDTO>,
        cellStyle: Font
    ) {

        // --- Switch this PAGE ONLY to LANDSCAPE ---
        document.pageSize = PageSize.A4.rotate()
        document.setMargins(20f, 20f, 30f, 20f)
        document.add(Paragraph("Back Page — All Stacks Summary", Font(Font.HELVETICA, 13f, Font.BOLD)))
        document.add(Paragraph("\n"))

        // ---- SETTINGS TO CONDENSE CONTENT ----
        val condensedHeader = Font(Font.HELVETICA, 8f, Font.BOLD)
        val condensedCell = Font(Font.HELVETICA, 7f, Font.NORMAL)
        val rowPadding = 2f

        // ---- COLUMNS WE WILL SHOW ----
        val columnTitles = listOf(
            "Name", "Code", "Description", "L (m)", "H (m)", "T (m)", "Pieces", "Volume"
        )

        // ---- MAX HEIGHT CONSIDERATIONS ----
        val maxRowsPerTable = 35   // fits safely on landscape A4

        val chunks: List<List<GetStackDTO>> = stacks.chunked(maxRowsPerTable)

        chunks.forEachIndexed { index, chunk ->
            val table = PdfPTable(floatArrayOf(2.2f, 1.2f, 2f, 1f, 1f, 1f, 1f, 1f))
            table.widthPercentage = 100f
            table.setSpacingAfter(5f)

            // ----- ADD HEADERS -----
            columnTitles.forEach { title ->
                val cell = PdfPCell(Phrase(title, condensedHeader)).apply {
                    backgroundColor = Color(240, 240, 240)
                    horizontalAlignment = Element.ALIGN_CENTER
                    paddingTop = rowPadding
                    paddingLeft = rowPadding
                    paddingRight = rowPadding
                    paddingBottom = rowPadding

                }
                table.addCell(cell)
            }

            // ----- BODY -----
            for (s in chunk) {
                table.addCell(makeCondensedCell(s.displayName ?: "-", condensedCell, rowPadding))
                table.addCell(makeCondensedCell(s.stackCode.displayCode ?: "-", condensedCell, rowPadding))
                table.addCell(makeCondensedCell(s.stackCode.displayName ?: "-", condensedCell, rowPadding))
                table.addCell(makeCondensedCell(fmt(s.length), condensedCell, rowPadding))
                table.addCell(makeCondensedCell(fmt(s.height), condensedCell, rowPadding))
                table.addCell(makeCondensedCell(fmt(s.thickness), condensedCell, rowPadding))
                table.addCell(makeCondensedCell(s.numberOfPieces?.toString() ?: "-", condensedCell, rowPadding))
                table.addCell(makeCondensedCell(fmt(s.volume), condensedCell, rowPadding))
            }

            // Add table
            document.add(table)

            // Add tiny divider if it's not the last one
            if (index != chunks.lastIndex) {
                document.add(Paragraph("\n"))
            }
        }

        // ---- FOOTER (Condensed) ----
        document.add(
            Paragraph(
                "\n$PDF_FOOTER_TEXT",
                Font(Font.HELVETICA, 7f, Font.ITALIC)
            )
        )

        // ---- RETURN TO PORTRAIT FOR NEXT PAGE ----
        document.pageSize = PageSize.A4
        document.setMargins(36f, 36f, 54f, 36f)
    }

    private  fun addHeaderCell(text: String, table: PdfPTable, headerStyle: Font) {
        val cell = PdfPCell(Phrase(text, headerStyle))
        cell.backgroundColor = Color(230, 230, 230)
        cell.horizontalAlignment = Element.ALIGN_CENTER
        cell.setPadding(5f)
        table.addCell(cell)
    }

    private fun addBodyCell(text: String, table: PdfPTable,cellStyle: Font) {
        val cell = PdfPCell(Phrase(text, cellStyle))
        cell.setPadding(5f)
        cell.isNoWrap = false
        cell.horizontalAlignment = Element.ALIGN_LEFT
        table.addCell(cell)
    }


    private fun makeCondensedCell(text: String, font: Font, myPadding: Float): PdfPCell {
        return PdfPCell(Phrase(text, font)).apply {
            horizontalAlignment = Element.ALIGN_LEFT
            isNoWrap = false
            paddingTop = myPadding
            paddingLeft = myPadding
            paddingRight =myPadding
            paddingBottom= myPadding
        }
    }

    private fun fmt(value: Double?): String =
        String.format("%.3f", value ?: 0.0)
    private fun fmt(value: Float?): String =
        String.format("%.3f", value ?: 0.0)

    private fun formatToLongDate(dateStr: String): String {
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat(HUMAN_READABLE_FORMAT_TEXT, Locale.getDefault())
            val date = input.parse(dateStr)
            output.format(date!!)
        } catch (e: Exception) {
            "Unspecified date"
        }
    }
}
