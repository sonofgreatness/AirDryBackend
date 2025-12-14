package org.montigny.client.persistence.repository

import org.montigny.client.dto.Common.ReleaseDTO
import org.montigny.client.dto.User.CurrentUserDTO
import java.util.UUID

interface ReleaseRepository {


   fun  createRelease(
       release: ReleaseDTO
   ) : UUID
   fun  updateRelease(
        release: ReleaseDTO
    ): Int

    fun deleteRelease(
       releaseId: UUID
   ): Int

    fun getReleaseById(id: UUID): ReleaseDTO?
   fun  getAllReleases(): List<ReleaseDTO>
   fun getAllWindowsReleases(): List<ReleaseDTO>
   fun getAllAndroidReleases(): List<ReleaseDTO>

}