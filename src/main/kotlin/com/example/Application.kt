package com.example

import com.example.db.DatabaseFactory
import com.example.routes.registerRoutes
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.http.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level.*

fun main() {
    embeddedServer(Netty, port = System.getenv("PORT")?.toInt() ?: 8080) {
        module()
    }.start(wait = true)
}

fun Application.module() {
    install(CallLogging) {
        level = INFO
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = false
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            // usar call.application.environment para acceder al environment desde este contexto
            call.application.environment.log.error("Unhandled error", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to "Internal", "message" to "Internal server error")
            )
        }
    }

    DatabaseFactory.init()

    registerRoutes()
}
