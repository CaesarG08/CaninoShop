# CaninoShop

Aplicación web para la venta y administración de productos para perros.

**SC-403 Desarrollo de Aplicaciones Web y Patrones**  
Universidad Fidelitas - Tercer cuatrimestre 2026

## Equipo y módulos

| Integrante | Módulo | Rama de trabajo |
|---|---|---|
| César Luis Mejía González | Carrito y pedidos | `feature/carrito` |
| Carlos Eduardo Chavarría Jiménez | Usuarios y seguridad | `feature/usuarios` |
| Génesis Dayana Castillo Lara | Catálogo e inventario | `feature/catalogo` |
| Cristopher Josué Murillo Granados | Gestión y seguimiento | `feature/gestion` |

## Tecnologías y organización

- Java 21 y Spring Boot
- Spring MVC, Spring Security y validación
- Thymeleaf y Bootstrap 5 mediante WebJars
- MySQL y Hibernate/JPA
- Maven
- Capas `controller`, `service`, `repository` y `domain`

## Requisitos

- JDK 21
- Maven instalado
- MySQL 8 en ejecución

## Instalación y ejecución

1. Clona el repositorio o descarga el proyecto.
2. En MySQL Workbench, con una cuenta administradora, ejecuta el script completo `src/main/resources/caninoshop.sql`. El script crea la base `caninoshop`, el usuario de conexión, las tablas y datos iniciales para roles, estados de pedido, categorías y productos. No crea usuarios de prueba.
3. Revisa `src/main/resources/application.properties`. La URL, el usuario y la contraseña de conexión deben coincidir con la configuración de tu MySQL y con el usuario creado por el script. Si cambias las credenciales, actualízalas en ambos lugares. Usa credenciales locales de desarrollo; no reutilices contraseñas de producción.
4. Desde la carpeta raíz del proyecto, ejecuta:

   ```text
   mvn spring-boot:run
   ```

   También puedes abrirlo como proyecto Maven en NetBeans y ejecutarlo desde allí.
- Java 21 + Spring Boot
- Thymeleaf + Bootstrap 5
- MySQL / Hibernate JPA
- Maven

5. Abre [http://localhost:82](http://localhost:82). Crea una cuenta desde **Crear cuenta**; el registro público asigna el rol `CLIENTE` y guarda la contraseña cifrada.

No se proporciona una cuenta preconfigurada de `ADMIN` o `ENCARGADO`. Para demostrar esos permisos, el equipo debe usar cuentas preparadas para la demostración y asignarles el rol correspondiente en la base de datos. No incluyas contraseñas reales en el repositorio.

## Funciones y acceso por rol

- **Visitante:** consulta el inicio y el catálogo público, busca por nombre o categoría y ve el detalle de un producto.
- **Cliente:** además de consultar el catálogo, agrega productos al carrito, revisa y confirma la compra y consulta sus pedidos.
- **Administrador:** administra categorías y productos y consulta pedidos recibidos; puede cambiar el estado de un pedido.
- **Encargado:** tiene acceso a la gestión de pedidos y a la pantalla de administración de productos. En la configuración actual, las acciones para guardar o desactivar productos están restringidas al administrador.

## Historias de usuario del Avance 2

El equipo seleccionó 10 historias para el alcance comprometido del Avance 2. La tabla también indica funcionalidades implementadas fuera de esa selección. Una historia solo se considera completada cuando su flujo satisface los criterios de aceptación y puede demostrarse con datos persistidos en MySQL.

| HU | Historia | Responsable | Estado |
|---|---|---|---|
| HU01 | Registro de clientes | Carlos | Seleccionada para Avance 2 |
| HU02 | Inicio de sesión | Carlos | Seleccionada para Avance 2 |
| HU03 | Gestión del perfil de usuario | Carlos | No seleccionada para Avance 2 |
| HU04 | Administración de roles de usuario | Carlos | No seleccionada para Avance 2 |
| HU05 | Acceso a funcionalidades según rol | Carlos | Implementada fuera de las 10 seleccionadas |
| HU06 | Consultar catálogo de productos | Génesis | Seleccionada para Avance 2 |
| HU07 | Buscar productos por nombre | Génesis | Implementada fuera de las 10 seleccionadas |
| HU08 | Productos destacados o más comprados | Génesis | No seleccionada para Avance 2 |
| HU09 | Administración de categorías | Génesis | Seleccionada para Avance 2 |
| HU10 | Actualización de existencias | Génesis | Seleccionada para Avance 2 |
| HU11 | Agregar productos al carrito | César | Seleccionada para Avance 2 |
| HU12 | Gestionar el carrito de compras | César | No seleccionada para Avance 2 |
| HU13 | Revisión previa del pedido | César | Seleccionada para Avance 2 |
| HU14 | Confirmación y registro del pedido | César | Seleccionada para Avance 2 |
| HU15 | Historial de pedidos | César | Implementada fuera de las 10 |
| HU16 | Administración de pedidos recibidos | Cristopher | Seleccionada para Avance 2 |
| HU17 | Actualizar estado del pedido | Cristopher | Seleccionada para Avance 2 |
| HU18 | Consulta y seguimiento de pedidos | Cristopher | No seleccionada para Avance 2 |
| HU19 | Notificaciones de pedidos | Cristopher | No seleccionada para Avance 2 |
| HU20 | Reportes de ventas | Cristopher | No seleccionada para Avance 2 |

**Aclaraciones del backlog:** HU10 tiene un título que no coincide con su descripción; el equipo la toma como actualización de existencias. HU15 no tiene una ficha completa de criterios de aceptación en el Avance 1, pero la funcionalidad de historial y detalle de pedidos está implementada en el proyecto.

## Recorrido principal de demostración

Registro e inicio de sesión → catálogo → agregar productos al carrito → revisión de compra → confirmación del pedido → gestión del pedido y cambio de estado. Los datos del recorrido se guardan en MySQL. El video de demostración se comparte mediante el enlace incluido en los comentarios de la entrega.

## Evidencia de colaboración

El trabajo se organizó por ramas funcionales. Cada integrante realizó commits en su rama y participó en pull requests con revisión antes de integrar cambios. El historial, las ramas y los pull requests se pueden consultar en el [repositorio de CaninoShop](https://github.com/CaesarG08/CaninoShop).

## Archivos que se entregan

El proyecto incluye el código fuente, `pom.xml`, este `README.md`, `.gitignore` y el script SQL necesario para crear la base de datos y sus datos iniciales. La carpeta `target` contiene archivos generados por Maven y se excluye del repositorio mediante `.gitignore`; no es necesaria para ejecutar el proyecto.

## Estado del avance

El Avance 2 se organiza alrededor de las diez historias seleccionadas arriba. El equipo debe confirmar que el video demuestra los criterios de aceptación de cada historia y que los cambios quedan persistidos en MySQL. Las historias no seleccionadas quedan fuera del alcance declarado para este avance; esto no implica que no tengan código implementado.
- [x] Avance 1 - Historias de usuario y prototipo (Semana 5)
- [x] Avance 2 - Implementacion funcional 50% (Semana 9)
- [ ] Entrega final (Semana 15)
