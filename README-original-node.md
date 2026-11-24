# Microservicios de Ferretería Los Maestros

Backend separado del proyecto React, hecho con **Node.js + Express**: 7 microservicios independientes que se comunican entre sí por HTTP. Los datos viven **en memoria** (no hay base de datos): al reiniciar un servicio vuelve a su estado inicial.

## Cómo ejecutarlo

Requiere Node.js 18 o superior. Desde la carpeta `microservicios`:

```bash
npm install
npm run todos        # levanta los 7 servicios a la vez
```

O desde la raíz del proyecto React: `npm run microservicios:instalar` y luego `npm run microservicios`.

Para levantar uno solo: `npm run inventario`, `npm run catalogo`, `npm run ventas`, `npm run pagos`, `npm run usuarios`, `npm run notificaciones`, `npm run proveedores`. Ojo: casi todos necesitan a Usuarios encendido (valida las sesiones), y Catálogo, Ventas y Compras necesitan a Inventario.

Para probar a mano abre **`solicitudes.http`** en VS Code (extensión *REST Client*) y presiona "Send Request" sobre cada petición. Para comprobar que todo funciona junto: `npm test` (levanta los 7 servicios en los puertos 4001-4007 y recorre los flujos principales).

## Servicios y puertos

| Servicio | Puerto | Qué hace |
|---|---:|---|
| Inventario | 3001 | Productos y stock (fuente de verdad), stock mínimo, alertas, movimientos |
| Catálogo | 3002 | Vitrina pública: productos, categorías, marcas, búsqueda, fotos |
| Ventas | 3003 | Registrar ventas, total, comprobantes, estado del pedido |
| Pagos | 3004 | Registrar y validar pagos, métodos de pago |
| Usuarios y autenticación | 3005 | Registro, sesión, roles, permisos, recuperar contraseña |
| Contacto y notificaciones | 3006 | Formulario de contacto y envío de correos |
| Proveedores y compras | 3007 | Proveedores, órdenes de compra, recepción de mercadería |

Todos responden `GET /estado`.

## Cómo se conectan

- **Todos → Usuarios:** cada servicio valida el token de la sesión preguntándole a Usuarios (`POST /autenticacion/verificar`). Por eso, al cerrar sesión, el token deja de servir en todo el sistema.
- **Catálogo → Inventario:** el catálogo lee los productos y precios del inventario, así siempre muestra lo mismo que se vende.
- **Ventas → Inventario:** al registrar una venta, Inventario valida y descuenta el stock en una sola operación (si algún producto no alcanza, no se descuenta nada) y devuelve los precios vigentes. El cliente nunca envía precios. Si la venta se anula, el stock vuelve.
- **Pagos → Ventas:** el pago consulta la venta (debe estar pendiente y el monto debe ser el total) y, si se aprueba, la deja `PAGADA`.
- **Proveedores → Inventario:** al recibir mercadería suma el stock y actualiza el costo de la última compra.
- **Inventario → Notificaciones:** cuando un producto queda en o bajo su stock mínimo se envía **una** alerta (se rearma cuando se repone).
- **Ventas → Notificaciones:** confirmación de la venta y aviso en cada cambio de estado del pedido.
- **Usuarios → Notificaciones:** correo de recuperación de contraseña.
- Si un servicio no responde, quien lo necesita contesta `503` con un mensaje claro.

## Usuarios, roles y permisos

Al iniciar se crean dos cuentas de prueba (la de administración se puede cambiar con `ADMIN_CORREO` y `ADMIN_CONTRASENA`):

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | `admin@ferreteria.cl` | `Admin123!` |
| CLIENTE | `cliente@ferreteria.cl` | `Cliente123!` |

Se inicia sesión en `POST /autenticacion/inicio-sesion`, que entrega un token. Se envía en cada petición: `Authorization: Bearer <token>`.

| Rol | Puede |
|---|---|
| **CLIENTE** | Ver el catálogo, registrar ventas, pagar sus ventas, ver **solo sus** ventas y pagos, escribir por contacto, editar su perfil |
| **ADMIN** | Todo: inventario, ver todas las ventas y pagos, métodos de pago, usuarios y roles, proveedores y compras, consultas de contacto e historial de correos |

Quien se registra por `POST /autenticacion/registro` siempre queda como CLIENTE; solo un ADMIN puede crear cuentas con otro rol. Tras 5 intentos fallidos de inicio de sesión la cuenta se bloquea 15 minutos. Las contraseñas se guardan con hash (bcrypt).

## Endpoints

`Público` = sin sesión. `Sesión` = cualquier usuario con token. `ADMIN` = solo administración. `Interno` = lo usan los otros servicios.

### Inventario (3001) — todo ADMIN
| Método y ruta | Descripción |
|---|---|
| `POST /productos` | Registrar producto |
| `GET /productos` | Listar (`?categoria=`, `?texto=`, `?soloStockBajo=true`) |
| `GET /productos/:codigo` | Detalle con indicadores de stock |
| `PUT /productos/:codigo` | Modificar datos o precios (el stock se cambia por su propia ruta) |
| `DELETE /productos/:codigo` | Eliminar |
| `GET /productos/:codigo/stock` | Consultar stock disponible |
| `PUT /productos/:codigo/stock` | Ajuste manual de stock `{ stock, motivo }` (queda en el historial) |
| `PUT /productos/:codigo/stock-minimo` | Controlar stock mínimo `{ stockMinimo }` |
| `GET /stock/bajo` | Productos con poco stock (los más críticos primero) |
| `GET /stock/resumen` | Totales y valor del inventario |
| `POST /stock/verificar-alertas` | Reenvía las alertas de todos los productos con stock bajo |
| `GET /movimientos` | Historial de movimientos (`?codigo=`, `?tipo=VENTA\|COMPRA\|ANULACION\|AJUSTE`) |
| `POST /stock/descontar` · `/stock/reponer` · `/stock/ingresar` | Operaciones atómicas que usan Ventas y Compras |

### Catálogo (3002) — público
| Método y ruta | Descripción |
|---|---|
| `GET /productos` | Listar. Filtros: `texto`, `categoria`, `subcategoria`, `marca`, `precioMin`, `precioMax`, `disponible=true`, `orden=nombre\|marca\|precio_asc\|precio_desc` |
| `GET /productos/buscar?texto=` | Buscar (ignora tildes y mayúsculas: "caneria" encuentra "Cañería") |
| `GET /productos/categoria/:categoria` | Productos de una categoría |
| `GET /productos/:codigo` | Detalle: precio, marca, imagen y descripción |
| `GET /categorias` · `GET /marcas` | Categorías (con subcategorías) y marcas, con cantidad de productos |
| `GET /imagenes/:archivo` | Fotos (carpeta `public/productos` del proyecto React, o la que indiques en `CARPETA_IMAGENES`; si no existe, el catálogo devuelve la ruta original) |
| `PUT /productos/:codigo/descripcion` | (ADMIN) Editar la descripción |

No expone precio de compra ni stock exacto; solo `disponible: true/false`.

### Ventas (3003)
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /ventas` | Sesión | Registrar venta `{ items: [{ codigo, cantidad }] }`. Calcula neto, IVA y total. ADMIN puede agregar `cliente: { nombre, correo }` (venta en mostrador) |
| `GET /ventas` | Sesión | Ventas anteriores (cliente: las suyas; ADMIN: todas, con `?estado=`, `?idCliente=`, `?desde=`, `?hasta=`) |
| `GET /ventas/:id` · `GET /ventas/cliente/:idCliente` | Sesión | Detalle / ventas de un cliente |
| `GET /ventas/:id/comprobante` | Sesión | Comprobante en texto (`?formato=json` para los datos) |
| `POST /ventas/:id/anular` | Sesión | El cliente anula su venta si aún no está pagada; devuelve el stock |
| `PUT /ventas/:id/estado` | ADMIN | Cambia el estado del pedido `{ estado, motivo }` |

Estados: `PENDIENTE_PAGO → PAGADA → EN_PREPARACION → LISTA_PARA_RETIRO → ENTREGADA` (o `ANULADA`). Cada cambio le llega al cliente por correo.

### Pagos (3004)
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /pagos` | Sesión | Registrar pago `{ idVenta, metodo, tarjeta?, referencia? }` |
| `POST /pagos/validar` | Sesión | Revisa si el pago sería aceptado, sin registrarlo |
| `GET /pagos` · `GET /pagos/:id` · `GET /pagos/venta/:idVenta` | Sesión | Pagos realizados (cliente: los suyos; ADMIN: todos, con `?estado=`, `?metodo=`) |
| `PUT /pagos/:id/estado` | ADMIN | Confirmar o rechazar un pago pendiente |
| `GET /metodos-pago` | Público | Métodos activos |
| `GET /metodos-pago/todos` · `POST /metodos-pago` · `PUT /metodos-pago/:codigo` | ADMIN | Administrar métodos de pago |

Es una pasarela **simulada** (no se conecta a ningún banco):
- **Tarjeta** (`TARJETA_DEBITO`, `TARJETA_CREDITO`): se valida el número (Luhn), el vencimiento (`MM/AA`) y el CVV. Prueba aprobada: `4111 1111 1111 1111`. Prueba rechazada por el banco: `4000 0000 0000 0002`. Un pago rechazado queda registrado y la venta sigue pendiente. No se guarda el número completo, solo los últimos 4 dígitos.
- **Transferencia**: queda `PENDIENTE` (exige `referencia`) hasta que un ADMIN la confirma.
- **Efectivo**: si lo registra un ADMIN en caja queda `APROBADO`; si lo elige el cliente queda `PENDIENTE` hasta confirmarlo.

### Usuarios y autenticación (3005)
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /autenticacion/registro` | Público | Registrar usuario (contraseña: mínimo 8 caracteres, con letras y números) |
| `POST /autenticacion/inicio-sesion` | Público | Iniciar sesión → token |
| `POST /autenticacion/cierre-sesion` | Sesión | Cerrar sesión |
| `POST /autenticacion/recuperar` | Público | Envía por correo un código de recuperación (30 min) |
| `POST /autenticacion/restablecer` | Público | Cambia la contraseña con el código `{ token, contrasenaNueva }` |
| `GET` · `PUT /autenticacion/perfil` | Sesión | Ver / editar el perfil |
| `POST /autenticacion/verificar` | Interno | Lo usan los otros servicios para validar tokens |
| `GET /roles` · `GET /usuarios` · `GET /usuarios/:id` | ADMIN | Roles con sus permisos y usuarios |
| `PUT /usuarios/:id/rol` · `PUT /usuarios/:id/estado` | ADMIN | Cambiar rol / activar o desactivar |

### Contacto y notificaciones (3006)
| Método y ruta | Quién | Descripción |
|---|---|---|
| `POST /contacto` | Público | Formulario de contacto `{ nombre, correo, telefono?, asunto, mensaje }` (registra la consulta y envía confirmación) |
| `GET /contacto` · `GET /contacto/:id` | ADMIN | Consultas recibidas (`?estado=RECIBIDA\|LEIDA\|RESPONDIDA`) |
| `PUT /contacto/:id` | ADMIN | Marcar como leída o responder por correo `{ respuesta }` |
| `GET /notificaciones` · `GET /notificaciones/:id` | ADMIN | Historial de correos (`?tipo=`, `?para=`) |
| `POST /notificaciones/confirmacion-venta` · `/alerta-stock` · `/cambio-pedido` · `/correo` | Interno | Los disparan los otros servicios |

**Correos:** sin configuración funcionan en modo **simulado**: se muestran en la consola y quedan en `GET /notificaciones` (ahí puedes ver, por ejemplo, el código de recuperación de contraseña). Para enviarlos de verdad define `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS` y `CORREO_REMITENTE`.

### Proveedores y compras (3007) — todo ADMIN
| Método y ruta | Descripción |
|---|---|
| `POST /proveedores` · `PUT /proveedores/:id` | Registrar / modificar proveedor (valida el RUT con su dígito verificador) |
| `GET /proveedores` · `GET /proveedores/:id` · `GET /proveedores/:id/compras` | Consultar |
| `POST /ordenes-compra` | Registrar orden `{ idProveedor, items: [{ codigo, cantidad, costoUnitario? }] }` (valida los productos en Inventario; costos netos, se suma IVA) |
| `GET /ordenes-compra` · `GET /ordenes-compra/:id` | Consultar (`?estado=`, `?idProveedor=`) |
| `POST /ordenes-compra/:id/recepcion` | Registrar recepción de mercadería, total o parcial → **actualiza el Inventario** |
| `POST /ordenes-compra/:id/cancelar` | Cancelar una orden sin recepciones |
| `GET /historial-compras` | Historial de compras recibidas (`?idProveedor=`, `?codigo=`, `?desde=`, `?hasta=`) |

Hay dos proveedores de ejemplo (`id` 1 y 2) para probar de inmediato.

## Flujo de ejemplo: una compra completa

1. `POST :3005/autenticacion/inicio-sesion` como cliente → token.
2. `GET :3002/productos/buscar?texto=taladro` → elegir un producto.
3. `POST :3003/ventas` con `items` → venta `PENDIENTE_PAGO` (el stock ya se descontó y llega un correo de confirmación).
4. `POST :3004/pagos` con la tarjeta de prueba → pago `APROBADO` y venta `PAGADA`.
5. `GET :3003/ventas/1/comprobante` → comprobante.
6. Como ADMIN: `PUT :3003/ventas/1/estado` → `EN_PREPARACION`, `LISTA_PARA_RETIRO`, `ENTREGADA` (el cliente recibe un correo en cada paso).

`solicitudes.http` trae este flujo y todos los demás listos para ejecutar.

## Variables de entorno (todas opcionales)

| Variable | Para qué | Por defecto |
|---|---|---|
| `PUERTO_INVENTARIO`, `PUERTO_CATALOGO`, `PUERTO_VENTAS`, `PUERTO_PAGOS`, `PUERTO_USUARIOS`, `PUERTO_NOTIFICACIONES`, `PUERTO_PROVEEDORES` | Cambiar puertos | 3001 a 3007 |
| `CLAVE_INTERNA` | Clave de confianza entre servicios | valor de desarrollo |
| `JWT_SECRETO`, `DURACION_SESION` | Firma y duración de los tokens | valor de desarrollo, `2h` |
| `ADMIN_CORREO`, `ADMIN_CONTRASENA` | Cuenta de administración inicial | `admin@ferreteria.cl`, `Admin123!` |
| `CORREO_ADMIN` | Quién recibe alertas de stock y mensajes de contacto | `admin@ferreteria.cl` |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASS`, `CORREO_REMITENTE` | Correos reales | modo simulado |

Todos los servicios deben arrancar con los mismos valores de `PUERTO_*`, `CLAVE_INTERNA` y `JWT_SECRETO`: si los cambias, defínelos en el entorno antes de `npm run todos`.

## Estructura

```
microservicios/
├── compartido/        Código común: configuración, auth, validaciones, cliente HTTP, comprobante
├── datos/             Catálogo inicial (85 productos, el mismo del proyecto React)
├── inventario/ catalogo/ ventas/ pagos/ usuarios/ notificaciones/ proveedores/   (un index.js cada uno)
├── pruebas/           Prueba de punta a punta (npm test)
└── solicitudes.http   Peticiones de ejemplo para REST Client
```

## Conectar el frontend

La aplicación React sigue funcionando sola con `npm run dev`. Para conectarla, reemplaza el contenido de `src/services/productRepository.ts` por llamadas `fetch` al Catálogo (lectura pública, `:3002`) y al Inventario (CRUD de administración, `:3001`, con el token en `Authorization`). Los servicios ya permiten CORS.

## Límites conocidos

- Datos en memoria: se pierden al reiniciar (no hay base de datos).
- Los folios de venta pueden saltarse un número si una venta falla justo después de reservarlo.
- La pasarela de pagos y el envío de correos por defecto son simulados.
- Las claves por defecto son solo para desarrollo: cámbialas si lo publicas en algún lugar.
