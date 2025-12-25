package org.montigny.client.persistence.implementation

import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.dto.Common.ReleaseDTO
import org.montigny.client.persistence.Platform
import org.montigny.client.persistence.Release
import org.montigny.client.persistence.Release.dateOfRelease
import org.montigny.client.persistence.repository.ReleaseRepository
import org.montigny.client.util.UtilityFunctions.Companion.convertDateToString
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class ReleasesRepositoryImpl() : ReleaseRepository {
    override fun createRelease(release: ReleaseDTO): UUID  = transaction{
        val today = LocalDate.now()
        Release.insertAndGetId {
            it[id] = release.id
            it[link] = release.link
            it[dateOfRelease] = today
            it[platform] = getPlatformFromText(release.platform)

        }.value
    }
    override fun updateRelease(release: ReleaseDTO) = transaction {
       val releaseToUpdate  =  Release.select {
           Release.id eq release.id
       }.firstOrNull()
        releaseToUpdate ?: throw IllegalArgumentException("Release not found")

        val today = LocalDate.now()
        Release.update({
            Release.id eq release.id })
        {
            it[link] = release.link
            it[dateOfRelease] = today
            it[platform] = getPlatformFromText(release.platform)
        }
    }
    private fun getPlatformFromText(text:String) : Platform{
        return if (text.lowercase().contains("android")){
            Platform.ANDROID
        } else
            Platform.WINDOWS
    }

    override fun deleteRelease(releaseId: UUID) :Int=transaction{
        val releaseToDelete  =  Release.select {
            Release.id eq releaseId
        }.firstOrNull()
        releaseToDelete ?: throw IllegalArgumentException("Release not found")
        Release.deleteWhere{
            id eq releaseId
        }

    }

    override fun getReleaseById(id: UUID): ReleaseDTO? = transaction {
        val release  =  Release.select {
            Release.id eq id
        }.singleOrNull()
            ?:return@transaction null
        ReleaseDTO(
            id = release[Release.id].value,
            link = release[Release.link],
            dateOfRelease = release[dateOfRelease].toString(),
            platform = release[Release.platform].name
        )
    }

    override fun getAllReleases(): List<ReleaseDTO> = transaction {
        Release.selectAll().orderBy(dateOfRelease, SortOrder.DESC
        ).map {
            release->
            ReleaseDTO(
                id = release[Release.id].value,
                link = release[Release.link],
                dateOfRelease = convertDateToString(release[dateOfRelease]),
                platform = release[Release.platform].name

            )
        }
    }

    override fun getAllWindowsReleases(): List<ReleaseDTO> = transaction {
        Release.selectAll().orderBy(
            dateOfRelease, SortOrder.DESC
        )
            .andWhere {
                Release.platform eq Platform.WINDOWS
            }
        .map {
                release->
            ReleaseDTO(
                id = release[Release.id].value,
                link = release[Release.link],
                dateOfRelease = convertDateToString(release[dateOfRelease]),
                platform = release[Release.platform].name

            )
        }
    }

    override fun getAllAndroidReleases(): List<ReleaseDTO> = transaction {
        Release.selectAll().orderBy(
            dateOfRelease, SortOrder.DESC)
            .andWhere {
                Release.platform eq Platform.ANDROID
            }
            .map {
                    release->
                ReleaseDTO(
                    id = release[Release.id].value,
                    link = release[Release.link],
                    dateOfRelease = convertDateToString(release[dateOfRelease]),
                    platform = release[Release.platform].name

                )
            }
    }

}