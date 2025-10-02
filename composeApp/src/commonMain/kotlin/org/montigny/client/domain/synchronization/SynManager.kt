package org.montigny.client.domain.synchronization

import com.russhwolf.settings.Settings
import org.com.client.database.User_type
import org.montigny.client.data.api.repository.UserApi
import org.montigny.client.database.dao.UserRepository
import org.montigny.client.dto.User.UserRegisterRequest
import org.montigny.client.dto.User.UserTypesDTO
import org.montigny.client.util.Constants.Companion.FRESH_DATA_KEY
import java.time.Instant
import kotlin.time.Duration.Companion.hours


/******************************************************************************
 * Responsible for handling sync  of remote database with
 *   LocalDatabase
 *@params:UserApi - retrieves  remote records
 *@params: UserTypeRepository - used to update LocalDatabase table User_type
 ****************************************************************************/
class SyncManager(private val userApi : UserApi,
                  private val userRepository: UserRepository,
                  private val settings: Settings

)
{




      suspend  fun downloadAndInsertUserTypes() {

        val remoteUserTypes: List<UserTypesDTO> = userApi.getUserTypes()
        val localUserTypes = userRepository.getAll()
        if(localUserTypes.isNotEmpty())
         userRepository.deleteAllUserType()
        val newInserts =  remoteUserTypes.map { User_type(
            id = it.id.toString(),
            displayname = it.displayName)
        }
        newInserts.forEach {
            type -> userRepository.insert(type)
        }
    }

    suspend fun  downloadAndInsertUsers(){
        try {
            val remoteUsers: List<UserRegisterRequest> = userApi.getUsers().items.map {
                UserRegisterRequest(
                    id = it.id, name = it.name, email = it.email,
                    phoneNumber = it.phoneNumber, secondaryPhoneNumber = it.secondaryPhoneNumber,
                    userTypeId = it.userType?.id, organizationId = it.organization?.id,
                    departmentId = it.department?.id, sectionId = it.section?.id,
                    isApproved = it.isApproved, isActive = it.isActive
                )
            }

            val localUsers = userRepository.selectAllUsers()

            if (localUsers?.isNotEmpty() == true)
                userRepository.deleteAllUsers()
            remoteUsers.forEach {
                userRepository.insertAppUser(it)
            }
        }catch (e:Exception){
            println("Download failed ${e.localizedMessage}")
        }

    }

    private fun isDataStale(): Boolean{
        val savedTimestamp = Instant.ofEpochMilli(
            settings.getLong(FRESH_DATA_KEY, defaultValue = 0L)
        ).toEpochMilli()

        val currentTimestamp = Instant.now().toEpochMilli()
        val difference =
            if(savedTimestamp > currentTimestamp)
                savedTimestamp- currentTimestamp
        else
            currentTimestamp - savedTimestamp
        return difference.hours >= 24.hours
    }

}