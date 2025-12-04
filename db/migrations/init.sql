-- db/migrations/init.sql
-- Migración inicial para catálogo musical

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Función que actualiza updated_at
CREATE OR REPLACE FUNCTION trigger_set_timestamp()
RETURNS TRIGGER AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Table: artistas
CREATE TABLE IF NOT EXISTS artistas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(255) NOT NULL,
  genre VARCHAR(100),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
CREATE TRIGGER artistas_set_timestamp
BEFORE UPDATE ON artistas
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- Table: albumes
CREATE TABLE IF NOT EXISTS albumes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255) NOT NULL,
  release_year INT,
  artist_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT fk_albumes_artista FOREIGN KEY (artist_id) REFERENCES artistas(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_release_year CHECK (release_year IS NULL OR release_year >= 1900)
);
CREATE INDEX IF NOT EXISTS idx_albumes_artist_id ON albumes(artist_id);
CREATE TRIGGER albumes_set_timestamp
BEFORE UPDATE ON albumes
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- Table: tracks
CREATE TABLE IF NOT EXISTS tracks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title VARCHAR(255) NOT NULL,
  duration INT NOT NULL,
  album_id UUID NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  CONSTRAINT fk_tracks_album FOREIGN KEY (album_id) REFERENCES albumes(id) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT chk_duration CHECK (duration > 0)
);
CREATE INDEX IF NOT EXISTS idx_tracks_album_id ON tracks(album_id);
CREATE TRIGGER tracks_set_timestamp
BEFORE UPDATE ON tracks
FOR EACH ROW EXECUTE PROCEDURE trigger_set_timestamp();

-- Nota: Las FK usan ON DELETE RESTRICT para prevenir borrado en cascada.
