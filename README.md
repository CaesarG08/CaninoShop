# CaninoShop

Tienda web de insumos caninos desarrollada con Java + Spring Boot.

**SC-403 Desarrollo de Aplicaciones Web y Patrones**  
Universidad Fidelitas - Tercer cuatrimestre 2026

---

## Integrantes

| Nombre | GitHub | Modulo | Rama |
|---|---|---|---|
| Cesar Luis Mejia Gonzalez | [@CaesarG08](https://github.com/CaesarG08) | Carrito y pedidos | `feature/carrito` |
| Carlos Eduardo Chavarria Jimenez | [@carloschavarria80109](https://github.com/carloschavarria80109) | Usuarios y seguridad | `feature/usuarios` |
| Genesis Dayana Castillo Lara | [@Gen1818](https://github.com/Gen1818) | Catalogo e inventario | `feature/catalogo` |
| Cristopher Josue Murillo Granados | [@CristopherMurillo](https://github.com/CristopherMurillo) | Gestion y seguimiento | `feature/gestion` |

---

## Tecnologias

- Java 21 + Spring Boot
- Thymeleaf + Bootstrap 5
- MySQL / Hibernate JPA
- Firebase Storage
- Maven

---

## Como ejecutar el proyecto

Requisitos: Java 21, Maven y MySQL 8.

1. Crear la base de datos: abrir MySQL con un usuario administrador y ejecutar el script
   `src/main/resources/caninoshop.sql`. Crea la base `caninoshop`, el usuario de la aplicacion
   (`usuario_caninoshop`), las tablas y los datos de referencia (roles, estados de pedido,
   categorias y productos). Se puede ejecutar mas de una vez sin borrar datos.
2. Los datos de conexion estan en `src/main/resources/application.properties`
   (por defecto `localhost:3306/caninoshop`).
3. Ejecutar el proyecto desde NetBeans o con `mvn spring-boot:run`. La aplicacion queda en
   `http://localhost:82`.
4. Crear una cuenta desde "Crear cuenta". Los usuarios se crean desde la aplicacion porque la
   contrasena se guarda encriptada.

---

## Acuerdo de trabajo por ramas

1. Cada integrante trabaja en su propia rama (`feature/modulo`).
2. Nadie hace push directo a `main`.
3. Para integrar cambios a `main` se crea un Pull Request.
4. Al menos un integrante debe revisar y aprobar el PR antes del merge.
5. Commits descriptivos: `feat: agregar carrito`, `fix: validacion de stock`, etc.
6. Antes de crear un PR, verificar que el codigo compila correctamente.

---

## Estado del proyecto

- [x] Avance 1 - Historias de usuario y prototipo (Semana 5)
- [ ] Avance 2 - Implementacion funcional 50% (Semana 9)
- [ ] Entrega final (Semana 15)
