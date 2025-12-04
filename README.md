# Catálogo Musical (Ktor + Exposed + PostgreSQL)

## Estructura
List of files included in the zip (project root):
- db/migrations/init.sql
- src/main/kotlin/com/example/Application.kt
- src/main/kotlin/com/example/db/DatabaseFactory.kt
- src/main/kotlin/com/example/models/Tables.kt
- src/main/kotlin/com/example/models/Dto.kt
- src/main/kotlin/com/example/repositories/ArtistRepo.kt
- src/main/kotlin/com/example/repositories/AlbumRepo.kt
- src/main/kotlin/com/example/repositories/TrackRepo.kt
- src/main/kotlin/com/example/routes/registerRoutes.kt
- src/main/kotlin/com/example/routes/artistsRoutes.kt
- src/main/kotlin/com/example/routes/albumsRoutes.kt
- src/main/kotlin/com/example/routes/tracksRoutes.kt
- src/main/resources/application.conf
- build.gradle.kts
- settings.gradle.kts
- postman_collection.json
- README.md

## Requisitos mínimos
- Java 17
- Gradle (wrapper incluido)
- PostgreSQL (>= 12)

## Migración
Crear la base de datos y ejecutar la migración:
```
psql $DATABASE_URL -f db/migrations/init.sql
```
Nota: `init.sql` incluye `CREATE EXTENSION IF NOT EXISTS "pgcrypto";` y las tablas con UUID por defecto `gen_random_uuid()`.

## Variables de entorno
- `DATABASE_URL` (recomendado): URL JDBC completa, por ejemplo `jdbc:postgresql://localhost:5432/catalogo?user=postgres&password=postgres`
- Alternativa: `DB_USER`, `DB_PASSWORD` y la URL local por defecto en `DatabaseFactory`.

## Correr la aplicación
```
./gradlew run
```

## Endpoints principales (base path `/api`, JSON en camelCase)
- **Artistas**
  - `POST /api/artistas` — crear artista.
  - `GET /api/artistas` — listar (soporta ?limit & ?offset).
  - `GET /api/artistas/:id` — obtener artista con `albums` (cada album incluye `tracks`). 
  - `PUT /api/artistas/:id` — actualizar (full o partial).
  - `DELETE /api/artistas/:id` — si tiene álbumes => `409 Conflict`.

- **Albumes**
  - `POST /api/albumes` — crear (valida existencia de artistId).
  - `GET /api/albumes` — listar (soporta ?artistId & ?year).
  - `GET /api/albumes/:id` — obtener con `tracks`.
  - `PUT /api/albumes/:id` — actualizar (si cambia artistId valida existencia).
  - `DELETE /api/albumes/:id` — si tiene tracks => `409 Conflict`.

- **Tracks**
  - `POST /api/tracks` — crear (valida albumId y duration > 0).
  - `GET /api/tracks` — listar (soporta ?albumId).
  - `GET /api/tracks/:id` — detalle.
  - `PUT /api/tracks/:id` — actualizar.
  - `DELETE /api/tracks/:id` — eliminar libremente.

## CamelCase <-> snake_case
- La API acepta y devuelve JSON en **camelCase** (ej: `artistId`, `createdAt`).
- En la base de datos las columnas usan **snake_case** (ej: `artist_id`, `created_at`).
- Ejemplo de SELECT con mapping explícito (psql):
```
SELECT id, name, genre, created_at AS createdAt, updated_at AS updatedAt FROM artistas;
```

## Postman
- Hay una colección `postman_collection.json` con 4 pruebas clave: crear artista, crear álbum, crear track, obtener artista con relaciones.
- Importar en Postman y configurar `{{base_url}}` a `http://localhost:8080` (o donde corra la app).

## Ejemplos de payloads y respuestas (textuales)
- `POST /api/artistas` request:
```
{ "name": "Artista Test Evaluador", "genre": "Rock Académico" }
```
response `201`:
```
{
  "id": "uuid",
  "name": "Artista Test Evaluador",
  "genre": "Rock Académico",
  "createdAt": "2025-11-30T...Z",
  "updatedAt": "2025-11-30T...Z"
}
```

- `POST /api/albumes` request:
```
{ "title": "Album de Prueba", "releaseYear": 2024, "artistId": "uuid-del-artista" }
```
response `201` similar (con `id`, `artistId`).

- `POST /api/tracks` request:
```
{ "title": "Track Final de Calificación", "duration": 240, "albumId": "uuid-del-album" }
```
response `201` similar (con `duration`, `albumId`).

- `GET /api/artistas/:id` response `200` incluye `albums` (cada album puede incluir `tracks`).

## Notas importantes
- Las FKs están definidas con `ON DELETE RESTRICT` y `ON UPDATE CASCADE` para prevenir borrado en cascada.
- Los triggers `trigger_set_timestamp()` actualizan `updated_at` automáticamente en cada tabla.
- Validaciones críticas se aplican tanto en la API como en la BD (por ejemplo `release_year >= 1900`, `duration > 0`).

## Entregables
- Proyecto listo para compilar con Gradle.
- `db/migrations/init.sql` (migración completa).
- `postman_collection.json`.
- `README.md` con instrucciones.
