# Cine - Microservicios (Spring Cloud)

| Modulo | Nombre en Eureka | Puerto | Ruta por el Gateway |
|---|---|---|---|
| registry | eurekaserver | 8761 | - |
| configServer | configserver | 8888 | - |
| AdminServer | adminserver | 8090 | - |
| Gateway | gateway | 8081 | (punto de entrada) |
| Productos | modulo-productos | 8082 | /productos/** |
| Salas | modulo-salas | 8083 | /salas/** |
| Ventas | modulo-ventas | 8084 | /ventas/** |
| Gmail | modulo-gmail | 8085 | /gmail/** |
| Auth (usuarios) | auth | 8086 | /auth/** |
| Factura | modulo-factura | 8087 | /facturas/** |

Orden de arranque: registry -> configServer -> Auth, Productos, Salas, Ventas, Gmail, Factura -> Gateway (y AdminServer).

Cada microservicio de dominio tiene las capas: controller / service / repository / model / dto,
mas config (GatewayInterceptor) y exception.

**Factura** es un microservicio nuevo (decision pedida por el profe: una factura
real, no solo un evento). No tiene POST propio: escucha por RabbitMQ el evento
`venta.confirmada` que publica Ventas (exchange `venta.eventos`, cola propia
`cola.factura`) y guarda la factura. Solo expone lectura (`GET /facturas/mias`,
`GET /facturas/{id}`).

**Gmail** manda el mail de confirmacion de forma totalmente independiente de
Factura: escucha el MISMO evento `venta.confirmada` pero con su PROPIA cola
(`cola.confirmacion-venta`, la que Ventas ya declaraba pensada para esto). Un
exchange topic puede tener varias colas bindeadas a la misma routing key —
Factura y Gmail reciben cada uno su copia del mismo mensaje, sin que Ventas
ni Factura tengan que saber que el otro existe (0 llamadas Feign entre ellos).
El evento solo trae el `username` del comprador, asi que Gmail resuelve el
mail real pegandole a Auth. Tiene reintento con backoff (3 intentos) y una
cola de descarte (`cola.confirmacion-venta.dlq`) para los mensajes que fallan
en serio — configurable en `modulo-gmail-*.yml`. Para auditar que mails
salieron: `GET /gmail/historial`.

**Productos** es el modulo real de confiteria (candy bar): alta, baja logica
(no se borra fisico, se marca `activo=false`), modificacion, ajuste de stock
(`PATCH /{id}/stock`), reactivacion, listado paginado con filtros
(`categoria`, `nombre`, `incluirInactivos`) y `GET /bajo-stock` para alertar
cuando queda poco. Tiene un campo `imagenUrl` (opcional, solo la URL, no se
almacena el archivo) que se muestra en la pantalla publica de candy; si no
se carga, el frontend cae a un emoji segun la categoria. El contrato que ya
usaba Ventas (`ProductoDTO`: id, nombre, precio, stock) es compatible sin
cambios — es un subconjunto estricto de los campos reales. Siembra 6
productos de ejemplo al arrancar si la tabla esta vacia.

**Peliculas** (cartelera) vive en el mismo microservicio (`/productos/peliculas`,
mismo ruteo `/productos/**` del Gateway, no necesito ruta nueva). CRUD
completo: alta, modificacion, baja logica, `GET /productos/peliculas`
(solo activas, para la cartelera publica) y `GET /productos/peliculas/todas`
(incluye las de baja, para la pantalla de administracion). No tiene relacion
formal con `Funcion` (en Salas) — Funcion solo guarda `peliculaId` como un
Long suelto sin validarlo via Feign, para no acoplar los dos modulos.
Tambien tiene su campo de imagen (`poster`, URL) y `fechaEstreno` (fecha de
estreno en el cine). Siembra las 3 peliculas que antes eran mock fijo en el
frontend.

Pantallas de administracion en el frontend (`/admin/peliculas`,
`/admin/confiteria`, `/admin/salas` y `/admin/funciones`, solo visibles y
accesibles para el rol ADMIN — logica duplicada por las dudas: el Gateway
rechaza la escritura igual si alguien intenta pegarle directo a la API):
alta, edicion y baja de peliculas, productos de confiteria, salas y
funciones (eligiendo pelicula, sala, fecha/hora y precio — el precio de la
entrada vive en la Funcion, no en la Pelicula, porque varia segun el tipo de
sala: 2D/3D/IMAX/VIP pueden tener distinto precio para la misma pelicula).

El microservicio de Salas siembra 3 salas (IMAX, 3D y VIP, cada una con su
mapa de butacas ya generado) y varias funciones repartidas en hoy/mañana/el
proximo finde para las 3 peliculas de ejemplo, asi la cartelera no arranca
vacia esperando que alguien cargue datos a mano.

La cartelera ya no filtra por botones HOY/MAÑANA/SAB-DOM (confundia que
habia que entender el filtro): ahora para la pelicula elegida se listan
directamente TODAS las funciones futuras, ordenadas por fecha y agrupadas
bajo un titulo legible ("Hoy", "Mañana", "Sábado 24/10", etc.) — no hay
nada para elegir, solo scrollear.

El flujo de compra completo es: elegir pelicula y funcion en la cartelera
-> elegir butacas (mapa de asientos) -> confiteria (opcional: se puede
elegir snacks o apretar "Siguiente" sin elegir nada) -> checkout final
("Revisa tu compra", con el desglose de entradas + confiteria + total) ->
confirmar y pagar. El historial (`/historial`, "Mis compras") muestra por
cada compra: pelicula, butacas, fecha, el detalle de la confiteria
comprada (si hubo), el desglose entradas/confiteria/total y, si la compra
quedo sin link de pago real de Mercado Pago, un aviso + el link para
completar el pago si existe.

El selector de "cine" (sucursal) que tenia el frontend original era
decorativo — no existe ningun concepto de sucursal/ubicacion en el backend
(las Salas no pertenecen a ningun Cine), asi que se saco para no mostrar una
opcion que no filtraba nada. Si el proyecto necesita multi-sucursal de
verdad, es una entidad nueva a agregar (Cine 1---N Sala) — no esta
implementada en esta entrega.

**Salas** ahora es el modulo real (Sala + Funcion + Butaca, con MySQL en el
perfil docker) en vez del esqueleto original. El cambio de estado de una
butaca (ocuparla al confirmar el pago, liberarla al cancelar) no es un
Feign sincronico: Ventas publica un evento async a RabbitMQ
(exchange `exchange.ventas` / routing key `routing.butacas`) que el
`VentasConsumer` de Salas escucha.

CRUD de Sala/Funcion (todos requieren rol ADMIN salvo lectura, que es publica):
- **Sala**: POST (crea + autogenera el mapa de butacas) / GET (lista, por id) /
  **PUT** (solo nombre y tipo — cambiar filas/asientos rompería butacas ya
  vendidas, por eso no se puede editar eso) / DELETE.
- **Funcion**: POST / GET (cartelera, por sala, por id) / **PUT** (pelicula,
  fecha/hora, precio — no se puede cambiar de sala) / DELETE.
- **Butaca**: GET por sala / PATCH estado (la usa Ventas via el evento async,
  no se crean ni borran sueltas: nacen y mueren con su sala).

Variables de entorno:
- `JWT_SECRET` (base64, `openssl rand -base64 32`): en **Gateway** y **Auth**.
- `GMAIL_USERNAME` y `GMAIL_APP_PASSWORD` (contrasena de aplicacion de Google): solo en **Gmail**.
- Mercado Pago (`mercadopago.access-token`, configurable en `cine-properties`): sandbox token para que
  **Ventas** pueda generar preferencias de pago reales. Sin eso, o si Mercado Pago rechaza la
  solicitud (cuenta sin verificar, token restringido, politicas de la cuenta, servicio caido, etc.),
  la venta se crea igual en estado `PENDIENTE` sin link de pago real — ya no se deshace la compra
  entera por un problema de Mercado Pago. El frontend (`/historial`) avisa cuando una compra quedo
  sin link de pago.

El repo de configuracion (`cine-properties`) lo lee el Config Server; la URI esta en
`configServer/src/main/resources/application.yml`.

Usuarios de prueba (Auth, se siembran solos si la base esta vacia):
- `admin` / `admin123` (ADMIN)
- `empleado` / `empleado123` (EMPLEADO)
- `cliente` / `cliente123` (CLIENTE)
