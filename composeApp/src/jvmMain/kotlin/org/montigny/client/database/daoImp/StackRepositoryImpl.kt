package org.montigny.client.database.daoImp

import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.StackRepository
import org.montigny.client.dto.Work.CreateStackCodeDTO
import org.montigny.client.dto.Work.CreateStackDTO
import org.montigny.client.dto.Work.GetStackDTO
import org.montigny.client.dto.Work.StackEntityResponse
import org.montigny.client.util.UtilityFunctions.Companion.convertBoolToLong
import org.montigny.client.util.UtilityFunctions.Companion.convertLongToBool
import org.montigny.client.util.UtilityFunctions.Companion.toUUIDOrNull
import org.slf4j.Logger
import java.util.UUID

class StackRepositoryImpl
    (    private val db: AppDatabase,
         private val logger : Logger
): StackRepository {
    private val queries = db.appDatabaseQueries
    override suspend fun createStackCode(req: CreateStackCodeDTO, isCached: Boolean) {
        try {
            queries.transaction {
                queries.insertStackCode(
                    id = req.id.toString(),
                    display_code = req.displayCode!!,
                    display_name = req.displayName!!,
                    is_pending_insert = convertBoolToLong(isCached),
                    is_pending_update = 0L
                )
            }
        } catch (e: Exception) {
            logger.error("FAILED TO CREATE Stack Code", e)
        }
    }

    override suspend fun createStack(req: CreateStackDTO, isCached: Boolean) {
        try {
            val volume = (req.length!! * req.height!! * req.thickness!! * req.numberOfPieces!!)
            queries.transaction {
                queries.insertStack(
                    id = req.id.toString(),
                    display_name = req.displayName!!,
                    length = req.length!!.toDouble(),
                    height = req.height!!.toDouble(),
                    thickness = req.thickness!!.toDouble(),
                    number_of_pieces = req.numberOfPieces!!.toLong(),
                    stack_code_id = req.stackCodeId.toString(),
                    volume = volume.toDouble(),
                    is_pending_insert = convertBoolToLong(isCached),
                    is_pending_update = 0L
                )
            }
        } catch (e: Exception) {
            logger.error("FAILED TO CREATE Stack", e)
        }
    }


    override suspend fun getAllStackCodes(): List<CreateStackCodeDTO> {
        return queries.selectAllStackCodes().executeAsList().map {
            CreateStackCodeDTO(
                id = it.id.toUUIDOrNull(),
                displayName = it.display_name,
                displayCode = it.display_code,
            )
        }
    }

    override suspend fun getAllStacks(): List<GetStackDTO> {
        return queries.selectAllStacks().executeAsList().map {
            GetStackDTO(
                id = it.id.toUUIDOrNull(),
                length = it.length.toFloat(),
                height = it.height.toFloat(),
                thickness = it.thickness.toFloat(),
                displayName = it.display_name,
                numberOfPieces = it.number_of_pieces.toInt(),
                volume = it.volume,
                stackCode = StackEntityResponse(
                    id = it.stack_code_id.toUUIDOrNull(),
                    displayCode = it.stack_code!!,
                    displayName = it.stack_code_name!!
                )
            )
        }
    }

    override suspend fun getStacksCachedForInsert(): List<CreateStackDTO> {
        return queries.selectCachedForInsertStack().executeAsList().map {
            CreateStackDTO(
                id = it.id.toUUIDOrNull(),
                length = it.length.toFloat(),
                height = it.height.toFloat(),
                thickness = it.thickness.toFloat(),
                displayName = it.display_name,
                numberOfPieces = it.number_of_pieces.toInt(),
                stackCodeId = it.stack_code_id.toUUIDOrNull(),
            )
        }
    }

    override suspend fun getStackCodesCachedForInsert(): List<CreateStackCodeDTO> {
        return queries.selectCachedForInsertStackCode().executeAsList().map {
            CreateStackCodeDTO(
                id = it.id.toUUIDOrNull(),
                displayCode = it.display_code,
                displayName = it.display_name,
            )
        }
    }

    override suspend fun unCacheStackInsert(id: UUID) = queries.transaction {
        queries.unCacheStackForInsert(id.toString())
    }

    override suspend fun unCacheStackCodeInsert(id: UUID) = queries.transaction {
        queries.unCacheStackCodeForInsert(id.toString())
    }

    override suspend fun deleteUnCachedStacks() = queries.transaction() {
        queries.deleteUnCachedStack()
    }

    override suspend fun deleteUnCachedStackCodes() = queries.transaction {
        queries.deleteUnCachedStackCode()
    }

    override suspend fun getStackById(id: UUID): GetStackDTO {
        return queries.selectStackById(id.toString()).executeAsOne().let {
            GetStackDTO(
                id = it.id.toUUIDOrNull(),
                length = it.length.toFloat(),
                height = it.height.toFloat(),
                thickness = it.thickness.toFloat(),
                displayName = it.display_name,
                numberOfPieces = it.number_of_pieces.toInt(),
                volume = it.volume,
                stackCode = StackEntityResponse(
                    id = it.stack_code_id.toUUIDOrNull(),
                    displayCode = it.stack_code!!,
                    displayName = it.stack_code_name!!
                )
            )
        }
    }
    override suspend fun updateStack(createStackDTO: CreateStackDTO) = queries.transaction {
        queries.updateStack(
            display_name = createStackDTO.displayName!!,
            length = createStackDTO.length!!.toDouble(),
            height = createStackDTO.height!!.toDouble(),
            thickness = createStackDTO.thickness!!.toDouble(),
            number_of_pieces = createStackDTO.numberOfPieces!!.toLong(),
            stack_code_id = createStackDTO.stackCodeId.toString(),
            id = createStackDTO.id.toString())
    }
    override suspend fun  updateStackCode(createStackCodeDTO: CreateStackCodeDTO)=
    queries.transaction{ queries.updateStackCode(
        display_code = createStackCodeDTO.displayCode!!,
        display_name = createStackCodeDTO.displayName!!,
        id = createStackCodeDTO.id.toString())
    }
    override suspend fun unCacheStackUpdate(id: UUID)=queries.transaction {
        queries.unCacheStackForUpdate(id.toString())
    }
    override  suspend fun  unCacheStackCodeUpdate(id: UUID) = queries.transaction{
        queries.unCacheStackCodeForUpdate(id.toString())
    }
    override suspend fun getStacksCachedForUpdate():List<CreateStackDTO>{
        return queries.selectCachedForUpdateStack().executeAsList().map {
            CreateStackDTO(
                id = it.id.toUUIDOrNull(),
                length = it.length.toFloat(),
                height = it.height.toFloat(),
                thickness = it.thickness.toFloat(),
                displayName = it.display_name,
                numberOfPieces = it.number_of_pieces.toInt(),
                stackCodeId = it.stack_code_id.toUUIDOrNull(),
            )
        }
    }
    override suspend fun getStackCodesCachedForUpdate():List<CreateStackCodeDTO>
    {
        return   queries.selectCachedForUpdateStackCode().executeAsList().map {
            CreateStackCodeDTO(
                id = it.id.toUUIDOrNull(),
                displayCode = it.display_code,
                displayName = it.display_name,
            )
        }
    }
}