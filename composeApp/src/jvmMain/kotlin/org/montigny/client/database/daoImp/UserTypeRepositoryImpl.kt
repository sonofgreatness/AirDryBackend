package org.montigny.client.database.daoImp

import org.com.client.database.User_type
import org.montigny.client.database.AppDatabase
import org.montigny.client.database.dao.UserTypeRepository

class UserTypeRepositoryImpl(private val db: AppDatabase) : UserTypeRepository {



    private val queries = db.appDatabaseQueries

    override suspend fun insert(userType: User_type) {
        queries.insertUserType(
            id = userType.id,
            created_at = userType.created_at,
            updated_at = userType.updated_at,
            created_by = userType.created_by,
            updated_by = userType.updated_by,
            displayname = userType.displayname
        )
    }

    override suspend fun getAll(): List<User_type> =
        queries.selectAllUserTypes().executeAsList()

    override suspend fun getById(id: String): User_type? =
        queries.selectUserTypeById(id).executeAsOneOrNull()

    override suspend fun delete(id: String) {
        queries.deleteUserTypeById(id)
    }
}