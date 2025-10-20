package org.montigny.client.ui.work

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.ViewModel

class WorkViewModel(
    private val workApi: WorkApi,
):
ViewModel(){
    private val _dailyWorkSummaries = MutableStateFlow<List<WorkDaySummaryDTO>>(emptyList())
    val  dailyWorkSummaries: StateFlow<List<WorkDaySummaryDTO>> = _dailyWorkSummaries
    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto
    fun populateDailyWorkSummaries(page: Int = DEFAULT_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE) {
        viewModelScope.launch {
            val resp = workApi.getDailyWorkSummaries(page, pageSize)
            resp?.let {
                // Append items on subsequent pages
                if (page <= 1) {
                    _dailyWorkSummaries.value = it.items
                } else {
                    _dailyWorkSummaries.value += it.items
                }
            }
        }
    }
    fun onChangeFilter(state : WorkDataFiltersDTOState, newValue: String) {
        when(state) {
            is WorkDataFiltersDTOState.IncludeInactiveWorkers ->{
                _filterDto.value = _filterDto.value.copy(includeInactiveWorkers = state.newValue)
            }
            is WorkDataFiltersDTOState.FilterDepartment -> {
                _filterDto.value = _filterDto.value.copy(filterDepartment = newValue)
            }
            is WorkDataFiltersDTOState.FilterDate -> {
                _filterDto.value = _filterDto.value.copy(filterDate = newValue)
            }
            is WorkDataFiltersDTOState.FilterName -> {
                _filterDto.value = _filterDto.value.copy(filterName = newValue)
            }
            is WorkDataFiltersDTOState.FilterSection -> {
                _filterDto.value = _filterDto.value.copy(filterSection = newValue)
            }
        }
    }
}
@Serializable
data class  WorkDataFiltersDTO (
    val includeInactiveWorkers: Boolean = false,
    val  filterDate : String? = null, val  filterDepartment :String? = null,
    val  filterSection :String? = null,
    val  filterName :String? = null,
)
sealed class  WorkDataFiltersDTOState {
    data class IncludeInactiveWorkers(val newValue : Boolean) : WorkDataFiltersDTOState()
    object  FilterDate : WorkDataFiltersDTOState()
    object  FilterSection : WorkDataFiltersDTOState()
    object  FilterName : WorkDataFiltersDTOState()
    object  FilterDepartment : WorkDataFiltersDTOState()

}