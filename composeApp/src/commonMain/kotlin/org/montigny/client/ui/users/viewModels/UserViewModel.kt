package org.montigny.client.ui.users.viewModels

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.com.client.database.User_type
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.UserTypeRepository
import org.montigny.client.util.ViewModel
import org.slf4j.Logger
class UserViewModel(private val userTypeRepository: UserTypeRepository,
private val userApi: UserApi,
    private val logger: Logger) : ViewModel() {

private val  _useTypes  = MutableStateFlow<List<User_type>> (emptyList())
val useTypes: StateFlow<List<User_type>> = _useTypes
init {
    populateUserTypes()
}
  fun populateUserTypes() {
        viewModelScope.launch {
            try {
                logger.info("Populating user types")
               pickDataSource()

            }catch (e: Exception) {
                logger.error("failed to populate user types Error ${e.message}")
            }
        }
    }

    /*
    * Check network if on  user UserApi.getAllUserTypes
    *
    * */
private suspend fun pickDataSource(isNetworkAvailable: Boolean = false) {
          if(isNetworkAvailable){
             val userTypes: List<User_type> =  userApi.getUserTypes().map { User_type(
                  id = it.id.toString(),
                  created_at = it.displayName,
                  updated_at = it.displayName,
                  created_by = it.displayName,
                  updated_by = it.displayName,
                  displayname = it.displayName,
              )
              }
              _useTypes.value = userTypes
          }
        else{
              val userTypes: List<User_type> =  userTypeRepository.getAll()
              _useTypes.value = userTypes
        }
}
 private    fun deleteUserTypes() {
        viewModelScope.launch {
            try {
                userTypeRepository.deleteAllUserType()
            }catch (e: Exception) {
                logger.error("failed to delete user type error ${e.message}")
            }
        }
    }


}