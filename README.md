# Microservicios de Ferretería Los Maestros — Spring Boot

7 microservicios Java 17+ / Spring Boot 4.0.9 / Maven. Cada carpeta genera un JAR independiente con su propia base de datos, su documentación Swagger y seguridad JWT por roles. El frontend React (`ferreteria-los-maestros`) los consume a través del proxy de Vite.

## Servicios

| Servicio | Puerto | Qué hace | Swagger |
|---|---:|---|---|
| inventario | 3001 | Productos, precios y stock (fuente de verdad). CRUD + operaciones atómicas de stock | http://localhost:3001/swagger-ui.html |
| catalogo | 3002 | Vitrina pública: lee los productos de **inventario** y les agrega la descripción comercial | http://localhost:3002/swagger-ui.html |
| ventas | 3003 | Registra ventas: **inventario** descuenta stock y entrega precios; calcula neto/IVA/total | http://localhost:3003/swagger-ui.html |
| pagos | 3004 | Pasarela simulada: valida la venta en **ventas** y la marca `PAGADA` | http://localhost:3004/swagger-ui.html |
| usuarios | 3005 | Registro (BCrypt), inicio de sesión (emite el **JWT**), perfil, roles | http://localhost:3005/swagger-ui.html |
| notificaciones | 3006 | Formulario de contacto e historial de correos | http://localhost:3006/swagger-ui.html |
| proveedores | 3007 | Proveedores y órdenes de compra | http://localhost:3007/swagger-ui.html |

Todos responden `GET /estado` (público).

## Requisitos
Java 17 o superior (probado con 21) y Maven 3.9+. MySQL es opcional.

## Compilar y ejecutar
```bash
mvn clean package
java -jar usuarios/target/usuarios-1.0.0.jar        # 3005  (emite los tokens)
java -jar inventario/target/inventario-1.0.0.jar    # 3001
java -jar catalogo/target/catalogo-1.0.0.jar        # 3002  (necesita inventario)
java -jar notificaciones/target/notificaciones-1.0.0.jar  # 3006
# opcionales: ventas (3003), pagos (3004), proveedores (3007)
```
En IntelliJ / VS Code también se puede ejecutar la clase `*Application` de cada módulo.

Luego, en el proyecto React: `npm run dev` → http://localhost:5173

## Cuentas de prueba (las crea la migración V2 de usuarios)

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | `admin@ferreteria.cl` | `Admin123!` |
| CLIENTE | `cliente@ferreteria.cl` | `Cliente123!` |

## Base de datos y migraciones (Flyway)

Cada servicio tiene su esquema versionado en `src/main/resources/db/migration`:

| Servicio | Migraciones |
|---|---|
| inventario | `V1__crear_tabla_producto.sql`, `V2__cargar_productos_iniciales.sql` (85 productos) |
| catalogo | `V1__crear_tabla_descripcion_producto.sql`, `V2__cargar_descripciones_iniciales.sql` |
| ventas | `V1__crear_tablas_venta.sql` (venta + venta_item) |
| pagos | `V1__crear_tabla_pago.sql` |
| usuarios | `V1__crear_tabla_usuario.sql`, `V2__crear_usuarios_de_prueba.sql` |
| notificaciones | `V1__crear_tabla_notificacion.sql` |
| proveedores | `V1__crear_tablas_proveedores.sql`, `V2__cargar_proveedores_iniciales.sql` |

- Al arrancar, Flyway aplica las migraciones pendientes y las registra en la tabla `flyway_schema_history`. Hibernate **no** modifica el esquema (`ddl-auto=none`).
- Para cambiar el esquema se agrega un archivo nuevo (`V3__...sql`); nunca se edita uno ya aplicado.
- Por defecto se usa **H2 en archivo** (`./data/db/<servicio>`), sin instalar nada. Para empezar de cero, borra la carpeta `data/`.
- Con **MySQL** (Laragon/XAMPP): `--spring.profiles.active=mysql`. Las bases `ferreteria_<servicio>` se crean solas; usuario/clave con `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` (por defecto root/root).

## Seguridad (JWT + roles)

1. `POST http://localhost:3005/autenticacion/inicio-sesion` con `{ "correo", "contrasena" }` → `{ token, expiraEn, usuario }`.
2. En cada petición protegida: `Authorization: Bearer <token>`.
3. Cada servicio valida el token **localmente** (firma HMAC-SHA256 con la clave compartida `JWT_SECRETO`, vencimiento y emisor) y lee el claim `rol`.

| Rol | Puede |
|---|---|
| (sin sesión) | Ver el catálogo, enviar el formulario de contacto, registrarse, ver métodos de pago |
| CLIENTE | Lo anterior + registrar ventas, pagar y ver **solo sus** ventas/pagos, ver su perfil |
| ADMIN | Todo: CRUD de inventario, todas las ventas y pagos, usuarios y roles, proveedores, consultas de contacto |
| SERVICIO | Token técnico que usan los microservicios entre sí (catálogo→inventario, ventas→inventario, pagos→ventas) |

Respuestas: `401` sin token o token inválido/vencido · `403` sin el rol necesario · errores de negocio en JSON `{ "error", "estado", "fecha" }` (400 datos inválidos, 404 no existe, 409 conflicto, 503 otro servicio caído).

**Swagger con token:** abre el Swagger de usuarios, ejecuta *inicio-sesion*, copia el `token`, y en el Swagger de otro servicio pulsa **Authorize** y pégalo.

Variables de entorno: `JWT_SECRETO` (mín. 32 caracteres, **la misma en los 7 servicios**), `JWT_DURACION_MINUTOS` (120), `CORS_ORIGENES`, `PORT_<SERVICIO>`, `URL_INVENTARIO`, `URL_VENTAS`.

## Estructura (igual en todos los servicios)

```
<servicio>/src/main/java/cl/ferreteria/<servicio>/
├── controller/   Rutas HTTP (+ anotaciones Swagger). Reciben DTOs y delegan en el service
├── service/      Lógica de negocio y validaciones (@Transactional)
├── repository/   Spring Data JPA
├── model/        Entidades JPA (mapeadas a las tablas de Flyway)
├── dto/          Requests y responses (records)
└── config/       SecurityConfig: reglas de acceso por rol
<servicio>/src/main/resources/db/migration/   Migraciones Flyway
```

El módulo `compartido` contiene: `JwtConfig` (validación/emisión JWT, CORS), `SeguridadBase` (reglas comunes), `GeneradorTokens`, `ClienteInterno` (RestClient con timeout y token de servicio), `OpenApiConfig` (Swagger + bearerAuth) y `ApiErrorHandler`.

## Probar a mano
`solicitudes.http` (extensión REST Client de VS Code o el cliente HTTP de IntelliJ) recorre el flujo completo: login → CRUD de inventario → venta → pago.

## Pendiente / mejoras
- Recepción de órdenes de compra → sumar stock en inventario (`/stock/reponer` ya existe).
- Lista de revocación de tokens (hoy el cierre de sesión es del lado del cliente; el token vence solo).
- Gateway único (Spring Cloud Gateway) en vez del proxy de Vite.
