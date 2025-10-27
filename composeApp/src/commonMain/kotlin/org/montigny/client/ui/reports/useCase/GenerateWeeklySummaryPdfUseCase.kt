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
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.stateClasses.UiState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/*********************************
 * Generates weekly summary workDone
 * week must be =< latest WeeklyApproval
 * 1. get active casual workers
 *    -> for each worker
 *        -> get list of work for that period
 *           -> process write into back file
 * Forced PAGE BREAK for user
 *********************************/

class GenerateWeeklySummaryPdfUseCase(
    private val workApi: WorkApi,
    private val organizationApi: OrganizationApi,
    private val userApi: UserApi
) {
    private val _progress = MutableStateFlow(0)
    private val _currentPage = MutableStateFlow(1)
    private val _totalPages = MutableStateFlow(Int.MAX_VALUE)
    private val _casualWorkers = MutableStateFlow<List<UserResponse>>(emptyList())

    // For summaries recursion
    private val _summaryCurrentPage = MutableStateFlow(1)
    private val _summaryTotalPages = MutableStateFlow(Int.MAX_VALUE)
    private val _summaries = MutableStateFlow<List<WorkDaySummaryDTO>>(emptyList())

    operator fun invoke(filters: WorkDataFiltersDTO): Flow<UiState> = flow {
        emit(UiState.Idle)
        try {
            emit(UiState.Loading)

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

            // 4. prepare output file
            val outputDir =
                File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/WeeklyReports")
            if (!outputDir.exists()) outputDir.mkdirs()
            val dateStr = generateDayDateForNowSpaced()
            val pdfFile = File(outputDir, "WeeklyReports_${dateStr}.pdf")

            withContext(Dispatchers.IO) {
                val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
                val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
                document.open()

                writeLetterHead(logo, organizationName, document, filters.filterDate?:"",
                    filters.filterDateEndDate?:"")

                // Iterate departments
                for ((deptName, sections) in grouped) {
                    addTitle(document, deptName, 16f)
                    document.add(Paragraph("\n"))

                    for ((sectionName, usersInSection) in sections) {
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
                            val userSummaries = fetchAllSummariesForUser(filters.copy(filterUserId = user.id))
                            //val userTotalStacks = userSummaries.sumOf { it.totalStacks.toLong() }.toInt()
                            //val userTotalCubes = userSummaries.sumOf { it.totalVolume }

                            userSummaries.forEach {dailySummary->
                                val stacksArray = getStacksArray(dailySummary)
                                val stacksPercentagesArray = getStacksPercentagesArray(dailySummary)
                                val totalPersonalCubes = getTotalPersonalCubes(dailySummary, user.id!!)

                                val startDate = userSummaries.minOfOrNull { it.dayLabel } ?: ""
                                /*val stackPercent = if (sectionTotals.totalStacks > 0) {
                                (userTotalStacks.toDouble() / sectionTotals.totalStacks.toDouble()) * 100.0
                            } else 0.0*/

                                addCell(table, "${user.name} ${user.surname}")
                                addCell(table, startDate)
                                addCell(table, stacksArray.toString())
                                addCell(table, stacksPercentagesArray.toString())
                                addCell(table, String.format("%.2f",totalPersonalCubes))
                            }
                            document.add(table)
                            document.newPage()

                            table.deleteBodyRows()
                            addHeaderCell(table, "Full name")
                            addHeaderCell(table, "Date")
                            addHeaderCell(table, "Stacks")
                            addHeaderCell(table, "Stack %")
                            addHeaderCell(table, "Total cubes")

                        }
                        document.newPage()
                    }
                    document.newPage()
                }
                document.close()
                writer.close()
            }

            emit(UiState.Success)
        } catch (t: Throwable) {
            emit(UiState.Error(t.localizedMessage ?: "Unknown error"))
        }
    }


    private   fun  getStacksArray(dailySummary : WorkDaySummaryDTO) :List<String> {
        // Flatten all stacks from all work summaries
        val allStacks = dailySummary.workList.flatMap { it.stacks }
        // Group by stackCode and count how many times each appears
        val groupedStacks = allStacks.groupingBy { it.stackCode }.eachCount()
        // Build list of "stackCode *count"
        return groupedStacks.map { (code, count) ->
            "$code *$count"
        }
    }

  private fun   getStacksPercentagesArray(dailySummary:WorkDaySummaryDTO): List<String>{
      val result = mutableListOf<String>()

      dailySummary.workList.forEach { work ->
          val totalUsers = work.totalUsers.takeIf { it > 0 } ?: return@forEach

          // Get distinct stacks for this work
          val distinctStacks = work.stacks.distinctBy { it.stackCode }

          // Compute percentage per stack
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
                    SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
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
                "Period: $startDate to $endDate",
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
}
