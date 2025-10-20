package org.montigny.client.ui.work

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
}