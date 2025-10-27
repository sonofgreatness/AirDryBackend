package org.montigny.client.ui.reports.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.ui.reports.useCase.GenerateWeeklySummaryPdfUseCase
import org.montigny.client.util.ConnectivityObserver
import  org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger

class GenerateReportsViewModel(
    private val sessionManager: SessionManager,
    private  val connectivityObserver: ConnectivityObserver,
    private  val logger : Logger,
    private val   generateWeeklySummaryPdfUseCase: GenerateWeeklySummaryPdfUseCase
):ViewModel() {


    private val _pdfGeneratorMessage  = MutableStateFlow<String?>(null)
    val  pdfGeneratorMessage: StateFlow<String?> = _pdfGeneratorMessage
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
}