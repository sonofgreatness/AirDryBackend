package org.montigny.client.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database


object DatabaseFactory {
    private lateinit var hikari: HikariDataSource
    fun init(config: ApplicationConfig) {
        val jdbcUrl = config.property("db.jdbcUrl").getString()
        val user = config.property("db.user").getString()
        val password = config.property("db.password").getString()
        val poolSize = config.propertyOrNull("db.maximumPoolSize")?.getString()?.toInt() ?: 10

        val config = HikariConfig().apply {
            this.jdbcUrl = jdbcUrl
            this.username = user
            this.password = password
            this.maximumPoolSize = poolSize
            this.driverClassName = "org.postgresql.Driver"
            this.connectionTimeout = 125000
            addDataSourceProperty("socketTimeout", "120") // 2 minutes
            addDataSourceProperty("loginTimeout", "60")
        }
        hikari = HikariDataSource(config)
        Database.connect(hikari)

        // Run Flyway migrations programmatically
        val flyway = Flyway.configure()
            .dataSource(hikari)
            .locations("classpath:db/migration")
            .baselineOnMigrate(true)
            .load()
        flyway.migrate()
    }
    fun close() {
        hikari.close()
    }
}
