package com.example.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val databaseUrl = System.getenv("DATABASE_URL")
        val ds = if (!databaseUrl.isNullOrBlank()) {
            val config = HikariConfig().apply {
                jdbcUrl = databaseUrl
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 3
                isAutoCommit = false
                transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            }
            HikariDataSource(config)
        } else {
            val config = HikariConfig().apply {
                jdbcUrl = "jdbc:postgresql://localhost:5432/music_catalog"
                username = "postgres"      // ← CORRECTO
                password = "uzzPost14"      // ← CORRECTO
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = 3
            }
            HikariDataSource(config)
        }
        Database.connect(ds)
    }
}
