package org.montigny.client.domain.synchronization

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.StateFlow
import org.com.client.database.User_type
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.Constants.Companion.FRESH_DATA_KEY
import org.montigny.client.util.Constants.Companion.NOT_LOGGED_IN_ERROR
import org.montigny.client.util.Constants.Companion.USER_FOUND_NOT_ERROR
import org.montigny.client.util.UtilityFunctions.Companion.toStringCustom
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
                  private val offlineSessionManager: OfflineSessionManager,
                  private val stackRepository: StackRepository,
                  private val stackApi: StackApi)
{
    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
     val isOnlineLoggedInd: StateFlow<Boolean> = sessionManager.isLoggedInFlow
    suspend fun synchronizeAllAppData(){
        deleteUncachedRecords()
        uploadCachedInserts()
        pushCachedUpdates()
        downloadAndInsertAllRecords()
    }

    private suspend fun downloadAndInsertAllRecords() {
        downloadAndInsertUsers()
        downloadAndInsertUserTypes()
        downloadAndInsertOrganizationsInternal()
        downloadAndInsertStackCodes()
        downloadAndInsertStacks()

    }

    private suspend fun pushCachedUpdates() {
        pushCachedUserUpdates()
        pushUpdatedDepartments()
        pushUpdatedSections()
        pushCachedOrganizationUpdates()
        pushUpdatedStackCodes()
        pushUpdatedStacks()
    }
    private  suspend fun pushUpdatedDepartments (){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val departments =  organizationRepository.selectCachedForUpdateDepartment()
                departments.forEach {
                    organizationApi.updateDepartment(it)
                    organizationRepository.unCacheForUpdateDepartment(it.id.toString())
                }
            }catch (e:Exception){
                println("push department updates failed ${e.localizedMessage}")
            }
        }

    }
    private  suspend fun pushUpdatedSections(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val cachedUpdates  = organizationRepository.selectCachedForUpdateSect()
                cachedUpdates.forEach {
                    organizationApi.updateSection(it)
                    organizationRepository.unCacheForUpdateSection(it.id.toString())
                }
            }catch (e:Exception){
                println("Push updated sections failed ${e.localizedMessage}")
            }
        }
    }
    private suspend fun pushCachedUserUpdates(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val cachedUsers = userRepository.selectCachedForUpdateUsers()
                cachedUsers.forEach {
                    userApi.updateUser(user =it)
                    userRepository.unCacheUserForUpdate(it.id.toStringCustom())
                }
            }catch (e:Exception){
                println("push update cached users  failed ${e.localizedMessage}")
                throw e
            }
        }
    }
    private suspend fun pushUpdatedStackCodes(){
        try {
            if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
                val stackCodes = stackRepository.getStackCodesCachedForUpdate()
                stackCodes.forEach {
                    stackApi.updateStackCode(it)
                    stackRepository.unCacheStackCodeUpdate(it.id!!)
                }
            }
        }catch (e:Exception){
            println("push update stack codes failed ${e.localizedMessage}")
        }
    }
    private suspend fun pushUpdatedStacks(){
      try {
          if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
              val  stacks = stackRepository.getStacksCachedForUpdate()
              stacks.forEach {
                  stackApi.updateStack(it)
                  stackRepository.unCacheStackUpdate(it.id!!)
              }
          }
      }catch (e:Exception){
          println("push update stacks failed ${e.localizedMessage}")
      }
    }
    private suspend fun uploadCachedInserts() {
        uploadCachedUsers()
        uploadCachedDepartmentInserts()
        uploadCachedSectionInserts()
        uploadCachedStackCodesInserts()
        uploadCachedStackInserts()
    }

    private suspend fun uploadCachedForInsertOrganizations(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val cachedOrgs = organizationRepository.selectCachedForInsertOrganization()
                cachedOrgs.forEach {
                    val req = it.genererateCreateOrgDto()
                    val logo = it .logo
                    organizationApi.createOrg(req,logo)
                    organizationRepository.unCacheForInsertOrganization(it.id.toString())
                }
            }catch (e:Exception){
                println("Upload  organizations failed ${e.localizedMessage}")
            }
        }
    }
    private suspend fun  uploadCachedSectionInserts(){
        try {
            val  cachedSections  =  organizationRepository.selectCachedForInsertSection()
            cachedSections.forEach {
                organizationApi.createSection(it)
                organizationRepository.unCacheForInsertSection(it.id.toString())
            }
        }catch (e:Exception){
            println("Upload  sections failed ${e.localizedMessage}")
        }
    }
    private  suspend fun  uploadCachedDepartmentInserts(){
        try {
            val  cachedDepartments = organizationRepository.selectCachedForInsertDepartment()
            cachedDepartments.forEach {
                organizationApi.createDepartment(it)
                organizationRepository.unCacheForInsertDepartment(it.id.toString())
            }
        }
        catch (e :Exception){
            println("Upload  departments failed ${e.localizedMessage}")
        }
    }
    private suspend fun uploadCachedStackInserts(){
        try {
            val cachedStacks = stackRepository.getStacksCachedForInsert()
            cachedStacks.forEach {
                stackApi.createStack(it)
                stackRepository.unCacheStackInsert(it.id!!)
            }
        }
        catch (e:Exception){
            println("Upload  stacks failed ${e.localizedMessage}")
        }
    }
    private suspend  fun uploadCachedStackCodesInserts(){
        try {
            val cachedStackCodes = stackRepository.getStackCodesCachedForInsert()
            cachedStackCodes.forEach {
                stackApi.createStackCode(it)
                stackRepository.unCacheStackCodeInsert(it.id!!)
            }
        }
        catch (e:Exception){
            println("Upload  stack codes failed ${e.localizedMessage}")
        }
    }
    private  suspend fun uploadCachedUsers(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {

            try {
                val cachedUsers = userRepository.selectCachedForInsertUsers()
                cachedUsers.forEach {
                    userApi.createUser(it)
                    userRepository.unCacheUserForInsert(it.id.toStringCustom())
                }
            }catch (e:Exception){
                println("Upload  cached users  failed ${e.localizedMessage}")
                throw e
            }
        }
    }

    private suspend fun deleteUncachedRecords() {
        organizationRepository.deleteUnCachedDepartments()
        organizationRepository.deleteUnCachedSections()
        userRepository.deleteUnCachedUsers()
        deleteUnCachedOrganizations()
        stackRepository.deleteUnCachedStackCodes()
        stackRepository.deleteUnCachedStacks()
    }

   private suspend fun  downloadAndInsertUsers(){
        try {
            val remoteUsers: List<UserRegisterRequest> = userApi.getUsersUnPaged()
            val localUsers = userRepository.selectAllUsers()
           if(isNetworkAvailable.value) {// do more checks
               if (!localUsers.isNullOrEmpty())
                   userRepository.deleteUnCachedUsers()
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
            organizationRepository.deleteUnCachedDepartments()
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

       var downloaded  = emptyList<CreateSectDTO>()
       try {
            downloaded = organizationApi.getAllSect()
       }catch (e:Exception){
           println("Download failed ${e.localizedMessage}")
       }
       if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
           if (sessionManager.userId == null)
               throw IllegalStateException(NOT_LOGGED_IN_ERROR)
           organizationRepository.deleteUnCachedSections()
           downloaded.forEach {section->
               organizationRepository.insertSection(section)
           }
       }
   }
   suspend  fun downloadAndInsertOrganizations(){
         if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
          deleteUnCachedOrganizations()
          pushCachedOrganizationUpdates()
          uploadCachedForInsertOrganizations()
          deleteUnCachedOrganizations()
         downloadAndInsertOrganizationsInternal()
         }
    }
   private  suspend fun pushCachedOrganizationUpdates(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val cachedOrgs = organizationRepository.selectCachedForUpdateOrganization()
                cachedOrgs.forEach {
                    val req = it.genererateCreateOrgDto()
                    val logo = it .logo
                    organizationApi.updateOrg(req,logo)
                    organizationRepository.unCacheForUpdateOrganization(it.id.toString())
                }
            }catch (e:Exception){
                println("Upload  organizations failed ${e.localizedMessage}")
            }
        }
    }

   private  suspend fun downloadAndInsertOrganizationsInternal() {
        println("inserting organizations")
        if (isNetworkAvailable.value && isOnlineLoggedInd.value) {
            val nestedOrg = organizationApi.getOrganizationWithNestedDepartments()
            nestedOrg.forEach {
                organizationRepository.insertOrganization(
                    GetOrgDTO(
                        id = it.id,
                        displayName = it.name, logo = it.logo))
            }
        }
        println("inserting organizations complete ")
    }
   private suspend fun  deleteUnCachedOrganizations(){
        try{
            organizationRepository.deleteUncachedOrganizations()
        }catch(e:Exception){
            println("Delete un cached organizations failed ${e.localizedMessage}")
        }
    }
   private suspend fun downloadAndInsertStacks(){
       if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
           try {
              val stacks =  stackApi.getAllStacks()
               stacks.forEach {
                   stackRepository.createStack(it.generateCreateStackDTO())
               }
           }catch (e:Exception){
               println("Download stacks failed ${e.localizedMessage}")
           }
       }
   }
   private suspend fun downloadAndInsertStackCodes(){
        if(isNetworkAvailable.value && isOnlineLoggedInd.value) {
            try {
                val stackCodes =  stackApi.getAllStackCodes()
                stackCodes.forEach {
                    stackRepository.createStackCode(it)
                }
            }catch (e:Exception){
                println("Download stacks failed ${e.localizedMessage}")
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