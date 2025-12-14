package org.montigny.client.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.persistence.UserDailyAccess
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

class TrackingService {
    suspend fun logOrUpdateDailyAccess(userId: UUID, routePath: String) = newSuspendedTransaction {
        val today = LocalDate.now()
        val query = UserDailyAccess.select {
            (UserDailyAccess.appUserId eq userId) and
                    (UserDailyAccess.accessDate eq today) and
                    (UserDailyAccess.routePath eq routePath)  }

        val existingRecord = query.singleOrNull()

        if (existingRecord != null) {
            UserDailyAccess.update({ UserDailyAccess.id eq existingRecord[UserDailyAccess.id] }) {
                with(SqlExpressionBuilder) {
                    // ATOMIC INCREMENT: accessCount = accessCount + 1
                    it.update(UserDailyAccess.accessCount, UserDailyAccess.accessCount + 1)
                    it[updatedAt] = Instant.now()
                    it[updatedBy] = userId
              }
            }
        } else {
            UserDailyAccess.insert {
                it[this.routePath] = routePath.trim()
                it[appUserId] = userId
                it[accessDate] = today
                it[createdBy] = userId
                it[updatedBy] = userId
            }
        }
    }
}