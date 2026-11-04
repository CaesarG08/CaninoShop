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
- Spring Security con doble autenticacion (2FA)
- Maven

---

## Como ejecutar el proyecto

### Requisitos

- JDK 21
- Maven (o NetBeans con soporte para Spring Boot)
- MySQL 8

### 1. Crear la base de datos

Ejecutar `src/main/resources/creaTablas.sql` en MySQL Workbench con un usuario administrador. El script crea la base `caninoshop`, el usuario de la aplicacion, todas las tablas y datos de prueba (roles, estados de pedido, categorias y productos). Se puede ejecutar mas de una vez.

### 2. Configurar la clave de la doble autenticacion

La aplicacion no inicia sin la variable de entorno `CANINOSHOP_2FA_KEY`, una clave aleatoria de 32 bytes en Base64.

Para generarla en PowerShell:

```powershell
$b = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Fill($b); [Convert]::ToBase64String($b)
```

En Linux o macOS: `openssl rand -base64 32`.

Se define como variable de entorno del sistema o de la sesion antes de iniciar la aplicacion. Si se cambia la clave, los usuarios que ya activaron el 2FA deben volver a configurarlo.

### 3. Ejecutar

```
mvn spring-boot:run
```

La aplicacion queda en http://localhost:82. Los usuarios se crean en `/registro/nuevo`; al iniciar sesion por primera vez se activa el 2FA con una aplicacion como Google Authenticator o Microsoft Authenticator.

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
