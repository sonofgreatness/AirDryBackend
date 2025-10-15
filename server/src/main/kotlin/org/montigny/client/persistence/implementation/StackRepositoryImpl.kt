package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.dto.User.CurrentUserDTO
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.dto.Work.StackEntityResponse
import org.montigny.client.persistence.StackCodes
import org.montigny.client.persistence.Stacks
import org.montigny.client.persistence.repository.StackRepository
import org.montigny.client.util.Constants.Companion.STACK_CODE_NOT_FOUND
import org.montigny.client.util.Constants.Companion.STACK_NOT_FOUND
import java.util.UUID

class StackRepositoryImpl() : StackRepository {
    override fun createStackCode(currentUser: CurrentUserDTO, req: CreateStackCodeDTO): UUID = transaction{
        StackCodes.insertAndGetId {
            it[id] = req.id!!
            it[createdBy] =currentUser.id
            it[displayName] = req.displayName!!
            it[displayCode] = req.displayCode!!
        }.value
    }
    override  fun  createStack(currentUser: CurrentUserDTO, req: CreateStackDTO): UUID = transaction{
        Stacks.insertAndGetId {
          it[id] = req.id!!
          it[createdBy] = currentUser.id
          it[displayName] = req.displayName!!
          it[length]  = req.length!!
          it[height] = req.height!!
          it[thickness] = req.thickness!!
          it[stackCodeId] = req.stackCodeId!!
          it[numberOfPieces] = req.numberOfPieces!!
        }.value
    }

    override fun updateStackCode(
        currentUser: CurrentUserDTO,
        req: CreateStackCodeDTO
    )=transaction {
        StackCodes.select { StackCodes.id eq req.id!! }.firstOrNull()?:
         throw IllegalArgumentException(STACK_CODE_NOT_FOUND)

        StackCodes.update({ StackCodes.id eq req.id!! }) {
            it[updatedBy] = currentUser.id
            req.displayCode?.let{dis ->it[displayCode] = dis }
            req.displayName?.let{dis ->it[displayName] = dis }
        }
    }

    override fun updateStack(
        currentUser: CurrentUserDTO,
        req: CreateStackDTO
    )  = transaction{
        Stacks.select { Stacks.id eq req.id!! }.firstOrNull()?:
        throw IllegalArgumentException(STACK_NOT_FOUND)

        Stacks.update({ Stacks.id eq req.id!! }) {
            it[updatedBy] = currentUser.id
            req.displayName?.let{dis ->it[displayName] = dis }
            req.stackCodeId?.let{dis ->it[stackCodeId] = dis }
            req.numberOfPieces?.let{dis ->it[numberOfPieces] = dis }
            req.height?.let{dis ->it[height] = dis }
            req.thickness?.let{dis ->it[thickness] = dis }
            req.length?.let{dis ->it[length] = dis }
        }
    }

   override fun deleteStackById(stackId: UUID): Int =transaction{
       Stacks.select { Stacks.id eq stackId }.firstOrNull()?:
       throw IllegalArgumentException(STACK_NOT_FOUND)
       Stacks.deleteWhere { Stacks.id eq stackId }
   }
    override fun  deleteStackCodeById(stackCodeId: UUID): Int =transaction{
        StackCodes.select { StackCodes.id eq stackCodeId }.firstOrNull()?:
        throw IllegalArgumentException(STACK_CODE_NOT_FOUND)

        StackCodes.deleteWhere { StackCodes.id eq stackCodeId }
    }

  override  fun getStackById(stackId: UUID): GetStackDTO? = transaction {
      // Fetch the stack record (and its related StackCode)
      val stackRow = Stacks
          .innerJoin(StackCodes)
          .select { Stacks.id eq stackId }
          .singleOrNull() ?: return@transaction null
      // Build and return the DTO
      GetStackDTO(
          id = stackRow[Stacks.id].value,
          length = stackRow[Stacks.length],
          height = stackRow[Stacks.height],
          thickness = stackRow[Stacks.thickness],
          displayName = stackRow[Stacks.displayName],
          numberOfPieces = stackRow[Stacks.numberOfPieces],
          volume = stackRow[Stacks.volume],
          stackCode = StackEntityResponse(
              id = stackRow[StackCodes.id].value,
              displayCode = stackRow[StackCodes.displayCode],
              displayName = stackRow[StackCodes.displayName]
          )
      )
  }
  override fun  getStackCodeByID(stackCodeId: UUID): CreateStackCodeDTO?=transaction{

        val row = StackCodes.select { StackCodes.id eq stackCodeId }.singleOrNull()?:return@transaction null
        CreateStackCodeDTO(
          id= row[StackCodes.id].value,
          displayName = row[StackCodes.displayName],
          displayCode = row[StackCodes.displayCode]
        )

    }
  override  fun  getAllStackCodes(): List<CreateStackCodeDTO> =transaction{
      StackCodes.selectAll().map{
          CreateStackCodeDTO(
              id = it[StackCodes.id].value,
              displayName = it[StackCodes.displayName],
              displayCode = it[StackCodes.displayCode])
      }

  }
  override  fun  getAllStacks():List<GetStackDTO> =transaction {
      Stacks.innerJoin(
          StackCodes
      ).selectAll().map{
          GetStackDTO(
              id = it[Stacks.id].value,
              length = it[Stacks.length],
              height = it[Stacks.height],
              thickness = it[Stacks.thickness],
              displayName = it[Stacks.displayName],
              numberOfPieces = it[Stacks.numberOfPieces],
              volume = it[Stacks.volume],
              stackCode = StackEntityResponse(
                  id = it[StackCodes.id].value,
                  displayCode = it[StackCodes.displayCode],
                  displayName = it[StackCodes.displayName]),
          )
      }
  }
}
