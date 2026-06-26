-- ============================================================
-- CitaFácil EsSalud — Esquema de base de datos (PostgreSQL)
-- Basado en el Capítulo 5.2 del documento del proyecto.
-- NOTA: Las tablas también se generan automáticamente vía JPA
-- (spring.jpa.hibernate.ddl-auto=update). Este script sirve como
-- documentación del modelo físico y para creación manual.
-- ============================================================

-- Ejecutar primero (como usuario postgres):
--   CREATE DATABASE citafacil;
-- Luego conectarse a 'citafacil' y correr este script.

CREATE TABLE IF NOT EXISTS establecimiento (
    id_establecimiento BIGSERIAL PRIMARY KEY,
    nombre     VARCHAR(150) NOT NULL,
    direccion  VARCHAR(200),
    distrito   VARCHAR(100),
    tipo       VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS asegurado (
    id_asegurado     BIGSERIAL PRIMARY KEY,
    dni              VARCHAR(8) NOT NULL UNIQUE,
    nombres          VARCHAR(100) NOT NULL,
    apellidos        VARCHAR(100) NOT NULL,
    fecha_nacimiento DATE,
    telefono         VARCHAR(20),
    correo           VARCHAR(120),
    contrasena_hash  VARCHAR(255) NOT NULL,
    fecha_registro   TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS medico (
    id_medico          BIGSERIAL PRIMARY KEY,
    nombres            VARCHAR(100) NOT NULL,
    apellidos          VARCHAR(100) NOT NULL,
    especialidad       VARCHAR(100) NOT NULL,
    cmp                VARCHAR(20),
    id_establecimiento BIGINT REFERENCES establecimiento(id_establecimiento)
);

CREATE TABLE IF NOT EXISTS horario_disponible (
    id_horario        BIGSERIAL PRIMARY KEY,
    id_medico         BIGINT NOT NULL REFERENCES medico(id_medico),
    fecha             DATE NOT NULL,
    hora_inicio       TIME,
    hora_fin          TIME,
    cupos_totales     INTEGER NOT NULL,
    cupos_disponibles INTEGER NOT NULL,
    estado            VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS cita (
    id_cita          BIGSERIAL PRIMARY KEY,
    id_asegurado     BIGINT NOT NULL REFERENCES asegurado(id_asegurado),
    id_horario       BIGINT NOT NULL REFERENCES horario_disponible(id_horario),
    codigo_reserva   VARCHAR(30) NOT NULL UNIQUE,
    fecha_reserva    TIMESTAMP NOT NULL DEFAULT NOW(),
    estado           VARCHAR(20) NOT NULL,  -- CONFIRMADA / CANCELADA / ATENDIDA / NO_ASISTIO
    canal_reserva    VARCHAR(20),
    fecha_cancelacion TIMESTAMP
);

CREATE TABLE IF NOT EXISTS lista_espera (
    id_espera             BIGSERIAL PRIMARY KEY,
    id_asegurado          BIGINT NOT NULL REFERENCES asegurado(id_asegurado),
    id_horario            BIGINT NOT NULL REFERENCES horario_disponible(id_horario),
    posicion              INTEGER,
    fecha_registro_espera TIMESTAMP NOT NULL DEFAULT NOW(),
    estado_espera         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion BIGSERIAL PRIMARY KEY,
    id_cita         BIGINT NOT NULL REFERENCES cita(id_cita),
    tipo            VARCHAR(20),  -- RECORDATORIO / CONFIRMACION / CANCELACION / CUPO_DISPONIBLE
    canal           VARCHAR(20),  -- SMS / WHATSAPP
    fecha_envio     TIMESTAMP,
    estado_envio    VARCHAR(20)
);

-- Índices sobre campos de búsqueda frecuente (modelo físico, 3FN)
CREATE INDEX IF NOT EXISTS idx_asegurado_dni       ON asegurado(dni);
CREATE INDEX IF NOT EXISTS idx_horario_medico_fecha ON horario_disponible(id_medico, fecha);
CREATE INDEX IF NOT EXISTS idx_cita_asegurado      ON cita(id_asegurado);
