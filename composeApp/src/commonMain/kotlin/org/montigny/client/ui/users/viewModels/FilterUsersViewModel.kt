package org.montigny.client.ui.users.viewModels

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.dto.WorkDataFiltersDTO
import org.montigny.client.ui.work.viewModel.WorkDataFiltersDTOState
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE
import org.montigny.client.util.Constants.Companion.DEFAULT_PAGE_SIZE
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.montigny.client.util.ViewModel
import org.slf4j.Logger
import java.util.*

class FilterUsersViewModel(
    private val userApi: UserApi,
    private val sessionManager: SessionManager,
    private val  connectivityObserver: ConnectivityObserver,
    private val  logger: Logger,
    private  val organizationApi: OrganizationApi,
    private val organizationRepository: OrganizationRepository,
): ViewModel(){

    private val _users  = MutableStateFlow<List<UserResponse>>(emptyList())
    val users  : StateFlow<List<UserResponse>> get() = _users
    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow
    private val _departments = MutableStateFlow<List<CreateOrgDTO>>(emptyList())
    val  departments: StateFlow<List<CreateOrgDTO>> = _departments

    private val _sections = MutableStateFlow<List<CreateSectDTO>>(emptyList())
    val  sections: StateFlow<List<CreateSectDTO>> = _sections
    

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val  _filterDto  = MutableStateFlow<WorkDataFiltersDTO>(WorkDataFiltersDTO())
    val  filterDto: StateFlow<WorkDataFiltersDTO> = _filterDto

    private val _snackbarEvent = Channel<String>()
    val snackbarEvent = _snackbarEvent.receiveAsFlow() // Use Flow for observing

    private val _currentPage = MutableStateFlow(DEFAULT_PAGE)
    val currentPage: StateFlow<Int> = _currentPage


    fun clearAllFilters(){
        println("clear filters called")
        _filterDto.value = WorkDataFiltersDTO()
        _totalPages.value = DEFAULT_PAGE
        _currentPage.value = DEFAULT_PAGE // <-- RESET CURRENT PAGE
        populateUsers()
    }
    fun onChangeFilter(state : WorkDataFiltersDTOState, newValue: String) {
        _currentPage.value = DEFAULT_PAGE
        _totalPages.value = DEFAULT_PAGE
        when(state) {
            is WorkDataFiltersDTOState.IncludeInactiveWorkers -> {
                _filterDto.value = _filterDto.value.copy(includeInactiveWorkers = state.newValue)
                populateUsers()
            }
            is WorkDataFiltersDTOState.FilterDepartment ->{
                _filterDto.value = _filterDto.value.copy(filterDepartmentId = newValue.toUUIDOrNull())
                populateSections(UUID.fromString(newValue))
                populateUsers()
            }
            is WorkDataFiltersDTOState.FilterDate ->{
            //NA
            }
            is WorkDataFiltersDTOState.FilterName -> {
                _filterDto.value = _filterDto.value.copy(filterName = newValue)
                populateUsers()
            }
            is WorkDataFiltersDTOState.FilterSection ->{
                _filterDto.value = _filterDto.value.copy(filterSectionId = newValue.toUUIDOrNull())
                populateUsers()}
            is WorkDataFiltersDTOState.FilterSurname->{
                _filterDto.value = _filterDto.value.copy(filterSurname = newValue)
                populateUsers()
            }
            is WorkDataFiltersDTOState.FilterPhone ->{
                _filterDto.value  = _filterDto.value.copy(filterPhoneNumber = newValue)
                populateUsers()
            }
            is WorkDataFiltersDTOState.FilterSecondaryPhone -> {
                _filterDto.value = _filterDto.value.copy(
                    filterSecondaryPhoneNumber = newValue
                )
                populateUsers()
            }
        }
    }
    fun loadNextPage(pageSize: Int = DEFAULT_PAGE_SIZE) {
        if (_currentPage.value < _totalPages.value) {
            _currentPage.value++
            populateUsers(pageSize)
        }
    }
    fun populateUsers(pageSize: Int = DEFAULT_PAGE_SIZE){
        val page = _currentPage.value
        if (page > _totalPages.value) {
            return
        }
        viewModelScope.launch {
          try {
              if(!isNetworkAvailable.value)
                  _snackbarEvent.send("Please check your internet connection!")
              if(!isOnlineLoggedIn.value)
                  _snackbarEvent.send("Please login to continue!")
            val resp = userApi.getFilteredUsers(page, pageSize, filterDto.value)
              resp?.let {
                 _totalPages.value = it.totalPages
                if (page <= 1) {
                    println("less than 1 $page")
                    _users.value = it.items
                }else {
                    println("less than more than $page")
                    _users.value += it.items
                }
            }
            }catch(e: Exception){
                logger.error(e.toString())
                 _snackbarEvent.send(e.message ?: e.toString())
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
                _snackbarEvent.send("failed to populate departments!")
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
                _snackbarEvent.send("There was an error loading sections.")
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