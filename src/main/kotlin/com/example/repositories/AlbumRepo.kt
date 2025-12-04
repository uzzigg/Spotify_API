package com.example.repositories

import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.time.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class AlbumRepo {
    fun create(title: String, releaseYear: Int?, artistId: UUID): AlbumDto = transaction {
        // validate artist exists
        val artistExists = Artistas.select { Artistas.id eq artistId }.singleOrNull() ?: throw IllegalArgumentException("ArtistNotFound")
        val id = UUID.randomUUID()
        val now = Instant.now()
        Albumes.insert {
            it[Albumes.id] = id
            it[Albumes.title] = title
            it[Albumes.releaseYear] = releaseYear
            it[Albumes.artistId] = artistId
            it[Albumes.createdAt] = now
            it[Albumes.updatedAt] = now
        }
        AlbumDto(id = id, title = title, releaseYear = releaseYear, artistId = artistId, createdAt = now, updatedAt = now)
    }

    fun list(artistId: UUID?, year: Int?): List<AlbumDto> = transaction {
        var query: Query = Albumes.selectAll()
        if (artistId != null) query = query.adjustWhere { Albumes.artistId eq artistId }
        if (year != null) query = query.adjustWhere { Albumes.releaseYear eq year }
        query.map { rowToDto(it, includeTracks = false) }
    }

    fun findById(id: UUID, includeTracks: Boolean = true): AlbumDto? = transaction {
        val row = Albumes.select { Albumes.id eq id }.singleOrNull() ?: return@transaction null
        rowToDto(row, includeTracks)
    }

    fun update(id: UUID, title: String?, releaseYear: Int?, artistId: UUID?): AlbumDto? = transaction {
        val row = Albumes.select { Albumes.id eq id }.singleOrNull() ?: return@transaction null
        if (artistId != null) {
            Artistas.select { Artistas.id eq artistId }.singleOrNull() ?: throw IllegalArgumentException("ArtistNotFound")
        }
        Albumes.update({ Albumes.id eq id }) {
            if (title != null) it[Albumes.title] = title
            if (releaseYear != null) it[Albumes.releaseYear] = releaseYear
            if (artistId != null) it[Albumes.artistId] = artistId
            it[Albumes.updatedAt] = Instant.now()
        }
        Albumes.select { Albumes.id eq id }.single().let { rowToDto(it, includeTracks = true) }
    }

    fun delete(id: UUID): Boolean = transaction {
        val hasTracks = Tracks.select { Tracks.albumId eq id }.limit(1).any()
        if (hasTracks) return@transaction false
        Albumes.deleteWhere { Albumes.id eq id } > 0
    }

    fun listByArtist(artistId: UUID): List<AlbumDto> = transaction {
        Albumes.select { Albumes.artistId eq artistId }.map { rowToDto(it, includeTracks = true) }
    }

    private fun rowToDto(row: ResultRow, includeTracks: Boolean): AlbumDto {
        val id = row[Albumes.id]
        val title = row[Albumes.title]
        val releaseYear = row[Albumes.releaseYear]
        val artistId = row[Albumes.artistId]
        val createdAt = row[Albumes.createdAt]
        val updatedAt = row[Albumes.updatedAt]
        val tracks = if (includeTracks) TrackRepo().listByAlbum(id) else null
        return AlbumDto(id = id, title = title, releaseYear = releaseYear, artistId = artistId, createdAt = createdAt, updatedAt = updatedAt, tracks = tracks)
    }
}
