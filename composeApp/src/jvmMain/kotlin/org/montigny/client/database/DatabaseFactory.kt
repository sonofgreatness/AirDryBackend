package org.montigny.client.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.io.File

object DatabaseFactory : DatabaseProvider {

    override val database: AppDatabase by lazy {
        val dbFile = getDatabasePath()
        println("Database file path: ${dbFile.absolutePath}")

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (!dbFile.exists()) {
            println("Database file not found. Creating schema...")
            AppDatabase.Schema.create(driver)
        } else {
            println("Database file exists. Running migrations if needed...")
            AppDatabase.Schema.migrate(driver, AppDatabase.Schema.version, AppDatabase.Schema.version)
        }

        AppDatabase(driver)
    }

    private fun getDatabasePath(): File {
        val appData = System.getenv("APPDATA") ?: System.getProperty("user.home")
        val appDir = File(appData, "MontignyClient")
        if (!appDir.exists()) appDir.mkdirs()
        return File(appDir, "app_database.db")
    }
}
