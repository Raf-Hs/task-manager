# Changelog

Todas las novedades de este proyecto se anotan en este fichero.

El formato sigue [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y
el versionado sigue [SemVer](https://semver.org/lang/es/).

Las fechas están en formato `AAAA-MM-DD`.

---

## [Sin publicar]

Todavía no hay nada publicado. Los cambios de esta sección se agrupan en la
siguiente versión cuando se decide publicarla.

### Añadido

- _Pendiente_

### Cambiado

- _Pendiente_

### Corregido

- _Pendiente_

### Eliminado

- _Pendiente_

---

## [0.1.0] - Sin publicar

Primera versión del proyecto: una API REST y una interfaz web para administrar
tareas personales.

### Añadido

**Backend**

- API REST CRUD sobre `/api/tasks`: listar, obtener, crear, actualizar,
  completar/descompletar y eliminar.
- Modelo de dominio `Task` con título, descripción, fecha límite, estado de
  completitud, prioridad y fechas de creación y modificación.
- Validación de la entrada con Bean Validation (`@NotBlank`, `@Size`), con
  mensajes por campo.
- Manejo centralizado de errores con `@RestControllerAdvice`, que devuelve
  siempre la misma estructura `ApiError` con el código HTTP correcto.
- Endpoint `GET /api/tasks/statistics` con el total, las pendientes, las
  completadas, las vencidas y el porcentaje de completitud.
- Listado con filtros por estado y por prioridad, búsqueda por texto, orden
  configurable y paginación, resueltos todos en la base de datos.
- CORS configurable por variable de entorno, con los orígenes de desarrollo por
  defecto.
- Documentación OpenAPI generada con springdoc, disponible en Swagger UI.

**Frontend**

- Interfaz React + TypeScript con el formulario de alta y edición, la lista
  de tareas, los filtros, el panel de estadísticas y los mensajes de éxito y
  error.
- Selector de prioridad y distintivo de prioridad en cada tarjeta.
- Caja de búsqueda con retardo (debounce) de 300 ms, selector de orden y
  controles de paginación.
- Confirmación antes de eliminar una tarea.
- Estado de carga, estado vacío y estado de error diferenciados.

**Infraestructura y documentación**

- `docker-compose.yml` con PostgreSQL 17 y su `healthcheck`.
- `.env.example` con todas las variables de entorno y sin credenciales reales.
- Scripts `api-smoke-test.sh` (49 comprobaciones) y `e2e-test.sh`
  (26 comprobaciones).
- `docs/SCOPE.md` con el alcance, las personas usuarias, las historias de
  usuario y los criterios de aceptación.
- `docs/adr/` con once registros de decisiones de arquitectura.
- `CONTRIBUTING.md`, `LICENSE` (MIT) y `SECURITY.md`.

### Decisiones

- Arquitectura por capas (`controller` → `service` → `repository`).
- DTOs en los bordes de la API: la entidad JPA nunca sale del service.
- Inyección de dependencias por constructor en todo el backend.
- Validación duplicada: el cliente da feedback inmediato y el servidor valida
  de nuevo porque siempre se puede saltarse el cliente.
- H2 en memoria para los tests, para que la suite no necesite Docker.
- Un hook propio (`useTasks`) en lugar de una librería de caché de queries.
- Proxy `/api` de Vite en desarrollo, para no pelearse con CORS.

[Sin publicar]: https://github.com/Raf-Hs/task-manager/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/Raf-Hs/task-manager/releases/tag/v0.1.0
