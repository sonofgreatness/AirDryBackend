package org.montigny.client.service

import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.montigny.client.persistence.ApprovalStatus
import org.montigny.client.persistence.Organizations
import org.montigny.client.persistence.WeeklyApprovals

import java.time.*

class WeeklyApprovalScheduler {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun start() {
        scope.launch {
            while (true) {
                try {
                    runCheck()
                } catch (e: Exception) {
                    println("WeeklyApprovalScheduler failed: ${e.message}")
                    e.printStackTrace()
                }

                delay(3 * 24 * 60 * 60 * 1000L) // every 3 days
            }
        }
    }

    private fun runCheck() {
        val today = LocalDate.now()
        val isStartOfWeek =
            today.dayOfWeek == DayOfWeek.MONDAY ||
                    today.dayOfWeek == DayOfWeek.SUNDAY

        ensureCurrentWeekExists()

        if (isStartOfWeek) {
            println("WeeklyApprovalScheduler: Start-of-week check completed")
        } else {
            println("WeeklyApprovalScheduler: Maintenance check completed")
        }
    }

    private fun ensureCurrentWeekExists() {
        val today = LocalDate.now()
        val weekStart = today.with(DayOfWeek.MONDAY)
        val weekEnd = today.with(DayOfWeek.SUNDAY)

        transaction {
            Organizations.selectAll().forEach { org ->
                val orgId = org[Organizations.id]

                val exists = WeeklyApprovals
                    .select {
                        (WeeklyApprovals.organizationId eq orgId) and
                                (WeeklyApprovals.weekStartDate eq weekStart)
                    }
                    .singleOrNull()

                if (exists == null) {
                    WeeklyApprovals.insert {
                        it[organizationId] = orgId
                        it[weekStartDate] = weekStart
                        it[weekEndDate] = weekEnd
                        it[status] = ApprovalStatus.PENDING
                    }
                    println("Created weekly approval for org=$orgId week=$weekStart")
                }
            }
        }
    }
}