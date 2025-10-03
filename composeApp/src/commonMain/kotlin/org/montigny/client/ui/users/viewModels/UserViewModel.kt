package org.montigny.client.ui.users.viewModels

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.client.database.User_type
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.montigny.client.util.stateClasses.UserRegisterDTOState
import org.slf4j.Logger
import java.util.UUID
import kotlin.jvm.Throws

class UserViewModel(private val userRepository: UserRepository,
                    private val userApi: UserApi,
                    private val logger: Logger) : ViewModel() {


    val syncManager = SyncManager(userApi,userRepository, settings = Settings())

private val  _useTypes  = MutableStateFlow<List<User_type>> (emptyList())
val useTypes: StateFlow<List<User_type>> = _useTypes

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



fun onCreateUserChange(newValue: String, state: UserRegisterDTOState) {

    when (state) {
        is UserRegisterDTOState.departmentId-> {
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

}
     fun synchronizeUserData(isNetworkAvailable: Boolean =false) {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                if (isNetworkAvailable) {
                    syncManager.downloadAndInsertUserTypes()
                    syncManager.downloadAndInsertUsers()
                }
                populateAUserLists()
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
            pickUserDataSource()
        }catch (e: Exception) {
            logger.error("failed to populate users Error ${e.message}")
        }
    }
}

private fun populateUserTypes() {
        viewModelScope.launch {
            try {
                logger.info("Populating user types")
               pickDataSource()

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
private    fun deleteUserTypes() {
        viewModelScope.launch {
            try {
                userRepository.deleteAllUserType()
            }catch (e: Exception) {
                logger.error("failed to delete user type error ${e.message}")
            }
        }
    }


    /**
     * If network is available add to remote Api
     * if not cache user
     **/
    fun createUser(user: UserRegisterRequest=_createUser.value!!) {
       viewModelScope.launch {
           pickUserCreateUploadSource(user=user)
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
            userRepository.insertAppUser(user)
            _createUserState.value = UiState.Success
        }
    }
}