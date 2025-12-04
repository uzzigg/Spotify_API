package com.example.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID
import java.time.Instant

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())
}

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)
    override fun deserialize(decoder: Decoder): Instant = Instant.parse(decoder.decodeString())
    override fun serialize(encoder: Encoder, value: Instant) = encoder.encodeString(value.toString())
}

@Serializable
data class ArtistCreateRequest(val name: String, val genre: String? = null)

@Serializable
data class ArtistDto(@Serializable(with = UUIDSerializer::class) val id: UUID,
                     val name: String,
                     val genre: String? = null,
                     @Serializable(with = InstantSerializer::class) val createdAt: Instant,
                     @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
                     val albums: List<AlbumDto>? = null)

@Serializable
data class AlbumCreateRequest(val title: String, val releaseYear: Int? = null, @Serializable(with = UUIDSerializer::class) val artistId: UUID)

@Serializable
data class AlbumDto(@Serializable(with = UUIDSerializer::class) val id: UUID,
                    val title: String,
                    val releaseYear: Int? = null,
                    @Serializable(with = UUIDSerializer::class) val artistId: UUID,
                    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
                    @Serializable(with = InstantSerializer::class) val updatedAt: Instant,
                    val tracks: List<TrackDto>? = null)

@Serializable
data class TrackCreateRequest(val title: String, val duration: Int, @Serializable(with = UUIDSerializer::class) val albumId: UUID)

@Serializable
data class TrackDto(@Serializable(with = UUIDSerializer::class) val id: UUID,
                    val title: String,
                    val duration: Int,
                    @Serializable(with = UUIDSerializer::class) val albumId: UUID,
                    @Serializable(with = InstantSerializer::class) val createdAt: Instant,
                    @Serializable(with = InstantSerializer::class) val updatedAt: Instant)

@Serializable
data class ErrorResponse(val error: String, val message: String)
