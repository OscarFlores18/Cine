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

Orden de arranque: registry -> configServer -> Auth, Productos, Salas, Ventas, Gmail -> Gateway (y AdminServer).

Cada microservicio de dominio tiene las capas: controller / service / repository / model / dto,
mas config (GatewayInterceptor) y exception.

Variables de entorno:
- `JWT_SECRET` (base64, `openssl rand -base64 32`): en **Gateway** y **Auth**.
- `GMAIL_USERNAME` y `GMAIL_APP_PASSWORD` (contrasena de aplicacion de Google): solo en **Gmail**.

El repo de configuracion (`cine-properties`) lo lee el Config Server; la URI esta en
`configServer/src/main/resources/application.yml`.

Usuario de prueba (Auth): `admin` / `admin123`.
