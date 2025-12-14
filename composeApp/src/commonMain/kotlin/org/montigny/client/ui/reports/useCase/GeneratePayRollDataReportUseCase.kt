package org.montigny.client.ui.reports.useCase

import io.ktor.util.logging.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.montigny.client.data.api.repository.ReportApi
import org.montigny.client.util.Constants.Companion.SYSTEM_REPORTS_MAIN_PATH
import org.montigny.client.util.UtilityFunctions.Companion.generateDayDateForNowSpaced
import org.montigny.client.util.stateClasses.FileState
import java.io.File

class GeneratePayRollDataReportUseCase(
    private val reportApi: ReportApi,
    private val logger: Logger
) {
    operator  fun  invoke(startDate:String , endDate:String): Flow<FileState> = flow{
        emit(FileState.Loading)
        try {

            val outputDir =
                File(System.getProperty("user.home"), "$SYSTEM_REPORTS_MAIN_PATH/Reports")
            if (!outputDir.exists()) outputDir.mkdirs()
            val dateStr = generateDayDateForNowSpaced()
            val excelFile = File(outputDir, "MasterPayRollData_${dateStr}.xlsx")
            logger.info("Generating pay roll data start :$startDate end : $endDate")
            val fileData: ByteArray? = reportApi.getPayRollDataReport(startDate, endDate)
            logger.info("Generating pay roll ${fileData?.size} pay roll data")
            fileData?.let {
                excelFile.setWritable(true, false)
                excelFile.writeBytes(it)
            }
            emit(FileState.Success(filePath = excelFile.absolutePath))
        }catch (ex: Exception){
            ex.printStackTrace()
            emit(FileState.Error(message = ex.message.toString()))
        }

    }
}