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

        headerTable.addCell(orgCell)
        headerTable.addCell(logoCell)
        document.add(headerTable)
        document.add(Paragraph("\n"))

        // ---------- HELPER FUNCTIONS ----------
        val headerStyle = Font(Font.HELVETICA, 11f, Font.BOLD)
        val cellStyle = Font(Font.HELVETICA, 10f, Font.NORMAL)



        // ---------- STACK CODES TABLE ----------
        document.add(Paragraph("Stack Codes", Font(Font.HELVETICA, 14f, Font.BOLD)))
        document.add(Paragraph("\n"))

        val codeTable = PdfPTable(floatArrayOf(1f, 2f))
        codeTable.widthPercentage = 100f

        addHeaderCell("Code", codeTable,headerStyle)
        addHeaderCell("Display Name", codeTable,headerStyle)

        stackCodes.forEach { code ->
            addBodyCell(code.displayCode ?: "-", codeTable, cellStyle)
            addBodyCell(code.displayName ?: "-", codeTable, cellStyle)
        }

        document.add(codeTable)

        // ---------- PAGE BREAK ----------
        document.newPage()

        // ---------- STACKS TABLE ----------
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
        document.close()
        writer.close()
        println("Generate pdf completed")
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
}
