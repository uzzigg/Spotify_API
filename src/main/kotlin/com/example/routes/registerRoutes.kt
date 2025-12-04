package com.example.routes

import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.registerRoutes() {
    routing {
        route("/api") {
            artistsRoutes()
            albumsRoutes()
            tracksRoutes()
        }
    }
}
