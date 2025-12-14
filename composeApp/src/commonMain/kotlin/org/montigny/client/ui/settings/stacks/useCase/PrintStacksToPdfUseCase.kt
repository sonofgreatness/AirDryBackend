package org.montigny.client.ui.settings.stacks.useCase

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.PageSize
import com.lowagie.text.Paragraph
import com.lowagie.text.Phrase
import com.lowagie.text.Rectangle
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.montigny.client.HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.dto.Work.StackEntityResponse
import org.montigny.client.util.Constants.Companion.PDF_FOOTER_TEXT
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.stateClasses.UiState

import java.awt.Color
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

//Must run
class PrintStacksToPdfUseCase(
    private val stackApi: StackApi,
    private val  stackRepository : StackRepository,
    private  val  organizationApi: OrganizationApi,
    private val  organizationRepository : OrganizationRepository,
    private val offlineSessionManager: OfflineSessionManager
) {

operator fun invoke(isConnectedToServer : Boolean): Flow<UiState> =  flow {
try {
    emit(UiState.Loading)
    val stacks = if (isConnectedToServer)
        stackApi.getAllStacks()
    else
        stackRepository.getAllStacks()
    val stackCodes: List<StackEntityResponse> = stacks.map {
        it.stackCode
    }.distinctBy { it.id }
    val organization =if (isConnectedToServer)
        organizationApi.getOrganizationWithNestedDepartments()
    else
        organizationRepository.selectOrganizationHierarchyByUserId(offlineSessionManager.userId?.toUUID()!!)
    val logo  = organization[0].logo

    generatePdf(stacks, stackCodes, logo, organization[0].name)
    emit(UiState.Success)

    }catch(e:Exception){
    emit(UiState.Error("Failed to prepare stacks for export: ${e.message}"))
    e.printStackTrace() }
}
    private fun generatePdf(
        stacks: List<GetStackDTO>,
        stackCodes: List<StackEntityResponse>,
        logo: ByteArray?,
        organizationName: String
    ) {
        val outputDir = File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/StackReports")
        if (!outputDir.exists()) outputDir.mkdirs()

       val dateStr =generateDayDateForNowSpaced()
        val pdfFile = File(outputDir, "StackReport_${dateStr}.pdf")
        val document = Document(PageSize.A4, 36f, 36f, 54f, 36f)
        val writer = PdfWriter.getInstance(document, FileOutputStream(pdfFile))
        document.open()

        // ---------- HEADER ----------
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

        val headerTable = PdfPTable(2)
        headerTable.widthPercentage = 100f
        headerTable.setWidths(floatArrayOf(2f, 1f))

        val orgCell = PdfPCell(
            Phrase(
                "$organizationName\n\nDate Issued: ${
                    SimpleDateFormat(HUMAN_READABLE_FORMAT_TEXT_HOUR_MINUTE).format(Date())
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

        headerTable.addCell(orgCell)
        headerTable.addCell(logoCell)
        document.add(headerTable)
        document.add(Paragraph("\n"))

        // ---------- HELPER FUNCTIONS ----------
        val headerStyle = Font(Font.HELVETICA, 11f, Font.BOLD)
        val cellStyle = Font(Font.HELVETICA, 10f, Font.NORMAL)
         val backPageCellStyle  = Font(Font.HELVETICA, 10f, Font.NORMAL) //redifine to fit necessary contraints

        // ---------- STACKS TABLE ----------
        writeStacksFrontPage(document, headerStyle, stacks, cellStyle)
        document.newPage()
        writeStacksBackPage(document, headerStyle, stacks, backPageCellStyle)

        document.close()
        writer.close()
        println("Generate pdf completed")
    }

    private fun writeStacksFrontPage(
        document: Document,
        headerStyle: Font,
        stacks: List<GetStackDTO>,
        cellStyle: Font
    ) {
        document.add(Paragraph("Stacks", Font(Font.HELVETICA, 14f, Font.BOLD)))
        document.add(Paragraph("\n"))

        val stackTable = PdfPTable(floatArrayOf(2f, 1f, 1f, 1f, 1f, 1f, 1f, 1f))
        stackTable.widthPercentage = 100f

        listOf(
            "Stack Name", "Description", "Code", "Length",
            "Height", "Thickness", "Pieces", "Volume"
        ).forEach { addHeaderCell(it, stackTable, headerStyle) }

        stacks.forEach { stack ->
            addBodyCell(stack.displayName ?: "-", stackTable, cellStyle)
            addBodyCell(stack.stackCode.displayName ?: "-", stackTable, cellStyle)
            addBodyCell(stack.stackCode.displayCode ?: "-", stackTable, cellStyle)
            addBodyCell(String.format("%.3f", stack.length ?: 0.0), stackTable, cellStyle)
            addBodyCell(String.format("%.3f", stack.height ?: 0.0), stackTable, cellStyle)
            addBodyCell(String.format("%.3f", stack.thickness ?: 0.0), stackTable, cellStyle)
            addBodyCell(stack.numberOfPieces?.toString() ?: "-", stackTable, cellStyle)
            addBodyCell(String.format("%.3f", stack.volume ?: 0.0), stackTable, cellStyle)
        }

        document.add(stackTable)
        // ---------- FOOTER ----------
        document.add(
            Paragraph(
                "\n\n$PDF_FOOTER_TEXT",
                Font(Font.HELVETICA, 9f, Font.ITALIC)
            )
        )
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


}
