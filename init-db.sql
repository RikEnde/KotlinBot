-- Enable the pgvector extension
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS hstore;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- CREATE TABLE IF NOT EXISTS vector_store
-- (
--     id        uuid DEFAULT uuid_generate_v4() PRIMARY KEY,
--     content   text,
--     metadata  json,
--     embedding vector(1536)
-- );
--
-- CREATE INDEX ON vector_store USING HNSW (embedding vector_cosine_ops);

--
-- Roles
--
CREATE ROLE javabot;
ALTER ROLE javabot WITH NOSUPERUSER INHERIT CREATEROLE CREATEDB LOGIN NOREPLICATION NOBYPASSRLS PASSWORD 'SCRAM-SHA-256$4096:5e16y43rTV8M3rMFN6MdBQ==$6iAopIXuiFu8vdnILwmW6+UuctVgXtA8og9M0c0yroA=:F3HofRKNRxkHizJMZekTu5zT6BOzPYQ3yQ7P9nIBJm4=';

--
-- Databases
--

-- CREATE DATABASE images WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'en_US.utf8';
ALTER DATABASE images OWNER TO postgres;

\connect images

CREATE TABLE public.images (
                               id bigint NOT NULL,
                               image_data bytea,
                               discord_url text,
                               file_name text,
                               user_name text,
                               prompt text,
                               revised_prompt text,
                               created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.images OWNER TO postgres;

CREATE SEQUENCE public.images_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.images_id_seq OWNER TO postgres;
ALTER SEQUENCE public.images_id_seq OWNED BY public.images.id;

ALTER TABLE ONLY public.images ALTER COLUMN id SET DEFAULT nextval('public.images_id_seq'::regclass);

SELECT pg_catalog.setval('public.images_id_seq', 1, false);

ALTER TABLE ONLY public.images
    ADD CONSTRAINT images_pkey PRIMARY KEY (id);

GRANT CREATE ON DATABASE images TO javabot;
