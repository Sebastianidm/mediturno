-- 1. Tabla de Roles.

CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(20) UNIQUE NOT NULL
);

-- Insertar roles por defecto de la aplicación

INSERT INTO roles (nombre) VALUES 
('ROLE_PACIENTE'),
('ROLE_MEDICO'),
('ROLE_ADMIN');

-- 2. Tabla de Usuarios 
CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);


-- 3. Tabla intermedia para relacion M:N (Usuarios <-> Roles)
CREATE TABLE usuario_roles(
    usuario_id BIGINT NOT NULL,
    rol_id INT NOT NULL,
    PRIMARY KEY (usuario_id, rol_id),
    CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT fk_rol FOREIGN KEY (rol_id) REFERENCES roles(id) ON DELETE CASCADE
);

--4. Tabla de disponibilidad médica (Agenda)
CREATE TABLE agendas_medicas (
    id BIGSERIAL PRIMARY KEY,
    medico_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_medico_agenda FOREIGN KEY (medico_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    CONSTRAINT unique_agenda_medico UNIQUE (medico_id, fecha, hora_inicio)
);

-- 5. Tabla de Turnos / Citas Médicas
CREATE TABLE turnos (
    id BIGSERIAL PRIMARY KEY,
    paciente_id BIGINT NOT NULL,
    agenda_id BIGINT NOT NULL UNIQUE, -- Una disponibilidad solo puede tener un turno reservado
    estado VARCHAR(20) DEFAULT 'PENDIENTE', -- PENDIENTE, CONFIRMADO, CANCELADO, ATENDIDO
    fecha_reserva TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT fk_paciente FOREIGN KEY (paciente_id) REFERENCES usuarios(id),
    CONSTRAINT fk_agenda FOREIGN KEY (agenda_id) REFERENCES agendas_medicas(id)
);