package org.montigny.client.util.stateClasses


sealed class ApiResponse {
    object Success : ApiResponse()                     // 200-level success without body
    data class Created(val id: String) : ApiResponse() // Optional: for POST that returns an ID
    data class Error(val code: Int, val message: String) : ApiResponse()
}
