package org.montigny.client.persistence.repository

import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import java.util.*

interface StackRepository {

    fun createStackCode(currentUser: CurrentUserDTO, req: CreateStackCodeDTO): UUID
    fun  createStack(currentUser: CurrentUserDTO, req: CreateStackDTO): UUID

    fun updateStackCode(currentUser: CurrentUserDTO, req: CreateStackCodeDTO):Int
    fun  updateStack(currentUser: CurrentUserDTO, req: CreateStackDTO):Int

    fun deleteStackById(stackId: UUID): Int
    fun deleteStackCodeById(stackCodeId: UUID): Int

    fun getStackById(stackId: UUID): GetStackDTO?
    fun  getStackCodeByID(stackCodeId: UUID): CreateStackCodeDTO?

    fun  getAllStackCodes(): List<CreateStackCodeDTO>
    fun  getAllStacks():List<GetStackDTO>
}