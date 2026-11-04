-- CaninoShop / Avance 2 - Script unico de la base de datos
-- Crea la base, el usuario de la aplicacion, todas las tablas y datos de prueba.
-- No elimina datos existentes: se puede ejecutar mas de una vez.
-- Ejecutar en MySQL Workbench con un usuario administrador (root) antes de iniciar la aplicacion.

CREATE DATABASE IF NOT EXISTS caninoshop
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

-- Credenciales de DESARROLLO (las mismas de application.properties): cambiarlas en produccion.
CREATE USER IF NOT EXISTS 'usuario_caninoshop'@'%' IDENTIFIED BY 'caninoshop_sc403';
GRANT SELECT, INSERT, UPDATE, DELETE ON caninoshop.* TO 'usuario_caninoshop'@'%';
FLUSH PRIVILEGES;

USE caninoshop;

-- ---------------------------------------------------------------------------
-- Usuarios y seguridad (HU01-HU05)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (
    id_rol INT NOT NULL AUTO_INCREMENT,
    rol VARCHAR(25) NOT NULL,
    PRIMARY KEY (id_rol),
    UNIQUE KEY uq_rol (rol)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario (
    id_usuario INT NOT NULL AUTO_INCREMENT,
    username VARCHAR(75) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    telefono VARCHAR(25),
    password VARCHAR(255) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    -- Doble autenticacion (2FA)
    totp_habilitado BOOLEAN NOT NULL DEFAULT FALSE,
    totp_secreto VARCHAR(512) NULL,
    totp_ultimo_paso BIGINT NOT NULL DEFAULT -1,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_usuario_username (username),
    UNIQUE KEY uq_usuario_correo (correo)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario INT NOT NULL,
    id_rol INT NOT NULL,
    PRIMARY KEY (id_usuario, id_rol),
    CONSTRAINT fk_usuario_rol_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_usuario_rol_rol FOREIGN KEY (id_rol) REFERENCES rol(id_rol)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS codigo_recuperacion (
    id_codigo BIGINT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    codigo_hash VARCHAR(255) NOT NULL,
    PRIMARY KEY (id_codigo),
    CONSTRAINT fk_codigo_recuperacion_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Catalogo e inventario (HU06-HU10)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo TINYINT(1) NOT NULL DEFAULT 1,
    PRIMARY KEY (id_categoria)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS producto (
    id_producto INT NOT NULL AUTO_INCREMENT,
    id_categoria INT NOT NULL,
    nombre VARCHAR(150) NOT NULL UNIQUE,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    estado TINYINT(1) NOT NULL DEFAULT 1,
    ruta_imagen VARCHAR(1024),
    destacado TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_producto),
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria),
    CHECK (precio >= 0),
    CHECK (stock >= 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Carrito y pedidos (HU11-HU15)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS carrito (
    id_carrito INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO',
    PRIMARY KEY (id_carrito),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_carrito (
    id_detalle_carrito INT NOT NULL AUTO_INCREMENT,
    id_carrito INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    PRIMARY KEY (id_detalle_carrito),
    UNIQUE KEY uq_detalle_carrito (id_carrito, id_producto),
    FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Gestion y seguimiento (HU16-HU20): estados de pedido, pedidos y notificaciones
-- El cliente crea el pedido al confirmar su carrito; gestion consulta y cambia su estado.
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS estado_pedido (
    id_estado_pedido INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    PRIMARY KEY (id_estado_pedido)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_estado_pedido INT NOT NULL,
    fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_pedido),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_estado_pedido) REFERENCES estado_pedido(id_estado_pedido),
    CHECK (total >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_detalle_pedido INT NOT NULL AUTO_INCREMENT,
    id_pedido INT NOT NULL,
    id_producto INT NOT NULL,
    cantidad INT NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_detalle_pedido),
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES producto(id_producto),
    CHECK (cantidad > 0),
    CHECK (precio >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS notificacion (
    id_notificacion INT NOT NULL AUTO_INCREMENT,
    id_usuario INT NOT NULL,
    id_pedido INT NOT NULL,
    mensaje VARCHAR(255) NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (id_notificacion),
    FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario),
    FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------------
-- Datos de prueba
-- Los usuarios se crean desde la pantalla de registro (/registro/nuevo); el rol CLIENTE se asigna
-- al registrarse. Para probar como administrador, asignarle el rol ADMIN a un usuario ya registrado:
--   INSERT IGNORE INTO usuario_rol (id_usuario, id_rol)
--   SELECT u.id_usuario, r.id_rol FROM usuario u, rol r WHERE u.username = 'su_usuario' AND r.rol = 'ADMIN';
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO rol (rol) VALUES ('CLIENTE'), ('VENDEDOR'), ('ADMIN');

INSERT IGNORE INTO estado_pedido (nombre) VALUES
    ('Pendiente'), ('En preparacion'), ('Enviado'), ('Entregado'), ('Cancelado');

INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
    ('Alimento', 'Comida para perros'),
    ('Accesorios', 'Collares, correas y juguetes');

INSERT IGNORE INTO producto (id_categoria, nombre, descripcion, precio, stock, estado, destacado)
SELECT c.id_categoria, p.nombre, p.descripcion, p.precio, p.stock, 1, p.destacado
FROM (
    SELECT 'Alimento' AS categoria, 'Croquetas Adulto 10kg' AS nombre, 'Alimento seco para perros adultos' AS descripcion, 18500.00 AS precio, 12 AS stock, 1 AS destacado
    UNION ALL SELECT 'Alimento', 'Snacks de pollo', 'Premios naturales de pollo', 3200.00, 30, 0
    UNION ALL SELECT 'Accesorios', 'Collar ajustable M', 'Collar de nylon talla mediana', 4500.00, 3, 0
    UNION ALL SELECT 'Accesorios', 'Juguete hueso de goma', 'Juguete resistente para morder', 2800.00, 25, 1
) p
JOIN categoria c ON c.nombre = p.categoria;
