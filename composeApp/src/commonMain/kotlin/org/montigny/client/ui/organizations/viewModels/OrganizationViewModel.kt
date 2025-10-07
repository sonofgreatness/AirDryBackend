package org.montigny.client.ui.organizations.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
import org.slf4j.Logger
import java.util.UUID

class OrganizationViewModel
    (): ViewModel(){

    private val _departments = MutableStateFlow<List<CreateOrgDTO>>(emptyList())
    val  departments: StateFlow<List<CreateOrgDTO>> = _departments

    private val _sections = MutableStateFlow<List<CreateSectDTO>>(emptyList())
    val  sections: StateFlow<List<CreateSectDTO>> = _sections

    private val _users = MutableStateFlow<List<UserResponse>>(emptyList())
    val users: StateFlow<List<UserResponse>> = _users
    private  val _organizations = MutableStateFlow<List<OrganizationResponse>>(emptyList())
    val  organizations: StateFlow<List<OrganizationResponse>> = _organizations

    private val  _isLoading =MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing
    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage


    fun  synchronizeOrganizations(){}
    fun deleteOrganization(orgToDelete: UUID) {

    }
    fun clearErrorMessage(){}

}