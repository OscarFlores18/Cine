# escuelatp-front

Front de **escuelatp** en Angular 16 + Bootstrap 5 + Bootstrap Icons. Habla solo con el Gateway (`http://localhost:8081`, se cambia en `src/app/config.ts`).

## Cómo correrlo

El front **solo habla con el Gateway** (`http://localhost:8081`, se cambia en `src/app/config.ts`), así que **sin el backend levantado no hay login ni datos**. Primero el backend, después el front.

### Opción 1: todo con Docker (recomendada)

Con Docker Desktop abierto, seguir el README del repo [escuelatp](https://github.com/maximiliano-occhiuzzi/escuelatp): `docker compose up -d` levanta backend **y** este front (con nginx) en http://localhost:4200. No hace falta instalar Node.

### Opción 2: solo el front, para desarrollo

Requiere **Node 18 o superior** (probado con Node 22) y el backend ya levantado (Opción 1 de arriba o desde el IDE).

```bash
npm install
ng serve          # http://localhost:4200
```

El Gateway ya permite CORS desde el puerto 4200.

## Usuarios de prueba (los crea el backend al arrancar)

| Usuario | Clave | Rol |
|---|---|---|
| root | root123 | ROOT |
| director | director123 | DIRECTOR |
| preceptor | preceptor123 | PRECEPTOR |
| docente | docente123 | DOCENTE (personalId 1) |

## Qué ve cada rol

| Pantalla | ROOT | DIRECTOR | PRECEPTOR | DOCENTE |
|---|:-:|:-:|:-:|:-:|
| Listado de alumnos | ✔ | ✔ | ✔ | ✔ |
| Alta de alumno / Deshabilitar | | | ✔ | |
| Listado de cursos | ✔ (todos) | ✔ (todos) | ✔ (todos) | ✔ (solo los suyos) |
| Alta de curso | ✔ | | ✔ | |
| Notas de un curso (ver) | | ✔ | | ✔ |
| Cargar / modificar / borrar notas | | | | ✔ (solo sus cursos) |
| Listado de personal | ✔ | ✔ | ✔ | |
| Alta y edición de personal | ✔ | | ✔ | |
| Registrar usuarios | ✔ | | | |

Es la misma matriz que aplica el Gateway: el front solo oculta lo que no corresponde, la seguridad real son los 403 del backend.

## Cómo está armado

```
src/app/
├── config.ts                 URL del Gateway
├── models/                   interfaces (coinciden con los DTOs del backend)
├── service/
│   ├── auth.service.ts       login, refresh, logout, rol, hasRole()
│   └── servicios.service.ts  llamadas REST de alumnos, cursos, notas y personal
├── auth/
│   ├── auth.guard.ts         exige sesión
│   ├── role.guard.ts         exige rol (lee data.roles de la ruta)
│   ├── has-role.directive.ts *hasRole="['PRECEPTOR']"
│   ├── auth.interceptor.ts   agrega el JWT y renueva con el refresh token
│   ├── login/ y register/
├── Alumnos/ Cursos/ Personal/   list-* y add-*  (Cursos además tiene notas-curso/)
└── shared/                   mensaje-error.ts (lee el ApiError del backend), curso-util.ts, loading-overlay
```

Conceptos de Angular que se usan: formularios reactivos (validaciones, validador de horario en el curso, `personalId` obligatorio solo para DOCENTE), observables (`loggedIn$` con `async`, `valueChanges`, `forkJoin`), guards funcionales, directiva estructural propia, interceptor HTTP.

## Cómo se protege una ruta

```ts
{ path: 'cursoForm', component: AddCursoComponent,
  canActivate: [authGuard, roleGuard], data: { roles: ['ROOT', 'PRECEPTOR'] } }
```

Y un elemento suelto en un template:

```html
<button *hasRole="['PRECEPTOR']">Deshabilitar</button>
```
