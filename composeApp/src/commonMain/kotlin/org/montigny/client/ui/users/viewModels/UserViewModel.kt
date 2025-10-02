package org.montigny.client.ui.users.viewModels

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.client.database.User_type
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.ViewModel
import org.slf4j.Logger
import kotlin.jvm.Throws

class UserViewModel(private val userRepository: UserRepository,
                    private val userApi: UserApi,
                    private val logger: Logger) : ViewModel() {

private val  _useTypes  = MutableStateFlow<List<User_type>> (emptyList())
val useTypes: StateFlow<List<User_type>> = _useTypes
private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
 val users: StateFlow<List<UserResponse>> = _users

    val syncManager = SyncManager(userApi,userRepository, settings = Settings())
    init {

    populateUsers()
    populateUserTypes()
}




fun populateUsers(){
    viewModelScope.launch {
        try {
            logger.info("Populating users ")
            syncManager.downloadAndInsertUsers()
            pickUserDataSource()

        }catch (e: Exception) {
            logger.error("failed to populate users Error ${e.message}")
        }
    }
}


  fun populateUserTypes() {
        viewModelScope.launch {
            try {
                logger.info("Populating user types")
                syncManager.downloadAndInsertUserTypes()
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
              )
              }
              _useTypes.value = userTypes
          }
        else{

              val userTypes: List<User_type> =  userRepository.getAll()
              _useTypes.value = userTypes
        }
   }

    @Throws(Exception::class)
    private suspend fun pickUserDataSource(isNetworkAvailable: Boolean = false) {
        if(isNetworkAvailable){
            val users: List<UserResponse> =  userApi.getUsers().items
            _users.value = users

        }
        else{
            val users: List<UserResponse> =userRepository.selectAllUsers()?:emptyList()
                _users.value = users
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


}