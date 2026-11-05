-- =====================================================================
-- CaninoShop / Avance 2 - Base de datos comun
-- Script oficial del grupo (elaborado por Cristopher, basado en la
-- Figura 22 del Avance 1).
--
-- Como usarlo: abrir MySQL Workbench (o el cliente mysql) con un usuario
-- administrador (root) y ejecutar el script completo. Se puede correr
-- varias veces: no elimina datos existentes.
-- Crea la base "caninoshop", el usuario de la aplicacion, las tablas y
-- los datos de referencia (roles, estados, categorias y productos).
-- NO incluye usuarios: cada quien crea su cuenta desde "Crear cuenta" en
-- la aplicacion, porque la contrasena se guarda encriptada.
-- Credenciales de DESARROLLO: cambiar antes de compartir el proyecto.
-- =====================================================================
CREATE DATABASE IF NOT EXISTS caninoshop
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'usuario_caninoshop'@'%' IDENTIFIED BY 'caninoshop_sc403';
GRANT SELECT, INSERT, UPDATE, DELETE ON caninoshop.* TO 'usuario_caninoshop'@'%';
FLUSH PRIVILEGES;
USE caninoshop;

-- ---------------------------------------------------------------------
-- Carlos / Usuarios y seguridad (HU01-HU05)
-- ---------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS rol (id_rol INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(50) NOT NULL UNIQUE, PRIMARY KEY (id_rol)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS usuario (id_usuario INT NOT NULL AUTO_INCREMENT, id_rol INT NOT NULL, cedula VARCHAR(20) UNIQUE, nombre VARCHAR(100) NOT NULL, apellidos VARCHAR(150), correo VARCHAR(150) NOT NULL UNIQUE, contrasena VARCHAR(255) NOT NULL, telefono VARCHAR(20), direccion TEXT, estado TINYINT(1) NOT NULL DEFAULT 1, PRIMARY KEY (id_usuario), FOREIGN KEY (id_rol) REFERENCES rol(id_rol)) ENGINE=InnoDB;

-- Genesis / Catalogo e inventario (HU06-HU10)
CREATE TABLE IF NOT EXISTS categoria (id_categoria INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL UNIQUE, descripcion VARCHAR(255), activo TINYINT(1) NOT NULL DEFAULT 1, PRIMARY KEY (id_categoria)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS producto (id_producto INT NOT NULL AUTO_INCREMENT, id_categoria INT NOT NULL, nombre VARCHAR(150) NOT NULL UNIQUE, descripcion TEXT, precio DECIMAL(10,2) NOT NULL, stock INT NOT NULL DEFAULT 0, estado TINYINT(1) NOT NULL DEFAULT 1, ruta_imagen VARCHAR(1024), destacado TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY (id_producto), FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria), CHECK (precio >= 0), CHECK (stock >= 0)) ENGINE=InnoDB;

-- Cesar / Carrito y pedidos (HU11-HU15)
-- Un carrito ABIERTO por cliente; al confirmar pasa a FINALIZADO y se crea el pedido.
CREATE TABLE IF NOT EXISTS carrito (id_carrito INT NOT NULL AUTO_INCREMENT, id_usuario INT NOT NULL, fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, estado VARCHAR(30) NOT NULL DEFAULT 'ABIERTO', PRIMARY KEY (id_carrito), FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS detalle_carrito (id_detalle_carrito INT NOT NULL AUTO_INCREMENT, id_carrito INT NOT NULL, id_producto INT NOT NULL, cantidad INT NOT NULL, PRIMARY KEY (id_detalle_carrito), UNIQUE KEY uq_detalle_carrito (id_carrito, id_producto), FOREIGN KEY (id_carrito) REFERENCES carrito(id_carrito), FOREIGN KEY (id_producto) REFERENCES producto(id_producto), CHECK (cantidad > 0)) ENGINE=InnoDB;

-- Cristopher / Gestion y seguimiento (HU16-HU20)
-- Cesar crea los pedidos; Cristopher los consulta y cambia su estado.
CREATE TABLE IF NOT EXISTS estado_pedido (id_estado_pedido INT NOT NULL AUTO_INCREMENT, nombre VARCHAR(50) NOT NULL UNIQUE, PRIMARY KEY (id_estado_pedido)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS pedido (id_pedido INT NOT NULL AUTO_INCREMENT, id_usuario INT NOT NULL, id_estado_pedido INT NOT NULL, fecha_pedido DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, total DECIMAL(10,2) NOT NULL, PRIMARY KEY (id_pedido), FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario), FOREIGN KEY (id_estado_pedido) REFERENCES estado_pedido(id_estado_pedido), CHECK (total >= 0)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS detalle_pedido (id_detalle_pedido INT NOT NULL AUTO_INCREMENT, id_pedido INT NOT NULL, id_producto INT NOT NULL, cantidad INT NOT NULL, precio DECIMAL(10,2) NOT NULL, PRIMARY KEY (id_detalle_pedido), FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido), FOREIGN KEY (id_producto) REFERENCES producto(id_producto), CHECK (cantidad > 0), CHECK (precio >= 0)) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS notificacion (id_notificacion INT NOT NULL AUTO_INCREMENT, id_usuario INT NOT NULL, id_pedido INT NOT NULL, mensaje VARCHAR(255) NOT NULL, fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, leida TINYINT(1) NOT NULL DEFAULT 0, PRIMARY KEY (id_notificacion), FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario), FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Datos de referencia para levantar catalogo y pantallas
-- ---------------------------------------------------------------------
INSERT INTO rol (id_rol, nombre) VALUES (1,'ADMIN'), (2,'ENCARGADO'), (3,'CLIENTE');
INSERT INTO estado_pedido (id_estado_pedido, nombre) VALUES (1,'PENDIENTE'), (2,'PREPARANDO'), (3,'ENTREGADO');
INSERT INTO categoria (id_categoria, nombre, descripcion, activo) VALUES (1,'Alimentos','Alimentos para perros',1),(2,'Juguetes','Articulos para entretener perros',1),(3,'Paseo','Correas y accesorios de paseo',1),(4,'Higiene','Productos de higiene',1),(5,'Camas','Descanso y comodidad',1);
INSERT INTO producto (id_producto, id_categoria, nombre, descripcion, precio, stock, estado, ruta_imagen, destacado) VALUES
(1,1,'Alimento Adulto 15 kg','Alimento seco para perros adultos',28500.00,12,1,'https://placehold.co/600x400/png?text=Alimento',1),
(2,3,'Correa resistente','Correa para paseo diario',8900.00,16,1,'https://placehold.co/600x400/png?text=Correa',1),
(3,2,'Juguete mordedor','Juguete para perros',4500.00,25,1,'https://placehold.co/600x400/png?text=Juguete',1),
(4,4,'Champu para perros','Producto de higiene canina',6200.00,8,1,'https://placehold.co/600x400/png?text=Higiene',0),
(5,5,'Cama mediana','Cama para perros medianos',19900.00,6,1,'https://placehold.co/600x400/png?text=Cama',0);
