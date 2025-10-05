package org.montigny.client.util

import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.User.UpdateUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import java.util.UUID

class ApiExamples {

    companion object {

        val exampleUser = UserRegisterRequest(
            "Solo",
            "Dube",
            phoneNumber = "76900009",
            secondaryPhoneNumber = "7998909",
            userTypeId = UUID.randomUUID(),
            organizationId = UUID.randomUUID(),
            sectionId = UUID.randomUUID(),
            departmentId = UUID.randomUUID(),
            email = "solo@email.com",
            password = "password",
            isActive = false,
            isApproved = false,
        )


        val exampleLogin = AuthRequest(
            username = "simphiweiq@gmail.com",
            password = "myverystrongpassword",
        )

        val exampleUdateUser = UpdateUserDTO(
            updated_userTypeId = null,
            updated_name = "me_to_you",
            isApproved = true,

            updated_email = null,
            updated_phoneNumber = null,
            updated_surname = null,
            updated_password = null,
            updated_sectionId = null,
            updated_departmentId = null,

            )
    }
}