package com.example.routes

import com.example.models.*
import com.example.repositories.TrackRepo
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.util.UUID

fun Route.tracksRoutes() {
    val repo = TrackRepo()

    route("/tracks") {
        post {
            try {
                val req = call.receive<TrackCreateRequest>()
                if (req.title.isBlank()) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","title no puede estar vacío"))
                if (req.duration <= 0) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","duration debe ser > 0"))
                val created = try { repo.create(req.title.trim(), req.duration, req.albumId) } catch (e: IllegalArgumentException) { return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Album no encontrado")) }
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.application.environment.log.error("Error creating track", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal","error interno"))
            }
        }

        get {
            val albumId = call.request.queryParameters["albumId"]?.let { try { UUID.fromString(it) } catch(e: Exception) { null } }
            val list = repo.list(albumId)
            call.respond(HttpStatusCode.OK, list)
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val track = repo.findById(id) ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Track no encontrado"))
            call.respond(HttpStatusCode.OK, track)
        }

        put("/{id}") {
            val idParam = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val req = call.receive<TrackCreateRequest>()
            try {
                val updated = repo.update(id, req.title.takeIf { it.isNotBlank() }, req.duration, req.albumId)
                if (updated == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Track no encontrado")) else call.respond(HttpStatusCode.OK, updated)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Album no encontrado"))
            } catch (e: Exception) {
                call.application.environment.log.error("Error updating track", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal","error interno"))
            }
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val success = repo.delete(id)
            if (!success) call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Track no encontrado")) else call.respond(HttpStatusCode.OK, mapOf("message" to "Track eliminado"))
        }
    }
}
