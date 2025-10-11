package org.montigny.client.ui.organizations.viewModels

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.dto.Organization.CreateDptDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.DepartmentResponse
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger
import java.util.UUID

class OrganizationDepartmentsAndSectionsViewModel(
    private val organizationRepository: OrganizationRepository,
    private val organizationApi: OrganizationApi,
    private  val syncManager: SyncManager,
    private val logger: Logger,
    private val sessionManager: SessionManager,
    private val offlineSessionManager: OfflineSessionManager,
    private val connectivityObserver: ConnectivityObserver,

    )
    : ViewModel() {
    private val  _loadOrganization = MutableStateFlow<OrganizationResponse?>(null)
    val  loadOrganization: StateFlow<OrganizationResponse?> = _loadOrganization

    private  val _creatDepartmentState = MutableStateFlow<UiState>(UiState.Idle)
    val  creatDepartmentState: StateFlow<UiState> = _creatDepartmentState


    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow

    fun initializeLoadOrganization(userId:String) {
        viewModelScope.launch {
            println("intialize org  for user $userId")
            try {
                if (isNetworkAvailable.value && isOnlineLoggedIn.value) {
                    val organization = organizationApi.getOrganizationWithNestedDepartments()
                    _loadOrganization.value = organization[0]
                } else {
                    val organization = organizationRepository.selectOrganizationHierarchyByUserId(userId.toUUID())
                    _loadOrganization.value = organization[0]
                }
            }catch (e: Exception) {
                logger.error("Failed to load update user", e)
            } }
    }
    fun createDepartment(departmentName:String) {
        viewModelScope.launch {
            try {
                val  dto  = CreateDptDTO(
                    id = UUID.randomUUID(),displayName = departmentName,
                    organizationId =  _loadOrganization.value!!.id)
                if(isNetworkAvailable.value && isOnlineLoggedIn.value) {
                    val resp = organizationApi.createDepartment(dto)
                    when(resp){
                        is ApiResponse.Error -> {
                            println("Response error ${resp.code}, response: ${resp.message}")
                        }
                        is ApiResponse.Success -> {
                            println("Create Department Response Ok")
                        }
                        is ApiResponse.Created -> {
                            println("Create Department Response Ok")
                        }
                    }
                }
                else
                {
                    logger.info("create department  offline dto $dto")
                    organizationRepository.insertDepartment(dto, true)
                }
            }
            catch (e: Exception) {
                _creatDepartmentState.value = UiState.Error(e.localizedMessage)
                logger.error("Failed to create department $departmentName", e)
            }
        }
    }
    fun editDepartment (newName: String, departmentId: UUID?){
        if (departmentId== null)
            throw Exception("department id is null")
        val dto  = CreateDptDTO(id = departmentId, displayName = newName,
            organizationId = _loadOrganization.value!!.id)
        println("calling editDepartment  with $dto")
        editDepartmentInternal(dto)
        println("edit department completed")
    }
    private fun  editDepartmentInternal(department: CreateDptDTO) {
     viewModelScope.launch{
         try{
             if (isNetworkAvailable.value && isOnlineLoggedIn.value) {
                 when(val resp = organizationApi.updateDepartment(department)){
                     is ApiResponse.Error -> {
                         println("Response error ${resp.code}, response: ${resp.message}")
                     }
                     is ApiResponse.Success -> {
                         println("update Department Response Ok")
                     }
                     is ApiResponse.Created -> {
                         println("update Department Response Ok")
                     }
                 }
             }
             else{
                 organizationRepository.updateDepartment(department)
             }
         }
         catch (e: Exception){
             logger.error("Failed to update department ${e.message}")
         }
         }
    }
    fun createSection(sectionName:String, departmentId: UUID?) {
        if (departmentId == null)
            throw Exception("department id is null")
        viewModelScope.launch {
            val dto  = CreateSectDTO(
                id = UUID.randomUUID(),displayName = sectionName,
                departmentId = departmentId)
            println("calling createSection with $dto")
            if(isNetworkAvailable.value && isOnlineLoggedIn.value) {
                when(val resp =organizationApi.createSection(dto)){
                    is ApiResponse.Error -> {
                        println("Create Section Response failed ${resp.code},response: ${resp.message}")
                    }
                    is ApiResponse.Success -> {println("Create Section Response Ok") }
                    is ApiResponse.Created -> {println("Create Section Response Ok")}
                }
            }
            else{
                organizationRepository.insertSection(dto, true)
            }
        }
    }
    fun editSection(newName: String, sectionId: UUID?, departmentId: UUID?){

        viewModelScope.launch {
            if (sectionId == null)
                throw Exception("section id is null")
            if (departmentId == null)
                throw Exception("department id is null")
            val dto = CreateSectDTO(
                id = sectionId, displayName = newName,
                departmentId = departmentId)
            try {
                println("calling editSection with $dto")
                if (isNetworkAvailable.value && isOnlineLoggedIn.value) {
                    when (val resp = organizationApi.updateSection(dto)) {
                        is ApiResponse.Error -> {
                            println("Update Section Response failed ${resp.code},response: ${resp.message}")
                        }
                        is ApiResponse.Success -> {println("Update Section Response Ok")}
                        is ApiResponse.Created -> {println("Update Section Response Ok")}
                    }

                }else{
                    organizationRepository.updateSection(dto) }
            }catch (e: Exception){
                logger.error("Failed to update section ${e.message}")
            }

        }
    }
}