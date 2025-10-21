package org.montigny.client.util

import org.montigny.client.dto.Auth.AuthRequest
import org.montigny.client.dto.User.UpdateUserDTO
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.CreateWorkDoneDTO
import org.montigny.client.dto.WorkDataFiltersDTO

import java.time.Instant
import java.util.Date
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
        val  exampleCreateStackDTO  = CreateStackDTO(
            length = 2.5f,
            height = 1.2f,
            thickness = 0.5f,
            numberOfPieces = 2,
            displayName = "Stack A",
            stackCodeId = UUID.randomUUID()

        )
        val exampleCreateStackCodeDTO = CreateStackCodeDTO(displayCode = "SC-001",
            displayName = "Stack Code 1")

        val  exampleCreateWorkDoneDTO = CreateWorkDoneDTO(
            id = UUID.randomUUID(),
            workDate = Date.from(Instant.now()),
            workersList = listOf(
                CreateWorkDoneDTO.WorkDoneUsersDTO(
                    id = UUID.randomUUID(),
                    listOf(
                        UUID.randomUUID()
                    )
                )
            ),
            stacksList = listOf(
                CreateWorkDoneDTO.WorkDoneStacksDTO(
                    id = UUID.randomUUID(),
                    stackIds = listOf(UUID.randomUUID())
                )
            ),
        )
        val exampleFilters  = WorkDataFiltersDTO()

    }

}