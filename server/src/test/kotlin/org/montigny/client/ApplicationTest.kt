package org.montigny.client

import com.typesafe.config.ConfigFactory
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.testing.*

import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.context.GlobalContext.startKoin
import org.montigny.client.config.DatabaseFactory
import org.montigny.client.di.appModule
import org.montigny.client.persistence.UserTypes
import kotlin.test.*

class ApplicationTest {

    @Ignore
    @Test
    fun testRoot() = testApplication {
        /*application {
            module()
        }*/
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Ktor: ${Greeting().greet()}", response.bodyAsText())
    }

    @Ignore
    @Test
    fun testDatabaseConnection() {
        // 1️⃣ Start Koin with your module
        startKoin {
            modules(appModule) // your DI module
        }
        val config = HoconApplicationConfig(ConfigFactory.load("application.conf"))
        DatabaseFactory.init(config.config("ktor"))
        // 2️⃣ Inject Database (or just run transaction)
        transaction {
            // Example: query user_type table
            val count = UserTypes.selectAll().count()
            assertEquals(3, count, "user_type table should have exactly 3 records")
        }
    }

}