package org.montigny.client.ui.work.viewModel

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.ViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class LoadUserWorkViewModel(
    private val  workApi: WorkApi,
    private  val userApi: UserApi,
    private  val sessionManager: SessionManager,
    private val  connectivityObserver: ConnectivityObserver
): ViewModel() {
    private  val  _user = MutableStateFlow<UserResponse?>(null)
    val user: StateFlow<UserResponse?> = _user

    private val _dailyWorkSummaries = MutableStateFlow<List<WorkDaySummaryDTO>>(emptyList())
    val  dailyWorkSummaries: StateFlow<List<WorkDaySummaryDTO>> = _dailyWorkSummaries
    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto


    private  val _startDate = MutableStateFlow<String?>(null)
    val startDate: StateFlow<String?> = _startDate

    private  val _endDate = MutableStateFlow<String?>(null)
    val endDate: StateFlow<String?> = _endDate


    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    // --- NEW: UI Event Channel for one-time messages (like Snackbars) ---
    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow() // Use Flow for observing
    // ------------------------------------------------------------------
    fun loadUser(userId : String) {
        viewModelScope.launch {
        try {
            if (isNetworkAvailable.value && isOnlineLoggedIn.value) {
                _user.value = userApi.findUserResponseById(userId.toUUID())

            }
            else {
                println("Update snackbar message : These features require a network available + login ")
            }
        }catch (e: Exception) {
            println("Update snackbar message : ${e.message}")
        }
     }
    }

    fun populateDailyWorkSummaries(page: Int = DEFAULT_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (page > _totalPages.value) {
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            val currentFilters = _filterDto.value.copy(
                includeInactiveWorkers = true,
                filterUserId = _user.value?.id?.toString()?.toUUID()
            )

            val resp = workApi.getDailyWorkSummariesByUser(page, pageSize, currentFilters)
            println("filters:   ${currentFilters.filterDate} ${currentFilters.filterDateEndDate} ${currentFilters.filterUserId}")
            println("resp ==:$resp")
            println ("_daily work summaries ${dailyWorkSummaries.value}")
            resp?.let {
                _totalPages.value = it.totalPages
                if (page <= 1) {
                    _dailyWorkSummaries.value = it.items
                } else {
                    _dailyWorkSummaries.value += it.items
                }
            }
        }
    }
    fun clearDateFilters() {
        _startDate.value = null
        _endDate.value = null
        _filterDto.value = _filterDto.value.copy(
            filterDate = null,
            filterDateEndDate = null
        )
        _totalPages.value = 1
        populateDailyWorkSummaries(page = DEFAULT_PAGE)
    }
    fun onStartDateChangeListener(newValue: String) {
        _startDate.value = newValue
    }
    fun onEndDateChangeListener(newValue: String) {
        _endDate.value = newValue
    }
    fun submitDateFilter(startDate: String?, endDate: String?) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        viewModelScope.launch { // Launch in viewModelScope to use send()
            // 1. Initial Validation: Check date format and logical order
            try {
                val start = startDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, formatter) }
                val end = endDate?.takeIf { it.isNotBlank() }?.let { LocalDate.parse(it, formatter) }

                if (start != null && end != null && start.isAfter(end)) {
                    // Send the error message to the UI
                    _snackbarEvent.send("Start Date cannot be after End Date. 🛑")
                    return@launch
                }
                // 2. Update Filter DTO
                _filterDto.value = _filterDto.value.copy(
                    filterDate = startDate, filterDateEndDate = endDate
                )
                _totalPages.value = 1
                populateDailyWorkSummaries(page = DEFAULT_PAGE)

            } catch (e: DateTimeParseException) {
                // Send the error message to the UI
                _snackbarEvent.send("Invalid date format. Please use YYYY-MM-DD. 📅")
            }
        }
    }
}