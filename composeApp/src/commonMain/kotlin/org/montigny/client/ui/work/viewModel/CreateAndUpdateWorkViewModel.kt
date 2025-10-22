package org.montigny.client.ui.work.viewModel

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger

class CreateAndUpdateWorkViewModel(
    private val workApi: WorkApi,
    private val userApi: UserApi,
    private val stackApi: StackApi,
    private val connectivityObserver: ConnectivityObserver,
    private val sessionManager: SessionManager,
    private val logger: Logger
) : ViewModel() {

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn: StateFlow<Boolean> = sessionManager.isLoggedInFlow

    private val _casualWorkers = MutableStateFlow<List<UserResponse>>(emptyList())
    val casualWorkers: StateFlow<List<UserResponse>> get() = _casualWorkers

    private val _stacks = MutableStateFlow<List<GetStackDTO>>(emptyList())
    val stacks: StateFlow<List<GetStackDTO>> get() = _stacks

    private val _casualWorkerQuery = MutableStateFlow<String?>(null)
    val casualWorkerQuery: StateFlow<String?> get() = _casualWorkerQuery

    private val _stacksQuery = MutableStateFlow<String?>(null)
    val stacksQuery: StateFlow<String?> get() = _stacksQuery

    private val _stackUiState = MutableStateFlow<UiState>(UiState.Idle)
    val stackUiState: StateFlow<UiState> get() = _stackUiState

    private val _workerUiState = MutableStateFlow<UiState>(UiState.Idle)
    val workerUiState: StateFlow<UiState> get() = _workerUiState

    // Pagination tracking
    private var stackPage = DEFAULT_PAGE
    private var stackTotalPages = 1
    private var isStackLoading = false

    private var workerPage = DEFAULT_PAGE
    private var workerTotalPages = 1
    private var isWorkerLoading = false



    // Define a sealed class for UI Events
    sealed class WorkUiEvent {
        data class ShowSnackbar(val message: String) : WorkUiEvent()
        object WorkSaved : WorkUiEvent()
        object WorkUpdated : WorkUiEvent()
    }

    private val _uiEvents = Channel<WorkUiEvent>()
    val uiEvents = _uiEvents.receiveAsFlow() // Expose as Flow

    private val _workDataToPopulate = MutableSharedFlow<CreateWorkDoneDTO>()
    val workDataToPopulate = _workDataToPopulate.asSharedFlow() // Use SharedFlow for data that needs consumption

    fun populateStacks(page: Int = DEFAULT_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (isStackLoading || page > stackTotalPages) return

        viewModelScope.launch {
            try {
                isStackLoading = true
                _stackUiState.value = UiState.Loading

                val response = stackApi.getPaginatedStacks(page, pageSize, stacksQuery.value)
                response?.let {
                    if (page == DEFAULT_PAGE) {
                        _stacks.value = it.items
                    } else {
                        _stacks.value += it.items
                    }

                    stackPage = it.page
                    stackTotalPages = it.totalPages
                }

                _stackUiState.value = UiState.Success
                logger.info("Loaded stacks page $page / $stackTotalPages")
            } catch (e: Exception) {
                _stackUiState.value = UiState.Error("Failed to load stacks")
                logger.error("Failed to load stacks", e)
            } finally {
                isStackLoading = false
            }
        }
    }

    fun populateWorkers(page: Int = DEFAULT_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (isWorkerLoading || page > workerTotalPages) return

        viewModelScope.launch {
            try {
                isWorkerLoading = true
                _workerUiState.value = UiState.Loading

                val response = userApi.getCasualUsers(page, pageSize, casualWorkerQuery.value)
                response?.let {
                    if (page == DEFAULT_PAGE) {
                        _casualWorkers.value = it.items
                    } else {
                        _casualWorkers.value += it.items
                    }

                    workerPage = it.page
                    workerTotalPages = it.totalPages
                }

                _workerUiState.value = UiState.Success
                logger.info("Loaded users page $page / $workerTotalPages")
            } catch (e: Exception) {
                _workerUiState.value = UiState.Error("Failed to load workers")
                logger.error("Failed to load workers", e)
            } finally {
                isWorkerLoading = false
            }
        }
    }

    fun setWorkerQuery(newQuery: String?) {
        _casualWorkerQuery.value = newQuery
        workerPage = DEFAULT_PAGE
        workerTotalPages = 1
        _casualWorkers.value = emptyList()
        populateWorkers()
    }

    fun setStackQuery(newQuery: String?) {
        _stacksQuery.value = newQuery
        stackPage = DEFAULT_PAGE
        stackTotalPages = 1
        _stacks.value = emptyList()
        populateStacks()
    }
    fun createWork(dto: CreateWorkDoneDTO) {
        viewModelScope.launch {
            try {
                when (val resp = workApi.createWork(dto)) {
                    is ApiResponse.Error -> {
                        // Send error message to UI
                        _uiEvents.send(WorkUiEvent.ShowSnackbar(resp.message ?: "An unknown error occurred."))
                    }
                    else -> {
                        // Send success message and navigation/reset signal
                        _uiEvents.send(WorkUiEvent.ShowSnackbar("Work saved successfully!"))
                        _uiEvents.send(WorkUiEvent.WorkSaved) // Optional: signal for navigation or list refresh
                    }
                }
            } catch (e: Exception) {
                // Send general exception message to UI
                _uiEvents.send(WorkUiEvent.ShowSnackbar("Connection Error: ${e.message}"))
            }
        }
    }

    fun updateWork(workId: String, dto: CreateWorkDoneDTO) {
        viewModelScope.launch {
            try {
                // Note: Assuming your API has a dedicated update endpoint or handles updates via the createWork endpoint
                // If there's a specific updateWorkApi.updateWork(workId.toUUID(), dto), use that instead.
                // For now, we'll assume a new WorkApi function `updateWork` is available.
                when (val resp = workApi.updateWork( dto.copy(id = workId.toUUID()))) { // <--- ASSUME THIS NEW API FUNCTION
                    is ApiResponse.Error -> {
                        _uiEvents.send(WorkUiEvent.ShowSnackbar(resp.message ?: "Failed to update work."))
                    }
                    else -> {
                        _uiEvents.send(WorkUiEvent.ShowSnackbar("Work updated successfully!"))
                        _uiEvents.send(WorkUiEvent.WorkUpdated) // Signal update success
                    }
                }
            } catch (e: Exception) {
                _uiEvents.send(WorkUiEvent.ShowSnackbar("Connection Error during update: ${e.message}"))
            }
        }
    }


    // --- MODIFIED: loadWork function ---
    fun loadWork(id: String) {
        viewModelScope.launch {
            try {
                val workId = id.toUUID()
                val workDone = workApi.getWorkById(workId)

                if (workDone != null) {
                    // Send the DTO to the UI to populate the selected lists
                    _workDataToPopulate.emit(workDone)
                } else {
                    _uiEvents.send(WorkUiEvent.ShowSnackbar("Work entry not found."))
                }
            } catch (e: Exception) {
                _uiEvents.send(WorkUiEvent.ShowSnackbar("Failed to load work data for update."))
                logger.error("Failed to load work data for update", e)
            }
        }
    }
}
