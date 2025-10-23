package org.montigny.client.ui.work.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.WorkApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Work.WorkDaySummaryDTO
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.montigny.client.util.ViewModel
import org.slf4j.Logger
import java.util.*

//LOAD, DISPLAY WORK
class LoadWorkViewModel(
    private val workApi: WorkApi,
    private  val organizationApi: OrganizationApi,
    private  val organizationRepository: OrganizationRepository,
    private val logger : Logger,
    connectivityObserver: ConnectivityObserver,
    sessionManager: SessionManager
):
ViewModel(){
    private val _dailyWorkSummaries = MutableStateFlow<List<WorkDaySummaryDTO>>(emptyList())
    val  dailyWorkSummaries: StateFlow<List<WorkDaySummaryDTO>> = _dailyWorkSummaries
    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto

    private val _departments = MutableStateFlow<List<CreateOrgDTO>>(emptyList())
    val  departments: StateFlow<List<CreateOrgDTO>> = _departments

    private val _sections = MutableStateFlow<List<CreateSectDTO>>(emptyList())
    val  sections: StateFlow<List<CreateSectDTO>> = _sections

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages


    fun clearAllFilters(){
        _filterDto.value = WorkDataFiltersDTO()
        _totalPages.value = DEFAULT_PAGE
        populateDailyWorkSummaries()
    }
    fun onChangeFilter(state : WorkDataFiltersDTOState, newValue: String) {
        when(state) {
            is WorkDataFiltersDTOState.IncludeInactiveWorkers -> {
                _filterDto.value = _filterDto.value.copy(includeInactiveWorkers = state.newValue)
            populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterDepartment ->{
                _filterDto.value = _filterDto.value.copy(filterDepartmentId = newValue.toUUIDOrNull())
                populateSections(UUID.fromString(newValue))
                populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterDate ->{
                _filterDto.value = _filterDto.value.copy(filterDate = newValue)
             populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterName -> {
                _filterDto.value = _filterDto.value.copy(filterName = newValue)
            populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterSection ->{
                _filterDto.value = _filterDto.value.copy(filterSectionId = newValue.toUUIDOrNull())
            populateDailyWorkSummaries()}
            is WorkDataFiltersDTOState.FilterSurname->{
                _filterDto.value = _filterDto.value.copy(filterSurname = newValue)
                populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterPhone ->{
                _filterDto.value  = _filterDto.value.copy(filterPhoneNumber = newValue)
            populateDailyWorkSummaries()
            }
            is WorkDataFiltersDTOState.FilterSecondaryPhone -> {
                _filterDto.value = _filterDto.value.copy(
                    filterSecondaryPhoneNumber = newValue
                )
                populateDailyWorkSummaries()
            }
        }
    }

    fun populateDailyWorkSummaries(page: Int = DEFAULT_PAGE, pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (page > _totalPages.value) {
            return
        }
        viewModelScope.launch {
            println("getDailyWorkSummaries called with date ${filterDto.value.filterDate}")
            val resp = workApi.getDailyWorkSummaries(page, pageSize, filterDto.value)
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

    fun populateDepartmentsAndSections(){
        populateDepartments()
    }
    private fun populateDepartments(){
        viewModelScope.launch {
            try {
                logger.info("Populating departments")
                pickDepartmentDataSource(isNetworkAvailable.value,isOnlineLoggedIn.value)
            }catch (e: Exception) {
                logger.error("failed to populate departments Error ${e.message}")
            }
        }
    }

    private suspend fun pickDepartmentDataSource(isNetworkAvailable: Boolean = false,isOnlineLoggedIn: Boolean) {
        try{
            if (isNetworkAvailable &&isOnlineLoggedIn){
                val departmentsVariable : List<CreateOrgDTO> =organizationApi.getAllDept()
                _departments.value = departmentsVariable
            }else{
                val  departmentsVariable : List<CreateOrgDTO> = organizationRepository.getAllDepartments()
                _departments.value = departmentsVariable
            }
        }catch (e: Exception){
            logger.error("failed to populate departments pickDepartmentDataSourceError ${e.message}")
        }
    }

    private  fun  populateSections(departmentId:UUID){
        viewModelScope.launch {
            try{
                pickSectionDataSource(isNetworkAvailable.value,departmentId, isOnlineLoggedIn.value)
            }
            catch (e: Exception){
                logger.error("failed to populate sections Error ${e.message}")
            }
        }
    }
    private  suspend fun  pickSectionDataSource(isNetworkAvailable: Boolean = false,
                                                departmentId:UUID, isOnlineLoggedIn: Boolean){
        try {
            if(isNetworkAvailable && isOnlineLoggedIn){
                val sections = organizationApi.getSectionByDepartmentId(departmentId)
                _sections.value = sections?:emptyList()
            }
            else{
                val sections = organizationRepository.getSectionByDepartmentId(departmentId)
                _sections.value = sections?:emptyList()
            }
        }  catch (e: Exception){
            println("Error populating sections ${e.message}")
        }
    }
}
sealed class  WorkDataFiltersDTOState {
    data class IncludeInactiveWorkers(val newValue : Boolean) : WorkDataFiltersDTOState()
    object  FilterDate : WorkDataFiltersDTOState()
    object  FilterSection : WorkDataFiltersDTOState()
    object  FilterName : WorkDataFiltersDTOState()
    object  FilterDepartment : WorkDataFiltersDTOState()
    object  FilterSurname : WorkDataFiltersDTOState()
    object  FilterPhone : WorkDataFiltersDTOState()
    object  FilterSecondaryPhone : WorkDataFiltersDTOState()

}