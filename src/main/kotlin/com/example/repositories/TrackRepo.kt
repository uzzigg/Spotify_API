package com.example.repositories

import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.time.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class TrackRepo {
    fun create(title: String, duration: Int, albumId: UUID): TrackDto = transaction {
        // validate album exists
        Albumes.select { Albumes.id eq albumId }.singleOrNull() ?: throw IllegalArgumentException("AlbumNotFound")
        if (duration <= 0) throw IllegalArgumentException("DurationMustBePositive")
        val id = UUID.randomUUID()
        val now = Instant.now()
        Tracks.insert {
            it[Tracks.id] = id
            it[Tracks.title] = title
            it[Tracks.duration] = duration
            it[Tracks.albumId] = albumId
            it[Tracks.createdAt] = now
            it[Tracks.updatedAt] = now
        }
        TrackDto(id = id, title = title, duration = duration, albumId = albumId, createdAt = now, updatedAt = now)
    }

    fun list(albumId: UUID?): List<TrackDto> = transaction {
        var query: Query = Tracks.selectAll()
        if (albumId != null) query = query.adjustWhere { Tracks.albumId eq albumId }
        query.map { rowToDto(it) }
    }

    fun findById(id: UUID): TrackDto? = transaction {
        val row = Tracks.select { Tracks.id eq id }.singleOrNull() ?: return@transaction null
        rowToDto(row)
    }

    fun update(id: UUID, title: String?, duration: Int?, albumId: UUID?): TrackDto? = transaction {
        val row = Tracks.select { Tracks.id eq id }.singleOrNull() ?: return@transaction null
        if (albumId != null) Albumes.select { Albumes.id eq albumId }.singleOrNull() ?: throw IllegalArgumentException("AlbumNotFound")
        if (duration != null && duration <= 0) throw IllegalArgumentException("DurationMustBePositive")
        Tracks.update({ Tracks.id eq id }) {
            if (title != null) it[Tracks.title] = title
            if (duration != null) it[Tracks.duration] = duration
            if (albumId != null) it[Tracks.albumId] = albumId
            it[Tracks.updatedAt] = Instant.now()
        }
        Tracks.select { Tracks.id eq id }.single().let { rowToDto(it) }
    }

    fun delete(id: UUID): Boolean = transaction {
        Tracks.deleteWhere { Tracks.id eq id } > 0
    }

    fun listByAlbum(albumId: UUID): List<TrackDto> = transaction {
        Tracks.select { Tracks.albumId eq albumId }.map { rowToDto(it) }
    }

    private fun rowToDto(row: ResultRow): TrackDto {
        val id = row[Tracks.id]
        val title = row[Tracks.title]
        val duration = row[Tracks.duration]
        val albumId = row[Tracks.albumId]
        val createdAt = row[Tracks.createdAt]
        val updatedAt = row[Tracks.updatedAt]
        return TrackDto(id = id, title = title, duration = duration, albumId = albumId, createdAt = createdAt, updatedAt = updatedAt)
    }
}
