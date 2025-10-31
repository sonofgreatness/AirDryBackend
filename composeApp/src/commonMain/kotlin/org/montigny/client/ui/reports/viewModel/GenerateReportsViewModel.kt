package org.montigny.client.ui.reports.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.ui.reports.useCase.GenerateDashBoardReportUseCase
import org.montigny.client.ui.reports.useCase.GeneratePayRollDataReportUseCase
import org.montigny.client.ui.reports.useCase.GenerateWeeklySummaryPdfUseCase
import org.montigny.client.util.ConnectivityObserver
import  org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.FileState
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger

class GenerateReportsViewModel(
    private val sessionManager: SessionManager,
    private  val connectivityObserver: ConnectivityObserver,
    private  val logger : Logger,
    private val   generateWeeklySummaryPdfUseCase: GenerateWeeklySummaryPdfUseCase,
    private val  generateDashBoardReportUseCase: GenerateDashBoardReportUseCase,
    private val generatePayRollDataReportUseCase: GeneratePayRollDataReportUseCase,


):ViewModel() {


    private val _pdfGeneratorMessage  = MutableStateFlow<String?>(null)
    val  pdfGeneratorMessage: StateFlow<String?> = _pdfGeneratorMessage

    private val _payRollGeneratorMessage  = MutableStateFlow<String?>(null)
    val  payRollGeneratorMessage: StateFlow<String?> = _payRollGeneratorMessage

    private val _dashBoardGeneratorMessage  = MutableStateFlow<String?>(null)
    val  dashBoardGeneratorMessage: StateFlow<String?> = _dashBoardGeneratorMessage


    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow
    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto

    fun  exportStacksDataToPdf(){
        viewModelScope.launch {
            generateWeeklySummaryPdfUseCase.invoke(filterDto.value).collect {
                when (it) {
                    is UiState.Error -> {
                        _pdfGeneratorMessage.value = "Failed to generate file"
                        logger.error("failed to export ${it.message}")
                    }
                    is UiState.Success -> {
                        _pdfGeneratorMessage.value = "Done."
                    }
                    is UiState.Loading -> {
                        _pdfGeneratorMessage.value = "Loading..."
                    }
                    else -> {}
                }
            }
        }
    }


    fun  exportPayRollDataToExcel(startdate:String, enddate:String) {
        println("viewModel print pay roll data")
        viewModelScope.launch {
            generatePayRollDataReportUseCase.invoke(startdate, enddate).collect {
                when (it) {
                    is FileState.Success -> {
                        _payRollGeneratorMessage.value = it.filePath
                    }
                    is FileState.Error -> {
                        _payRollGeneratorMessage.value = it.message
                    }
                     else -> {}
                }
            }
        }
    }
     fun exportDashBoardToExcel(startdate:String, enddate:String) {
         println("viewModel print DashBoardToExcel")
         viewModelScope.launch {
             generateDashBoardReportUseCase.invoke(startdate, enddate).collect {
                 when (it) {
                     is FileState.Success -> {
                         _dashBoardGeneratorMessage.value = it.filePath
                     }
                     is FileState.Error -> {
                         _dashBoardGeneratorMessage.value = it.message
                     }
                     is FileState.Loading -> {}
                 }
             }
         }
     }

}