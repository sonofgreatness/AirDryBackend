package org.montigny.client.service

import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.persistence.repository.StackRepository
import java.util.UUID

class StackService (val stackRepo: StackRepository){
    fun createStackCode(currentUser: CurrentUserDTO, req: CreateStackCodeDTO): UUID {
        return stackRepo.createStackCode(currentUser, req)
    }
    fun  createStack(currentUser: CurrentUserDTO, req: CreateStackDTO): UUID {
        return stackRepo.createStack(currentUser, req)
    }
    fun updateStackCode(currentUser: CurrentUserDTO, req: CreateStackCodeDTO) =stackRepo.updateStackCode(currentUser, req)
    fun  updateStack(currentUser: CurrentUserDTO, req: CreateStackDTO) =stackRepo.updateStack(currentUser, req)

    fun getAllStacks(currentUser: CurrentUserDTO):List<GetStackDTO>{
        return  emptyList<GetStackDTO>()
    }
    fun deleteStackById(stackId: UUID): Int = stackRepo.deleteStackById(stackId)
    fun deleteStackCodeById(stackCodeId: UUID): Int = stackRepo.deleteStackCodeById(stackCodeId)

    fun getStackById(stackId: UUID): GetStackDTO? = stackRepo.getStackById(stackId)
    fun  getStackCodeByID(stackCodeId: UUID): CreateStackCodeDTO? = stackRepo.getStackCodeByID(stackCodeId)

    fun  getAllStackCodes(): List<CreateStackCodeDTO>  =stackRepo.getAllStackCodes()
    fun  getAllStacks():List<GetStackDTO>  =stackRepo.getAllStacks()
}