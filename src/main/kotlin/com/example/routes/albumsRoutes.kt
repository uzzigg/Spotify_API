package com.example.routes

import com.example.models.*
import com.example.repositories.AlbumRepo
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.util.UUID

fun Route.albumsRoutes() {
    val repo = AlbumRepo()

    route("/album") {
        post {
            try {
                val req = call.receive<AlbumCreateRequest>()
                if (req.title.isBlank()) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","title no puede estar vacío"))
                if (req.releaseYear != null && req.releaseYear < 1900) return@post call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","releaseYear debe ser >= 1900"))
                val created = try { repo.create(req.title.trim(), req.releaseYear, req.artistId) } catch (e: IllegalArgumentException) { return@post call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Artista no encontrado")) }
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.application.environment.log.error("Error creating album", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal","error interno"))
            }
        }

        get {
            val artistId = call.request.queryParameters["artistId"]?.let { try { UUID.fromString(it) } catch(e: Exception) { null } }
            val year = call.request.queryParameters["year"]?.toIntOrNull()
            val list = repo.list(artistId, year)
            call.respond(HttpStatusCode.OK, list)
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val album = repo.findById(id, includeTracks = true) ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Album no encontrado"))
            call.respond(HttpStatusCode.OK, album)
        }

        put("/{id}") {
            val idParam = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val req = call.receive<AlbumCreateRequest>()
            try {
                val updated = repo.update(id, req.title.takeIf { it.isNotBlank() }, req.releaseYear, req.artistId)
                if (updated == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Album no encontrado")) else call.respond(HttpStatusCode.OK, updated)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Artista no encontrado"))
            } catch (e: Exception) {
                call.application.environment.log.error("Error updating album", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal","error interno"))
            }
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val success = repo.delete(id)
            if (!success) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Conflict","No se puede eliminar el album: existen tracks asociados."))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Album eliminado"))
            }
        }
    }
}
