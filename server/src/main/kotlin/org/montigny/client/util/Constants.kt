package org.montigny.client.util

class Constants {
    companion object{
        const val GET_USERS_DESCRIPTION = "Get all users belonging to the organization"
        const val GET_USERS_DESCRIPTION_TYPE = "Get all user types"
        const val  ERROR_PRINCIPAL = "Auth Error : Missing or malformed  principal"
        const val ERROR_EXCEPTION = "Exception occurred"
        const val DEPARTMENTS_VALIDATION_ERROR ="Invalid department"
        const val ORGANIZATION_MISMATCH_VALIDATION_ERROR ="Organization Mismatch"
        const val SECTION_VALIDATION_ERROR = "Invalid section"
        const val USER_TYPE_VALIDATION_ERROR = "Invalid user type"
        const  val  BEARER_AUTH_TAG = "BearerAuth"
        const val  UPDATE_USER_DESCRIPTION = "Update user details"
        const val UPDATED_USER_SUCCESS_DESCRIPTION = "Updated user successfully"
        const val UPDATED_SUCCESS_DESCRIPTION = "Updated  successfully"
        const val  DELETED_SUCCESS_DESCRIPTION = "Deleted successfully"
        const val CREATED_ID_DESCRIPTION = "Returns the ID of the newly created record"
        const val CREATED_SUCCESS_DESCRIPTION = "Updated  successfully"
        const val REGISTER_ORGANIZATION_DESCRIPTION = "Creates a new organization in the system, including optional logo upload."
        const val UPDATE_ORGANIZATION_DESCRIPTION = "Update a new organization in the system, including optional logo upload."
        const val ORGANIZATION_ID = "organizationId"
        const val DEPARTMENT_ID = "departmentId"
        const val  SECTION_ID = "sectionId"
        const val  USER_TYPE_ID = "userTypeId"
        const val GET_ORGS_DESCRIPTION_TYPE = "Get all organizations"
        const val GET_DPTS_DESCRIPTION_TYPE = "Get all departments"
        const val GET_SECTS_DESCRIPTION_TYPE = "Get all sections"


    }
}