package com.example.models

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Artistas : Table("artistas") {
    val id = uuid("id")
    val name = varchar("name", 255)
    val genre = varchar("genre", 100).nullable()
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

object Albumes : Table("albumes") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val releaseYear = integer("release_year").nullable()
    val artistId = uuid("artist_id").references(
        Artistas.id,
        onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT,
        onUpdate = org.jetbrains.exposed.sql.ReferenceOption.CASCADE
    )
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

object Tracks : Table("tracks") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val duration = integer("duration")
    val albumId = uuid("album_id").references(
        Albumes.id,
        onDelete = org.jetbrains.exposed.sql.ReferenceOption.RESTRICT,
        onUpdate = org.jetbrains.exposed.sql.ReferenceOption.CASCADE
    )
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}
