package org.montigny.client.ui.reports.useCase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.montigny.client.data.api.repository.ReportApi
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.stateClasses.FileState
import java.io.File

class GenerateDashBoardReportUseCase
    (val reportApi: ReportApi){
    operator  fun  invoke(startDate:String , endDate:String): Flow<FileState> = flow{
        emit(FileState.Loading)
        try {
            val outputDir =
                File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/Reports")
            if (!outputDir.exists()) outputDir.mkdirs()
            val dateStr = generateDayDateForNowSpaced()
            val excelFile = File(outputDir, "MasterDashBoardData_${dateStr}.xlsx")
            val fileData: ByteArray? = reportApi.getDashBoardExcelReport(startDate, endDate)
            fileData?.let {
                excelFile.writeBytes(it)
            }
            emit(FileState.Success(filePath = outputDir.absolutePath))
        }catch (ex: Exception){
            ex.printStackTrace()
            emit(FileState.Error(message = ex.message.toString()))
        }

    }
}