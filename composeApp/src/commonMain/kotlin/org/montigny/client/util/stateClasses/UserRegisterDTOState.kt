package org.montigny.client.util.stateClasses

sealed  class UserRegisterDTOState {
    object name : UserRegisterDTOState()
    object phoneNumber : UserRegisterDTOState()
    object surname :UserRegisterDTOState()
    object secondaryPhoneNumber:UserRegisterDTOState()
    object userTypeId:UserRegisterDTOState()
    object organizationId:UserRegisterDTOState()
    object departmentId:UserRegisterDTOState()
    object sectionId:UserRegisterDTOState()
    object email:UserRegisterDTOState()
    object password:UserRegisterDTOState()
    data class isActive(val value: Boolean):UserRegisterDTOState()
    data class  isApproved(val value: Boolean):UserRegisterDTOState()

    
    
}