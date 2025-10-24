package org.montigny.client.ui.reports.viewModel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.UpdateWeeklyApprovalDTO
import org.montigny.client.dto.WeeklyApprovalDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import java.util.UUID


class WeekApprovalsViewModel(
    private val workApi: WorkApi,
    private val connectivityObserver: ConnectivityObserver,
    private val sessionManager: SessionManager
):ViewModel(){

    // Renaming to follow the private/public convention
    private  val  _weeklyApprovalsList = MutableStateFlow<List<WeeklyApprovalDTO>>(emptyList())
    val  weeklyApprovalsList : StateFlow<List<WeeklyApprovalDTO>> get() = _weeklyApprovalsList

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow()

    private val _currentPage = MutableStateFlow(DEFAULT_PAGE)
    val currentPage: StateFlow<Int> = _currentPage

    // State to track which item is currently being updated (for loading indicator)
    private val _isUpdatingId = MutableStateFlow<UUID?>(null)
    val isUpdatingId: StateFlow<UUID?> = _isUpdatingId

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow

    // --- Data Loading and Pagination ---

    fun loadNextPage(pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value++
            populateWeeklyApprovals(pageSize)
        }
    }

    fun populateWeeklyApprovals(pageSize: Int  = DEFAULT_PAGE_SIZE) {
        val page = _currentPage.value
        if (page > _totalPages.value) {
            return
        }
        viewModelScope.launch {
            if (!isNetworkAvailable.value) {
                _snackbarEvent.send("Server unreachable! Please check your connection.")
                return@launch
            }
            if (!isOnlineLoggedIn.value) {
                _snackbarEvent.send("Not logged in! Please re-login.")
                return@launch
            }
            try {
                val resp = workApi.getWeeklyApprovals(page, pageSize)
                resp?.let{
                    _totalPages.value = it.totalPages
                    if (page == DEFAULT_PAGE) {
                        // For the first page, replace the list
                        _weeklyApprovalsList.value = it.items
                    } else {
                        // For subsequent pages, append the items
                        _weeklyApprovalsList.value += it.items
                    }
                }
            } catch (e: Exception) {
                _snackbarEvent.send("Failed to load approvals: ${e.message ?: "Unknown error"}")
            }
        }
    }

    // --- Approval/Revoke Actions ---
    fun updateApprovalStatus(approvalId: UUID, newStatus: String, notes: String? = null) {
        if (_isUpdatingId.value == approvalId) return // Prevent double-click

        _isUpdatingId.value = approvalId
        viewModelScope.launch {
            try {
                // The DTO construction here IS compatible, as 'notes' defaults to null
                // if no value is passed, or uses the value provided by the Composable.
                val updateDTO = UpdateWeeklyApprovalDTO(approvalId, newStatus, notes)
                val response: ApiResponse = workApi.updateWeeklyApproval(updateDTO)

                when(response){
                    is ApiResponse.Success -> {
                        updateLocalApproval(approvalId, newStatus, notes) // Update local function signature
                        _snackbarEvent.send("Weekly record ${newStatus.lowercase()} successfully.")
                    }
                    is ApiResponse.Error -> {
                        _snackbarEvent.send("Failed to update status: ${response.message}")
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _snackbarEvent.send("API Error: Failed to update status: ${e.message}")
            } finally {
                _isUpdatingId.value = null
            }
        }
    }
    private fun updateLocalApproval(id: UUID, newStatus: String, newNotes: String? = null) {
        _weeklyApprovalsList.update { list ->
            list.map { dto ->
                if (dto.id == id) {
                    dto.copy(
                        status = newStatus,
                        notes = newNotes ?: dto.notes // Use new notes if provided, otherwise keep existing
                    )
                } else {
                    dto
                }
            }
        }
    }
}