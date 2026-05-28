CREATE DATABASE IF NOT EXISTS travelupp_db;
USE travelupp_db;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    telefono VARCHAR(20) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(100) DEFAULT '',
    foto_perfil TEXT,
    codigo_verificacion VARCHAR(10),
    verificado BOOLEAN DEFAULT FALSE,
    es_admin BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS viajes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    destino VARCHAR(100) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    participantes TEXT,
    usuario_id INT,
    estado VARCHAR(50) DEFAULT 'En preparación',
    cover_image TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS actividades (
    id INT AUTO_INCREMENT PRIMARY KEY,
    viaje_id INT NOT NULL,
    dia INT DEFAULT 1,
    tipo VARCHAR(50) DEFAULT 'actividad',
    hora VARCHAR(10) DEFAULT '',
    hora_fin VARCHAR(10) DEFAULT '',
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT,
    imagen TEXT,
    FOREIGN KEY (viaje_id) REFERENCES viajes(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contactos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario_id INT NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_contact (usuario_id, telefono)
);

CREATE TABLE IF NOT EXISTS viaje_invitaciones (
    id INT AUTO_INCREMENT PRIMARY KEY,
    viaje_id INT NOT NULL,
    invitador_usuario_id INT NOT NULL,
    invitado_usuario_id INT NOT NULL,
    estado VARCHAR(20) DEFAULT 'pendiente',
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_invitacion (viaje_id, invitado_usuario_id),
    FOREIGN KEY (viaje_id) REFERENCES viajes(id) ON DELETE CASCADE,
    FOREIGN KEY (invitador_usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (invitado_usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS album_fotos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    viaje_id INT NOT NULL,
    usuario_id INT NOT NULL,
    imagen TEXT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (viaje_id) REFERENCES viajes(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE
);

INSERT INTO usuarios (telefono, nombre, email, verificado, es_admin) VALUES
('618941168', 'Teresa Pedrero', 'teresa@travelupp.com', TRUE, TRUE),
('612345678', 'Ana García', 'ana@email.com', TRUE, FALSE),
('698765432', 'Carlos López', 'carlos@email.com', TRUE, FALSE);


