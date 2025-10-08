package org.montigny.client.ui.organizations.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.OfflineSessionManager
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.OrganizationApi
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.OrganizationRepository
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.domain.synchronization.SyncManager
import org.montigny.client.dto.Organization.CreateOrgDTO
import org.montigny.client.dto.Organization.CreateSectDTO
import org.montigny.client.dto.Organization.GetOrgDTO
import org.montigny.client.dto.Organization.OrganizationResponse
import org.montigny.client.dto.User.UserResponse
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.montigny.client.util.stateClasses.OrgDTOState
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger
import java.util.UUID

class OrganizationViewModel
    (private val organizationRepository: OrganizationRepository,
     private val organizationApi: OrganizationApi,
     private  val syncManager: SyncManager,
     private val logger: Logger,
     private val sessionManager: SessionManager,
     private val offlineSessionManager: OfflineSessionManager,
     private val connectivityObserver: ConnectivityObserver,
            ): ViewModel(){
    private  val _organizations = MutableStateFlow<List<OrganizationResponse>>(emptyList())
    val  organizations: StateFlow<List<OrganizationResponse>> = _organizations
    private  val _createdOrganization = MutableStateFlow(GetOrgDTO(null))
    val  createdOrganization: StateFlow<GetOrgDTO> = _createdOrganization

    private val _createOrganizationState = MutableStateFlow<UiState> (UiState.Idle)
    val createOrganizationState: StateFlow<UiState> = _createOrganizationState


    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow


    fun  synchronizeOrganizations(){
        viewModelScope.launch {
            try {
                synchronizeOrganizationsInternal(isNetworkAvailable.value, isOnlineLoggedIn.value)
            }
            catch (e: Exception) {
                logger.error("failed to synchronize organizations", e)
            }
        }
    }
    private suspend fun  synchronizeOrganizationsInternal(isNetworkAvailable:Boolean, isOnlineLoggedIn:Boolean){
        if(isNetworkAvailable && isOnlineLoggedIn){
            logger.info("calling synchronizeOrganizations")
            syncManager.downloadAndInsertOrganizations()
        }
        populateOrganizations(isNetworkAvailable, isOnlineLoggedIn)
    }
    private suspend fun populateOrganizations(isNetworkAvailable:Boolean,isOnlineLoggedIn:Boolean){
        pickOrganizationsDataSource(isNetworkAvailable, isOnlineLoggedIn)
    }
    private suspend fun pickOrganizationsDataSource(isNetworkAvailable:Boolean,isOnlineLoggedIn:Boolean){
        if(isNetworkAvailable && isOnlineLoggedIn){
            try {

                val nestedOrg = organizationApi.getOrganizationWithNestedDepartments()
                 _organizations.value = nestedOrg
            }catch (e:Exception){
                logger.error("failed load organizations from server ${e.message}")
            }
        }
        else{
            try {
                val userId = offlineSessionManager.userId.toUUIDOrNull() ?: throw Exception("User ID is invalid (null)")
                val nestedOrg = organizationRepository.selectOrganizationHierarchyByUserId(userId)
               logger.info("no twerk userId ${userId}")
                nestedOrg.let{
                    _organizations.value = it
                }
            }catch (e:Exception){
                logger.error("failed load organizations from local db ${e.message}")
            }
        }
    }
    fun onCreateOrgChangeListener(newValue :String,state: OrgDTOState){
         when(state){
             is OrgDTOState.DisplayName -> {
                 _createdOrganization.value = _createdOrganization.value.copy(displayName = newValue)
             }
             is OrgDTOState.Logo -> {
                 _createdOrganization.value = _createdOrganization.value.copy(logo = state.placeholderLogo)
             }
         }
    }
    fun  createOrganization(){
      viewModelScope.launch {
        try {
            _createOrganizationState.value = UiState.Loading
            val req = CreateOrgDTO(
                id = UUID.randomUUID(),
                displayName = _createdOrganization.value.displayName.toString()
            )
            val logo = _createdOrganization.value.logo
            if(isNetworkAvailable.value && isOnlineLoggedIn.value){
                organizationApi.createOrg(req,logo)
            }else{
                organizationRepository.insertOrganization(_createdOrganization.value.copy(id= UUID.randomUUID()), true)
            }
            logger.info("successfully created organization")
            _createOrganizationState.value = UiState.Success
        }
        catch (e:Exception){
            _createOrganizationState.value = UiState.Error("failed to create organization ${e.message}")
            logger.error("failed to create organization ${e.message}")
        }
    }
    }
    fun deleteOrganization(orgToDelete: UUID) {

    }
    fun clearErrorMessage(){}

}