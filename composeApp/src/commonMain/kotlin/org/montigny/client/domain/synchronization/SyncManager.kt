package org.montigny.client.domain.synchronization

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow
import org.com.client.database.User_type
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.FRESH_DATA_KEY
import org.montigny.client.util.Constants.Companion.NOT_LOGGED_IN_ERROR
import org.montigny.client.util.Constants.Companion.USER_FOUND_NOT_ERROR
import java.time.Instant
import java.util.UUID
import kotlin.time.Duration.Companion.hours


/******************************************************************************
 * Responsible for handling sync  of remote database with
 *   LocalDatabase
 *@params:UserApi - retrieves  remote records
 *@params: UserTypeRepository - used to update LocalDatabase table User_type
 ****************************************************************************/
class SyncManager(private val userApi : UserApi,
                  private val userRepository: UserRepository,
                  private val organizationRepository: OrganizationRepository,
                  private val organizationApi: OrganizationApi,
                 private val  connectivityObserver: ConnectivityObserver,
                 private val settings: Settings,
                 private val  sessionManager: SessionManager,
    private val offlineSessionManager: OfflineSessionManager


)
{

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
     val isOnlineLoggedInd: StateFlow<Boolean> = sessionManager.isLoggedInFlow


    /**-- USERS AND USER TYPES-----------------------------
    * Must check network before making delete choice
    *1. Download user data from api
     *    deletes old data  inserts
     *    downloaded users
     *
     * 2. Download  user_types from api
     *    deletes old user_types ,
     *    inserts downloaded data
    *---------------------------------------------------*/
    suspend fun downloadUserTypesAndUsers(){
        downloadAndInsertUsers()
        downloadAndInsertUserTypes()
    }

   private suspend fun  downloadAndInsertUsers(){
        try {
            val remoteUsers: List<UserRegisterRequest> = userApi.getUsersUnPaged()
            val localUsers = userRepository.selectAllUsers()
           if(isNetworkAvailable.value) {// do more checks
               if (!localUsers.isNullOrEmpty())
                   userRepository.deleteAllUsers()
               remoteUsers.forEach {
                   userRepository.insertAppUser(it)
               }
           }else{
               return
           }
        }catch (e:Exception){
            println("Download failed ${e.localizedMessage}")
        }
    }

    private  suspend  fun downloadAndInsertUserTypes() {

        val localUserTypes = userRepository.getAll()

        if (isNetworkAvailable.value && isOnlineLoggedInd.value) {//do more checks here (isDataStale)
            val remoteUserTypes: List<UserTypesDTO> = userApi.getUserTypes()

            if (localUserTypes.isNotEmpty())
                userRepository.deleteAllUserType()
            val newInserts = remoteUserTypes.map {
                User_type(
                    id = it.id.toString(),
                    displayname = it.displayName,
                    0L,
                    is_pending_update = 0L
                )
            }
            newInserts.forEach { type ->
                userRepository.insert(type)
            }
        }
        else{
            return// do nothing
        }
    }



    /**---------DEPARTMENTS AND SECTIONS-------------------
     * Must check network before making delete choice
     *1. Download departments from api
     *    deletes old  departments data  inserts
     *    downloaded departments
     *
     * 2. Download  sections from api
     *    deletes old sections ,
     *    inserts downloaded data
     *
     *---------------------------------------------------*/

     suspend fun  downLoadDepartmentsAndSections() {
         downloadAndInsertDepartments()
        downloadAndInsertSections()
     }

    private suspend fun  downloadAndInsertDepartments() {
        val downloaded = organizationApi.getAllDept()
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            if (sessionManager.userId == null)
                 throw IllegalStateException(NOT_LOGGED_IN_ERROR)
            val user = userApi.findById(UUID.fromString(sessionManager.userId))?:
            throw IllegalStateException(USER_FOUND_NOT_ERROR)
            println("department  User  id ==> ${sessionManager.userId}dept -${user.departmentId}  org - ${user.organizationId}")
           downloaded.forEach {it ->
               organizationRepository.insertDepartment(CreateDptDTO(
                   id = it.id,
                   displayName = it.displayName,
                   organizationId =user.organizationId,
               ))
           }
        }
    }

   private suspend fun downloadAndInsertSections(){
       println("Downloading sections called")
       var downloaded  = emptyList<CreateSectDTO>()
       try {
            downloaded = organizationApi.getAllSect()
       }catch (e:Exception){
           println("Download failed ${e.localizedMessage}")
       }
       println("Downloading sections called  000")
       if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
           if (sessionManager.userId == null)
               throw IllegalStateException(NOT_LOGGED_IN_ERROR)
           downloaded.forEach {section->
               println("Downloading sections called  111")
               organizationRepository.insertSection(section)
           }
       }
       }

    private fun isDataStale(): Boolean{
        val savedTimestamp = Instant.ofEpochMilli(
            settings.getLong(FRESH_DATA_KEY, defaultValue = 0L)
        ).toEpochMilli()

        val currentTimestamp = Instant.now().toEpochMilli()
        val difference =
            if(savedTimestamp > currentTimestamp)
                savedTimestamp- currentTimestamp
        else
            currentTimestamp - savedTimestamp
        return difference.hours >= 24.hours
    }
}