package com.example.routes

import com.example.models.*
import com.example.repositories.ArtistRepo
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.http.*
import java.util.UUID

fun Route.artistsRoutes() {
    val repo = ArtistRepo()

    route("/artist") {
        post {
            try {
                val req = call.receive<ArtistCreateRequest>()
                if (req.name.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation", "name no puede estar vacío"))
                    return@post
                }
                val created = repo.create(req.name.trim(), req.genre?.trim())
                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                call.application.environment.log.error("Error creating artist", e)
                call.respond(HttpStatusCode.InternalServerError, ErrorResponse("Internal", "error interno"))
            }
        }

        get {
            val limit = call.request.queryParameters["limit"]?.toIntOrNull()
            val offset = call.request.queryParameters["offset"]?.toLongOrNull()
            val list = repo.list(limit, offset)
            call.respond(HttpStatusCode.OK, list)
        }

        get("/{id}") {
            val idParam = call.parameters["id"] ?: return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@get call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val artist = repo.findById(id, includeAlbums = true) ?: return@get call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Artista no encontrado"))
            call.respond(HttpStatusCode.OK, artist)
        }

        put("/{id}") {
            val idParam = call.parameters["id"] ?: return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@put call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val req = call.receive<ArtistCreateRequest>()
            val updated = repo.update(id, req.name.takeIf { it.isNotBlank() }, req.genre)
            if (updated == null) call.respond(HttpStatusCode.NotFound, ErrorResponse("NotFound","Artista no encontrado")) else call.respond(HttpStatusCode.OK, updated)
        }

        delete("/{id}") {
            val idParam = call.parameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id requerido"))
            val id = try { UUID.fromString(idParam) } catch (e: Exception) { return@delete call.respond(HttpStatusCode.BadRequest, ErrorResponse("Validation","id inválido")) }
            val success = repo.delete(id)
            if (!success) {
                call.respond(HttpStatusCode.Conflict, ErrorResponse("Conflict","No se puede eliminar el artista: existen albumes asociados."))
            } else {
                call.respond(HttpStatusCode.OK, mapOf("message" to "Artista eliminado"))
            }
        }
    }
}
