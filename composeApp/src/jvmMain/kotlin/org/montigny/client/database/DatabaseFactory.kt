package org.montigny.client.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object DatabaseFactory : DatabaseProvider {
    override val database: AppDatabase by lazy {
        val dbFile = getDatabasePath()
        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")
        AppDatabase.Schema.create(driver)
        AppDatabase(driver)
    }

    private fun getDatabasePath(): File {
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val appDir = File(appData, "MontignyClient")
        if (!appDir.exists()) appDir.mkdirs()
        return File(appDir, "app_database.db")
    }
}