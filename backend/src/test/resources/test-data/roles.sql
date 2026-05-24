-- test-data/roles.sql
-- Reference data for the "roles" table — required by JwtAuthenticationFilter.
--
-- Uses PostgreSQL "ON CONFLICT DO NOTHING" (not H2's MERGE ... KEY syntax)
-- because @ActiveProfiles("test") connects to the real PostgreSQL database.
--
-- Safe to run repeatedly: if the row already exists it is left untouched.
-- Place this file at: src/test/resources/test-data/roles.sql

INSERT INTO roles (id, name) VALUES (1, 'ADMIN') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (2, 'TUTOR') ON CONFLICT (id) DO NOTHING;
INSERT INTO roles (id, name) VALUES (3, 'STUDENT') ON CONFLICT (id) DO NOTHING;