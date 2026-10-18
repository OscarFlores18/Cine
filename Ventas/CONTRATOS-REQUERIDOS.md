# Contratos entre Ventas y el resto de los microservicios

> Reescrito desde cero: las versiones anteriores de este documento estaban
> basadas en los diagramas de clases/secuencia del Figma, que NO coincidían
> con lo que el equipo de Sala/Productos/Auth terminó implementando. Esta
> version esta chequeada contra el codigo real de `Cine-Backend-main`
> (Salas, Productos, Auth, Gateway) al momento de escribirla.

## 1) MS Sala (`modulo-salas` en Eureka)

Ventas usa `SalaClient` (Feign, solo lectura):

| Metodo | Endpoint real | Uso |
|---|---|---|
| `obtenerFuncion(id)` | `GET /funciones/{id}` | precio base, salaId, nombreSala |
| `obtenerSala(id)` | `GET /salas/{id}` | nombre/tipo de sala para la factura |
| `obtenerButacasPorSala(salaId)` | `GET /butacas/sala/{salaId}` | **unica** forma de leer butacas — no existe un GET por id singular |

**Cambio de estado de butaca (ocupar al pagar / liberar al cancelar): YA NO es Feign.**
Sala no expone ningun endpoint de escritura pensado para este flujo (su
`PATCH /butacas/{id}/estado` es una accion administrativa). En su lugar,
Ventas publica un evento async a RabbitMQ que el propio `VentasConsumer` de
Sala ya tiene implementado y escuchando:

- Exchange: `exchange.ventas` (direct) — declarado por Sala (dueña de la cola
  y el binding); Ventas solo redeclara el exchange (idempotente) para poder
  publicar aunque arranque antes que Sala.
- Routing key: `routing.butacas`
- Cola: `queue.butacas.actualizar` (dueña: Sala)
- Payload: `{ "butacaId": <Long>, "nuevoEstado": "DISPONIBLE" | "OCUPADO" }`

Esto YA FUNCIONA de punta a punta con el `VentasConsumer` real de Sala —
no hace falta que el equipo de Sala cambie nada mas.

## 2) MS Productos (`modulo-productos` en Eureka)

Ventas usa `ProductoClient`:

| Metodo | Endpoint real | Uso |
|---|---|---|
| `obtenerProducto(id)` | `GET /productos/{id}` | precio y stock real de confiteria |

Productos **no tiene** entidad Pelicula ni rutas `/confiteria`. Por eso el
titulo de la pelicula para la factura ya no se pide por Feign: el front lo
manda como texto informativo en `VentaAltaDTO.peliculaTitulo` (no se valida
contra ningun microservicio, es solo para mostrar en el mail).

## 3) MS Auth (`auth` en Eureka)

Ventas usa `AuthClient` (nuevo):

| Metodo | Endpoint real | Uso |
|---|---|---|
| `obtenerUsuario(username)` | `GET /auth/usuarios/{username}` | nombre/apellido reales del comprador, para la factura |

Este endpoint es nuevo en Auth (ver `AuthController.obtenerUsuario`), pensado
para trafico service-to-service (no pasa por el Gateway en este flujo:
Feign llama directo via Eureka). Degrada a `null` si Auth no responde — no
bloquea la confirmacion de una venta ya pagada.

Tambien relevante para el equipo de front/Auth: ahora existe
`POST /auth/registro-cliente` (permitAll, siempre crea rol CLIENTE) para que
un visitante se registre solo, sin depender de que un ADMIN le cree la
cuenta (que sigue siendo el unico camino para `POST /auth/register`, con
`hasRole("ADMIN")`, para crear EMPLEADO/ADMIN).

## 4) MS Factura (nuevo microservicio, decision del profesor) y MS Email

**Cambio de arquitectura**: la factura ya no es "solo un evento que arma
Ventas y consume Email" — el profesor pidio que sea un microservicio
aparte, con su propia base, para separar de verdad esa responsabilidad.
Se creo `Factura` (Eureka: `modulo-factura`):

- Escucha el mismo exchange `venta.eventos` (topic) / routing key
  `venta.confirmada` que ya publicaba Ventas, pero con su PROPIA cola
  (`cola.factura`) — un exchange topic puede tener varias colas bindeadas a
  la misma routing key, asi que Ventas no tuvo que cambiar nada para esto.
- Persiste la factura como recurso propio (tabla `factura`, con su
  snapshot de butacas/items — no referencia en vivo a Venta/Sala/Productos).
- Resuelve nombre/apellido/email del comprador contra Auth
  (`GET /auth/usuarios/{username}`, que ahora tambien devuelve `email`).
- Le manda el mail de confirmacion a MS Gmail (`POST /gmail/enviar`,
  que ya existia) con el resumen armado. Si el envio falla, la factura
  queda persistida igual (es "mejor esfuerzo", igual que el resto de las
  integraciones opcionales de este proyecto).
- Expone `GET /facturas/mias`, `GET /facturas/{id}`, `GET /facturas/venta/{ventaId}`.

Con esto, **MS Email deja de necesitar su propio consumer de RabbitMQ**:
sigue siendo un servicio simple de "mandame este mail" (`EmailController`
actual, sin cambios), y quien decide CUANDO mandar el mail de factura y
QUE dice es Factura, no Email. Esto es mas simple para el equipo de Email
que lo que se habia documentado antes (ya no hace falta que agreguen un
`@RabbitListener` ni una copia de `VentaConfirmadaEvent`).

Exchange `venta.eventos` (topic), igual que antes — Ventas sigue
publicando los dos routing keys:

- `venta.confirmada` -> colas `cola.confirmacion-venta` (si algun dia hace
  falta) y `cola.factura` (la usa Factura) -> payload `VentaConfirmadaEvent`
- `venta.cancelada` -> cola `cola.cancelacion-venta` -> payload `VentaCanceladaEvent`

**`VentaConfirmadaEvent` cambio de forma** para soportar comprar varias
butacas en una sola compra y mostrar el nombre real del comprador:

```json
{
  "idVenta": 123,
  "username": "wquispe",
  "nombreCompleto": "Walter Quispe Pretel",
  "funcionId": 101,
  "numeroCompra": "V-123",
  "fecha": "2026-10-16T21:00:00",
  "peliculaTitulo": "Minions: Nace un Villano",
  "salaNombre": "Sala 1 - IMAX",
  "salaTipo": "IMAX",
  "funcionFechaHora": "2026-10-16T21:00:00",
  "butacas": ["J6", "J7", "J8"],
  "entradas": [{"nombre": "Entrada (butacas J6, J7, J8)", "cantidad": 3, "precioUnitario": 8500, "subtotal": 25500}],
  "productos": [{"nombre": "Combo Mega Pochoclo", "cantidad": 1, "precioUnitario": 14500, "subtotal": 14500}],
  "subtotalEntradas": 25500,
  "subtotalProductos": 14500,
  "total": 40000
}
```

## 5) MS Gateway

Cambio necesario (ya aplicado en este zip) en `GatewaySecurityConfig`:
`POST /ventas/webhook/**` tiene que estar `permitAll()` — lo llama Mercado
Pago directamente, sin JWT nuestro. Sin este cambio el webhook nunca llega
a confirmar ningun pago (quedaba tapado por la regla general
`hasAnyRole("ADMIN","EMPLEADO")` de `/ventas/**`).

## 6.5) Pendiente del lado de infra: config repo (ver carpeta `Cine-config-repo/`)

El Gateway de este proyecto NO define sus rutas en codigo Java: vienen del
Config Server, que las clona de un repo de GitHub propio del equipo (no
alcanzable desde donde corri esto). El repo que me pasaron (zip) estaba
incompleto — le faltaba `gateway.internal-secret` para casi todos los
microservicios (sin eso ni arrancaban) y no tenia NINGUNA ruta de Gateway.
Deje todo completado en la carpeta `Cine-config-repo/` de este mismo zip
(con un README explicando que agregue y por que); alguien del equipo tiene
que fusionar eso a mano con el repo real de GitHub — no es algo que yo
pueda pushear desde aca.

## 6) Compra de varias butacas en una sola venta

`Venta` dejo de tener un `butacaId` propio: ahora tiene una lista
`butacas: List<VentaButaca>` (mismo patron que `venta_producto`). El front
manda `VentaAltaDTO.butacaIds: List<Long>` con todas las butacas elegidas en
un mismo checkout; Ventas las valida todas contra `GET /butacas/sala/{id}`,
crea una fila `VentaButaca` por cada una, y al confirmar el pago publica un
evento de ocupacion por butaca (ver seccion 1). El total se calcula como
`precioBase * cantidad de butacas + confiteria`.
