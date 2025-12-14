package org.montigny.client.ui.reports.viewModel
import kotlinx.coroutines.delay
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


    private val _pdfGeneratorMessage  = MutableStateFlow<String>("")
    val  pdfGeneratorMessage: StateFlow<String?> = _pdfGeneratorMessage

    private val _payRollGeneratorMessage  = MutableStateFlow<String>("")
    val  payRollGeneratorMessage: StateFlow<String?> = _payRollGeneratorMessage

    private val _dashBoardGeneratorMessage  = MutableStateFlow<String>("")
    val  dashBoardGeneratorMessage: StateFlow<String?> = _dashBoardGeneratorMessage


    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow
    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto

    private val _isSyncingPdf = MutableStateFlow<Boolean>(false)
    val isSyncingPdf: StateFlow<Boolean> = _isSyncingPdf
    private  val  _isSyncingPayRoll = MutableStateFlow(false)
    val isSyncingPayRoll: StateFlow<Boolean> = _isSyncingPayRoll

    private  val _isSyncingMasterData  = MutableStateFlow(false)
    val isSyncingMasterData: StateFlow<Boolean> = _isSyncingMasterData

  fun   setStartAndEndDateFilters(start: String, end:String) {
      println("setting start date and end date filters with  values $start and $end")
        _filterDto.value  = _filterDto.value.copy(filterDate = start, filterDateEndDate = end)
    }
    fun  exportStacksDataToPdf(){
        viewModelScope.launch {
            _isSyncingPdf.value = true
            generateWeeklySummaryPdfUseCase.invoke(filterDto.value).collect {
                when (it) {
                    is FileState.Error -> {
                         _pdfGeneratorMessage.value = "Failed to generate file, make sure file is not open by other apps"
                         logger.error("failed to export ${it.message}")
                         delay(2000)
                        _isSyncingPdf.value = false
                    }
                    is FileState.Success -> {
                        _pdfGeneratorMessage.value = it.filePath
                         delay(2000)
                        _isSyncingPdf.value = false
                    }
                    else -> {}
                }
            }
        }
    }
    fun  exportPayRollDataToExcel(startdate:String, enddate:String) {
        viewModelScope.launch {
            _isSyncingPayRoll.value = true
            generatePayRollDataReportUseCase.invoke(startdate, enddate).collect {
                when (it) {
                    is FileState.Success -> {
                        _payRollGeneratorMessage.value = it.filePath
                        delay(2000)
                        _isSyncingPayRoll.value = false
                    }
                    is FileState.Error -> {
                        _payRollGeneratorMessage.value = it.message
                        delay(2000)
                        _isSyncingPayRoll.value = false
                    }
                     else -> {}
                }
            }
        }
    }
     fun exportDashBoardToExcel(startdate:String, enddate:String) {
         viewModelScope.launch {
             _isSyncingMasterData.value = true
             generateDashBoardReportUseCase.invoke(startdate, enddate).collect {
                 when (it) {
                     is FileState.Success -> {
                         _dashBoardGeneratorMessage.value = it.filePath
                         delay(2000)
                         _isSyncingMasterData.value=false
                     }
                     is FileState.Error -> {
                         _dashBoardGeneratorMessage.value = it.message
                         delay(2000)
                         _isSyncingMasterData.value=false
                     }
                     is FileState.Loading -> {}
                 }
             }
         }
     }
    fun resetReportMessages() {
        _pdfGeneratorMessage.value = ""
        _payRollGeneratorMessage.value = ""
        _dashBoardGeneratorMessage.value = ""
    }
}