-- Modulo de usuarios. Ejecutar en MySQL Workbench antes de iniciar la aplicacion.
-- No elimina tablas ni modifica otros modulos.
CREATE DATABASE IF NOT EXISTS caninoshop;
USE caninoshop;

CREATE USER IF NOT EXISTS 'usuario_caninoshop'@'%' IDENTIFIED BY 'caninoshop_sc403';
GRANT ALL PRIVILEGES ON caninoshop.* TO 'usuario_caninoshop'@'%';

CREATE TABLE IF NOT EXISTS rol (
    id_rol INT NOT NULL AUTO_INCREMENT,
    rol VARCHAR(25) NOT NULL,
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol (rol)
);

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(75) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    telefono VARCHAR(25),
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_username (username),
    UNIQUE KEY uq_usuario_correo (correo)
);

CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
);

INSERT IGNORE INTO rol (rol) VALUES ('CLIENTE'), ('VENDEDOR'), ('ADMIN');
