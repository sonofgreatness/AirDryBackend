package org.montigny.client.ui.settings.stacks.viewModel

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.montigny.client.auth.SessionManager
import org.montigny.client.data.api.repository.StackApi
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.util.ConnectivityObserver
import org.montigny.client.util.UtilityFunctions.Companion.toUUID
import org.montigny.client.util.ViewModel
import org.montigny.client.util.stateClasses.ApiResponse
import org.montigny.client.util.stateClasses.UiState
import org.slf4j.Logger
import java.util.UUID

class StacksViewModel(
    private  val stackApi: StackApi,
    private val sessionManager: SessionManager,
    private  val connectivityObserver: ConnectivityObserver,
    private  val logger : Logger,
    private val  stackRepository : StackRepository
): ViewModel()
{
    private  val _allStacks  = MutableStateFlow<List<GetStackDTO>>(emptyList())
    val allStacks : StateFlow<List<GetStackDTO>> = _allStacks

    private val  _allStackCodes = MutableStateFlow<List<CreateStackCodeDTO>>(emptyList())
    val allStackCodes : StateFlow<List<CreateStackCodeDTO>> = _allStackCodes

    private val _errorMessage  = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private  val _isSyncing  = MutableStateFlow(false)
    val  isSyncing: StateFlow<Boolean> = _isSyncing

    private val _createStackState =MutableStateFlow<UiState>(UiState.Idle)
    val  createStackState: StateFlow<UiState> = _createStackState

    private val _updateStackState =MutableStateFlow<UiState>(UiState.Idle)
    val  updateStackState: StateFlow<UiState> = _updateStackState

    private  val _updateStack = MutableStateFlow<GetStackDTO?>(null)
    val  updateStack: StateFlow<GetStackDTO?> = _updateStack

    private  val _isDeleting  = MutableStateFlow(false)
    val  isDeleting: StateFlow<Boolean> = _isDeleting

    private val  _createCodeDto =MutableStateFlow<CreateStackCodeDTO?>(null)
    val createCodeDto: StateFlow<CreateStackCodeDTO?> = _createCodeDto

    val isNetworkAvailable: StateFlow<Boolean> = connectivityObserver.isServerReachable
    val isOnlineLoggedIn : StateFlow<Boolean> =  sessionManager.isLoggedInFlow

    fun  populateAllStacksList(){
        populateStacks()
        populateStackCodes()
    }
    private fun populateStacks(){
        viewModelScope.launch {
            pickStacksDataSource(isNetworkAvailable.value, isOnlineLoggedIn.value,)
        }
    }
    private suspend fun  pickStacksDataSource(isNetworkAvailable: Boolean, isOnlineLoggedIn: Boolean){
        try {

            val  stacks  =if (isNetworkAvailable && isOnlineLoggedIn)
                stackApi.getAllStacks()
            else
                stackRepository.getAllStacks()
            _allStacks.value = stacks
        }catch (e: Exception){
            logger.error ("failed to populate stacksDataSource , Exception : ${e.message}",)
        }
    }
    private fun populateStackCodes(){
        viewModelScope.launch {
            pickStackCodesDataSource(isNetworkAvailable.value, isOnlineLoggedIn.value)
        }
    }
    private suspend fun  pickStackCodesDataSource(isNetworkAvailable: Boolean, isOnlineLoggedIn: Boolean){
        try {
            val stacks = if(isNetworkAvailable && isOnlineLoggedIn)
                stackApi.getAllStackCodes()
            else
                stackRepository.getAllStackCodes()
            _allStackCodes.value = stacks
        }catch (e: Exception){
            logger.error ("failed to populate stackCodesDataSource , Exception : ${e.message}",)
        }
    }
    fun createStack(displayName : String, length:Float?,
        height:Float?, thickness:Float?,
        numberOfPieces:Int?, displayCode: String,
        displayNameForStackCode:String
    ){
        viewModelScope.launch {
             val req = CreateStackCodeDTO(
            id = UUID.randomUUID(),
            displayName = displayNameForStackCode,
            displayCode = displayCode)
            val stackReq = CreateStackDTO(
                id =UUID.randomUUID(), length = length,
                height = height, thickness = thickness,
                numberOfPieces = numberOfPieces, displayName = displayName,
                stackCodeId = req.id,
            )
            _createStackState.value = UiState.Loading
            if (isNetworkAvailable.value && isOnlineLoggedIn.value){
                createStackCodeOnline(req)
                createStackOnline(stackReq)
                _createStackState.value = UiState.Success
            }else{
                createStackCodeOffline(req)
                createStackOffline(stackReq)
                _createStackState.value = UiState.Success

            }
        }
    }
    private suspend fun createStackCodeOnline(req: CreateStackCodeDTO) {
        try {
            println("Create stackCode online called")
            when (val resp = stackApi.createStackCode(req = req)) {
                is ApiResponse.Error -> {
                    logger.error(" failed to create stack code ${resp.message}")
                }
                else -> {
                    _createCodeDto.value = req
                }
            }
        }catch (e: Exception){
            logger.error(" failed to create stack code  xception ${e.message}")
        }
    }
    private suspend fun createStackOnline(req: CreateStackDTO) {
        try {
            println("Create stack online called")
            when (val resp = stackApi.createStack(req = req)) {
                is ApiResponse.Error -> {
                    logger.error("failed to create stack  ${resp.message}")
                }
                else -> {
                    _createStackState.value = UiState.Success
                }
            }
        }
        catch (e: Exception){
            logger.error ("failed to create stack Exception ${e.message}")
        }
    }
    private  suspend fun  createStackCodeOffline(req: CreateStackCodeDTO){
        try {
            println("Create stackCode offline called")
            stackRepository.createStackCode(req = req,true)
        }catch (e:Exception){
            logger.error ("failed to create  stack  code offline  ${e.message}")
        }
    }
    private suspend fun createStackOffline(req: CreateStackDTO){
        try {
            println("Create stack offline called")
            stackRepository.createStack(req = req,true)
        }catch (e:Exception){
            logger.error ("failed to create stack offline  ${e.message}")
        }
    }
    fun initializeStack(id : UUID){
        viewModelScope.launch {
            if (isNetworkAvailable.value && isOnlineLoggedIn.value){
                println("get stack online")
                val stack  = stackApi.getStackById(id)
                _updateStack.value=stack
            }
            else{
                println("get stack offline")
                val stack = stackRepository.getStackById(id)
                _updateStack.value=stack
            }
        }
    }
    fun resetUpdateState(){
        _updateStackState.value = UiState.Idle
    }
    fun updateStack(displayName : String, length:Float?,
                    height:Float?, thickness:Float?,
                    numberOfPieces:Int?, displayCode: String,
                    displayNameForStackCode:String
    ){
        viewModelScope.launch {
            val req = CreateStackCodeDTO(
                id = updateStack.value!!.stackCode.id,
                displayName = displayNameForStackCode,
                displayCode = displayCode)
            val stackReq = CreateStackDTO(
                id =updateStack.value!!.id, length = length,
                height = height, thickness = thickness,
                numberOfPieces = numberOfPieces, displayName = displayName,
                stackCodeId = req.id,
            )
            _updateStackState.value = UiState.Loading
            if (isNetworkAvailable.value && isOnlineLoggedIn.value){
                updateStackCodeOnline(req)
                updateStackOnline(stackReq)
                _updateStackState.value = UiState.Success
            }else{
                updateStackCodeOffline(req)
                updateStackOffline(stackReq)
                _updateStackState.value = UiState.Success

            }
        }
    }
    private suspend fun updateStackCodeOnline(req: CreateStackCodeDTO){
        try {
            when (val resp = stackApi.updateStackCode(req = req)) {
                is ApiResponse.Error -> {
                    logger.error("failed to update stack code ${resp.message}")
                }
                else -> {
                    _updateStackState.value = UiState.Loading
                }
            }
            println("Update stack code")
        }catch (e:Exception){
            logger.error ("failed to update stack code ${e.message}")
        }
    }
    private suspend fun updateStackOnline(req: CreateStackDTO){
        try {
            when (val resp = stackApi.updateStack(req = req)) {
                is ApiResponse.Error -> {
                    logger.error("failed to update stack ${resp.message}")
                }
                else ->{
                    _updateStackState.value = UiState.Success
                }
            }
        }catch (e:Exception){
            logger.error ("failed to update stack  ${e.message}")
        }
    }
    private suspend fun updateStackCodeOffline(req: CreateStackCodeDTO){
        try {
            stackRepository.updateStackCode(req)
        }catch (e:Exception){
            logger.error ("failed to update stack code[Offline] ${e.message}")
        }
    }
    private suspend fun updateStackOffline(stackReq: CreateStackDTO){
        try {
            stackRepository.updateStack(stackReq)
        }catch (e:Exception){
            logger.error ("failed to update stack  [Offline] ${e.message}")
        }
    }
    fun  deleteStack(id : String){
        viewModelScope.launch {
             //CASCADING DELETE
            stackApi.deleteStackCodeById(id.toUUID())
        }
    }
}