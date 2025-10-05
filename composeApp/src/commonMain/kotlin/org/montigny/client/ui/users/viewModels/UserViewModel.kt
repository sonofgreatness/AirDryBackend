package org.montigny.client.ui.users.viewModels

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.client.database.User_type
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState
import org.slf4j.Logger
import java.util.UUID
import kotlin.jvm.Throws

class UserViewModel(private val userRepository: UserRepository,
                    private val userApi: UserApi,
                    private val organizationRepository: OrganizationRepository,
                    private val organizationApi: OrganizationApi,
                    private val logger: Logger,
                   private   val syncManager: SyncManager,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {


private val  _useTypes  = MutableStateFlow<List<User_type>> (emptyList())
val useTypes: StateFlow<List<User_type>> = _useTypes

private val _departments = MutableStateFlow<List<CreateOrgDTO>>(emptyList())
val  departments: StateFlow<List<CreateOrgDTO>> = _departments

private val _sections = MutableStateFlow<List<CreateSectDTO>>(emptyList())
val  sections: StateFlow<List<CreateSectDTO>> = _sections

private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
 val users: StateFlow<List<UserResponse>> = _users

private val _updateUser = MutableStateFlow<UserRegisterRequest?> (null)
    val updateUser: StateFlow<UserRegisterRequest?> = _updateUser

private val _createUser = MutableStateFlow<UserRegisterRequest?> (UserRegisterRequest(
    id = UUID.randomUUID(),
    name ="", email = null, phoneNumber = "",surname = "",
    password = "" , secondaryPhoneNumber = null,)
)
val createUser: StateFlow<UserRegisterRequest?> = _createUser

private val _createUserState = MutableStateFlow<UiState> (UiState.Idle)
val createUserState: StateFlow<UiState> = _createUserState

private val _isSyncing = MutableStateFlow(false)
val isSyncing: StateFlow<Boolean> = _isSyncing
val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable


fun onCreateUserChange(newValue: String, state: UserRegisterDTOState) {

    when (state) {
        is UserRegisterDTOState.departmentId-> {
            populateSections(UUID.fromString(newValue))
            val copy = _createUser.value?.copy(departmentId = UUID.fromString(newValue))
            _createUser.value= copy

        }
        is UserRegisterDTOState.email -> {
            val copy = _createUser.value?.copy(email = newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.isActive -> {
            val copy = _createUser.value?.copy(isActive =state.value)
            _createUser.value= copy
        }
        is  UserRegisterDTOState.isApproved ->{
            val copy = _createUser.value?.copy(isApproved =state.value)
            _createUser.value= copy
        }
        is  UserRegisterDTOState.name -> {
            val copy = _createUser.value?.copy(name =newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.organizationId -> {
            val copy = _createUser.value?.copy(organizationId = UUID.fromString(newValue))
            _createUser.value= copy
        }
        is UserRegisterDTOState.password -> {
            val copy = _createUser.value?.copy(password = newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.phoneNumber -> {
            val copy = _createUser.value?.copy(phoneNumber = newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.secondaryPhoneNumber -> {
            val copy = _createUser.value?.copy(secondaryPhoneNumber = newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.sectionId -> {
            val copy = _createUser.value?.copy(sectionId = UUID.fromString(newValue))
            _createUser.value= copy
        }
        is UserRegisterDTOState.surname -> {
            val copy = _createUser.value?.copy(surname = newValue)
            _createUser.value= copy
        }
        is UserRegisterDTOState.userTypeId -> {
            val copy = _createUser.value?.copy(userTypeId = UUID.fromString(newValue))
            _createUser.value= copy
        }
    }
}

fun initializeEmptyCreateUse(){
    logger.info("Initializing empty CreateUse")
    _createUser.value = UserRegisterRequest(
        name ="", email = null, phoneNumber = "", surname = "",
        password = "" , secondaryPhoneNumber = null, id = UUID.randomUUID())
}
fun resetCreateUserState()
{
 _createUserState.value = UiState.Idle
}

init {
    populateUserTypes()
    populateUsers()
    populateDepartments()

}

    fun synchronizeUserData(){
        synchronizeUserDataInternal(isNetworkAvailable.value)
    }

     private fun synchronizeUserDataInternal(isNetworkAvailable: Boolean =false) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                println("Dowloading network 00")
                if (isNetworkAvailable) {
                    println("Dowloading network")
                    syncManager.downloadUserTypesAndUsers()//deletes old to insert new
                    syncManager.downLoadDepartmentsAndSections()
                }
                populateAUserLists()
                populateDepartmentsAndSections()
            } catch (e: Exception) {
                logger.error("Sync failed: ${e.message}")
            } finally {
                _isSyncing.value = false
            }
        }
    }


    fun populateAUserLists(){
        populateUserTypes()
        populateUsers()
}
private fun populateUsers(){
    viewModelScope.launch {
        try {
            logger.info("Populating users ")
            pickUserDataSource(isNetworkAvailable.value)
        }catch (e: Exception) {
            logger.error("failed to populate users Error ${e.message}")
        }
    }
}

private fun populateUserTypes() {
        viewModelScope.launch {
            try {
                logger.info("Populating user types")
               pickDataSource(isNetworkAvailable.value)

            }catch (e: Exception) {
                logger.error("failed to populate user types Error ${e.message}")
            }
        }
    }
    @Throws(Exception::class)
   private suspend fun pickDataSource(isNetworkAvailable: Boolean = false) {
          if(isNetworkAvailable){
             val userTypes: List<User_type> =  userApi.getUserTypes().map { User_type(
                  id = it.id.toString(),
                  displayname = it.displayName,
                 is_pending_update = 0L, is_pending_insert = 0L,
              )
              }
              _useTypes.value = userTypes
          }
        else{
              val userTypes: List<User_type> =  userRepository.getAll()
              _useTypes.value = userTypes
        }
   }
private suspend fun pickUserDataSource(isNetworkAvailable: Boolean = false) {
      try {
          if (isNetworkAvailable) {
              val users: List<UserResponse> = userApi.getUsers().items
              _users.value = users

          } else {
              val users: List<UserResponse> = userRepository.selectAllUsers() ?: emptyList()
              _users.value = users
          }
      }catch (e: Exception){
          logger.error("failed to select users Error pickDataSource ${e.message}")
      }
    }


fun populateDepartmentsAndSections(){
    populateDepartments()
}
private fun populateDepartments(){

    viewModelScope.launch {
        try {
            logger.info("Populating departments")
            pickDepartmentDataSource(isNetworkAvailable.value)
        }catch (e: Exception) {
            logger.error("failed to populate departments Error ${e.message}")
        }
    }
}

private suspend fun pickDepartmentDataSource(isNetworkAvailable: Boolean = false) {
         try{
             if (isNetworkAvailable){
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
        logger.info("Populating sections")
        pickSectionDataSource(isNetworkAvailable.value,departmentId)
    }
    catch (e: Exception){
        logger.error("failed to populate sections Error ${e.message}")
    }
    }
}
private  suspend fun  pickSectionDataSource(isNetworkAvailable: Boolean = false, departmentId:UUID){
    try {
        if(isNetworkAvailable){

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


    fun createUser(user: UserRegisterRequest=_createUser.value!!) {
       viewModelScope.launch {
           pickUserCreateUploadSource(isNetworkAvailable.value, user=user)
       }

    }
    private  suspend  fun pickUserCreateUploadSource(isNetworkAvailable: Boolean = false, user: UserRegisterRequest) {
        if(isNetworkAvailable){
            _createUserState.value = UiState.Loading
            when(val response =  userApi.createUser(user)){
                is ApiResponse.Error -> {
                    _createUserState.value = UiState.Error(response.message)
                }
                else -> {
                    _createUserState.value = UiState.Success
                }
            }
        }
        else{
            _createUserState.value = UiState.Loading
            userRepository.insertAppUserToCacheForLaterUpload(user=user)
            _createUserState.value = UiState.Success
        }
    }
}