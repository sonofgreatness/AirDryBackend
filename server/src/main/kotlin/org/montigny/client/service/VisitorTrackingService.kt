package org.montigny.client.service

import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.montigny.client.persistence.VisitorDailyTraffic
import java.time.LocalDate
import org.jetbrains.exposed.sql.and

class VisitorTrackingService {

    /**
     * Logs or updates the daily access count for an anonymous visitor
     * based on their IP address, the current date, and the specific route accessed.
     * * @param ipAddress The visitor's IP address (or hashed IP).
     * @param routePath The specific route/page they visited.
     */
    suspend fun logOrUpdateDailyTraffic(ipAddress: String, routePath: String) = newSuspendedTransaction {
        val today = LocalDate.now()
        val query = VisitorDailyTraffic.select {
            (VisitorDailyTraffic.ipAddress eq ipAddress) and
                    (VisitorDailyTraffic.accessDate eq today) and
                    (VisitorDailyTraffic.routePath eq routePath)
        }

        val existingRecord = query.singleOrNull()

        if (existingRecord != null) {
            // 2. Record exists: Increment the accessCount atomically and update the timestamp.
            VisitorDailyTraffic.update({ VisitorDailyTraffic.id eq existingRecord[VisitorDailyTraffic.id] }) {
                with(SqlExpressionBuilder) {
                    // Atomic increment: accessCount = accessCount + 1
                    it.update(VisitorDailyTraffic.accessCount, VisitorDailyTraffic.accessCount + 1)
               }
            }
        } else {
            VisitorDailyTraffic.insert {
                it[VisitorDailyTraffic.ipAddress] = ipAddress
                it[VisitorDailyTraffic.accessDate] = today
                it[VisitorDailyTraffic.routePath] = routePath
            }
        }
    }
}