package com.example.repositories

import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import java.time.Instant
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq


class ArtistRepo {
    fun create(name: String, genre: String?): ArtistDto = transaction {
        val id = UUID.randomUUID()
        val now = Instant.now()
        Artistas.insert {
            it[Artistas.id] = id
            it[Artistas.name] = name
            it[Artistas.genre] = genre
            it[Artistas.createdAt] = now
            it[Artistas.updatedAt] = now
        }
        ArtistDto(id = id, name = name, genre = genre, createdAt = now, updatedAt = now)
    }

    fun list(limit: Int?, offset: Long?): List<ArtistDto> = transaction {
        Artistas.selectAll().limit(limit ?: 100, offset ?: 0L).map { rowToDto(it, includeAlbums = false) }
    }

    fun findById(id: UUID, includeAlbums: Boolean = true): ArtistDto? = transaction {
        val row = Artistas.select { Artistas.id eq id }.singleOrNull() ?: return@transaction null
        rowToDto(row, includeAlbums)
    }

    fun update(id: UUID, name: String?, genre: String?): ArtistDto? = transaction {
        val exists = Artistas.select { Artistas.id eq id }.singleOrNull() ?: return@transaction null
        Artistas.update({ Artistas.id eq id }) {
            if (name != null) it[Artistas.name] = name
            if (genre != null) it[Artistas.genre] = genre
            it[Artistas.updatedAt] = Instant.now()
        }
        Artistas.select { Artistas.id eq id }.single().let { rowToDto(it, includeAlbums = true) }
    }

    fun delete(id: UUID): Boolean = transaction {
        val hasAlbums = Albumes.select { Albumes.artistId eq id }.limit(1).any()
        if (hasAlbums) return@transaction false
        Artistas.deleteWhere { Artistas.id eq id } > 0
    }

    private fun rowToDto(row: ResultRow, includeAlbums: Boolean): ArtistDto {
        val id = row[Artistas.id]
        val name = row[Artistas.name]
        val genre = row[Artistas.genre]
        val createdAt = row[Artistas.createdAt]
        val updatedAt = row[Artistas.updatedAt]
        val albums = if (includeAlbums) {
            AlbumRepo().listByArtist(id)
        } else null
        return ArtistDto(id = id, name = name, genre = genre, createdAt = createdAt, updatedAt = updatedAt, albums = albums)
    }
}
