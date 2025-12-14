package org.montigny.client.service

import org.montigny.client.dto.Common.ReleaseDTO
import org.montigny.client.persistence.repository.ReleaseRepository
import java.util.UUID

class ReleaseService (private val releaseRepository: ReleaseRepository) {

    fun  createRelease(
        release: ReleaseDTO
    ) : UUID = releaseRepository.createRelease(release)

    fun  updateRelease(
        release: ReleaseDTO
    ): Int = releaseRepository.updateRelease(release)

    fun deleteRelease(
        releaseId: UUID
    ): Int  = releaseRepository.deleteRelease(releaseId)

    fun getReleaseById(id: UUID): ReleaseDTO? =releaseRepository.getReleaseById(id)
    fun  getAllReleases(): List<ReleaseDTO> =releaseRepository.getAllReleases()

    fun getAllWindowsReleases(): List<ReleaseDTO> = releaseRepository.getAllWindowsReleases()

    fun getAllAndroidReleases(): List<ReleaseDTO> = releaseRepository.getAllAndroidReleases()

}