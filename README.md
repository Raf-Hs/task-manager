# Task Manager

Aplicación web completa para administrar tareas personales: crear, consultar,
editar, completar y eliminar tareas, con prioridades, búsqueda, filtros, orden,
paginación y estadísticas. Validación en frontend y en backend, y persistencia
real en PostgreSQL.

Proyecto de práctica pensado para explicar y defender en una entrevista
técnica: código pequeño, legible y con una arquitectura que se puede ampliar.
Cada decisión importante está escrita en un
[ADR](docs/adr/README.md), con el contexto, las alternativas y las
consecuencias.

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, PostgreSQL, Maven, JUnit 5 + Mockito, springdoc (Swagger) |
| Frontend | React 18, TypeScript, Vite, Axios, CSS puro (responsive) |
| Infraestructura | Docker Compose (PostgreSQL), Git, variables de entorno |

---

## 1. Qué hace

- **Listado paginado** de tareas, con filtros por estado y por prioridad.
- **Búsqueda por texto** en el título y en la descripción, sin distinguir
  mayúsculas.
- **Orden configurable**: fecha de creación, fecha de modificación, fecha
  límite, título o prioridad. Las tareas completadas van siempre al final.
- **Panel de estadísticas**: total, pendientes, completadas, vencidas y
  porcentaje de completitud.
- **Alta y edición** con un formulario validado en el cliente y en el servidor.
- **Completar y reabrir** una tarea con un clic.
- **Eliminar** con confirmación previa.
- **Mensajes de éxito y de error** después de cada operación.
- **Estados de carga, de lista vacía y de error** diferenciados.
- **Diseño responsive** para móvil, tableta y escritorio.

La lista completa de funcionalidades, con sus criterios de aceptación, está en
[`docs/SCOPE.md`](docs/SCOPE.md).

---

## 2. Documentación

| Documento | Contenido |
| --- | --- |
| [`docs/SCOPE.md`](docs/SCOPE.md) | Alcance, personas usuarias, historias de usuario y criterios de aceptación. Es la fuente de verdad del proyecto. |
| [`docs/API.md`](docs/API.md) | Referencia de los siete endpoints, con ejemplos y códigos de error. |
| [`docs/DATABASE.md`](docs/DATABASE.md) | Modelo de datos, índices, consultas y copias de seguridad. |
| [`docs/adr/`](docs/adr/README.md) | Once registros de decisiones de arquitectura. |
| [`CHANGELOG.md`](CHANGELOG.md) | Qué ha cambiado en cada versión. |
| [`CONTRIBUTING.md`](CONTRIBUTING.md) | Cómo preparar el entorno y cómo proponer un cambio. |
| [`SECURITY.md`](SECURITY.md) | Cómo informar de una vulnerabilidad. |

---

## 3. Requisitos previos

| Herramienta | Versión mínima | Comprobación |
| --- | --- | --- |
| JDK | 21 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| Node.js | 18 (recomendado 20+) | `node -v` |
| npm | 9+ | `npm -v` |
| Docker + Docker Compose | reciente | `docker compose version` |
| Git | cualquiera | `git --version` |

> Docker es necesario **solo** para PostgreSQL. Si ya tienes PostgreSQL
> instalado en local, puedes saltarte el paso 5 y configurar las variables
> `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con tus propias credenciales.

---

## 4. Puesta en marcha rápida

```bash
# 1. Variables de entorno (los valores por defecto ya sirven en local)
cp .env.example .env

# 2. Base de datos
docker compose up -d

# 3. Backend (en una terminal)
cd backend && mvn spring-boot:run

# 4. Frontend (en otra terminal)
cd frontend && npm install && npm run dev
```

- Aplicación: <http://localhost:5173>
- API: <http://localhost:8080/api/tasks>
- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

Los detalles de cada paso están en las secciones siguientes.

---

## 5. Estructura del proyecto

```
task-manager/
├── backend/                          # API REST (Spring Boot)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/taskmanager/
│       │   │   ├── TaskManagerApplication.java
│       │   │   ├── config/          # CorsConfig, OpenApiConfig
│       │   │   ├── controller/      # TaskController (endpoints REST)
│       │   │   ├── dto/             # TaskRequest, TaskResponse, TaskFilter,
│       │   │   │                    # TaskPriority, TaskSortOption,
│       │   │   │                    # TaskStatistics, PagedResponse, ApiError…
│       │   │   ├── entity/          # Task (JPA)
│       │   │   ├── exception/       # TaskNotFoundException, GlobalExceptionHandler
│       │   │   ├── mapper/          # TaskMapper (entidad <-> DTO)
│       │   │   ├── repository/      # TaskRepository, TaskSpecifications
│       │   │   └── service/         # TaskService, TaskServiceImpl
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           ├── java/com/taskmanager/  # TaskServiceImplTest, TaskControllerTest,
│           │                          # TaskRepositoryTest
│           └── resources/application-test.yml
├── frontend/                         # UI (React + TypeScript + Vite)
│   ├── index.html
│   ├── vite.config.ts
│   ├── package.json
│   └── src/
│       ├── components/              # TaskForm, TaskList, TaskItem, TaskFilters,
│       │                            # TaskSearch, TaskSort, TaskStats,
│       │                            # Pagination, ConfirmDialog, NotificationList
│       ├── hooks/                   # useTasks, useNotifications, useDebouncedValue
│       ├── pages/                   # TasksPage
│       ├── services/                # api.ts (Axios), taskService.ts
│       ├── types/                   # task.ts
│       ├── utils/                   # dateUtils.ts
│       ├── test/                    # setup.ts
│       ├── App.tsx
│       ├── main.tsx
│       └── index.css
├── docs/
│   ├── SCOPE.md
│   ├── API.md
│   ├── DATABASE.md
│   └── adr/                         # Un fichero por decisión
├── scripts/
│   ├── api-smoke-test.sh            # Comprueba cada endpoint contra el backend
│   └── e2e-test.sh                  # Flujo completo: frontend -> backend -> PostgreSQL
├── docker-compose.yml               # PostgreSQL 17
├── .env.example                     # Plantilla de variables de entorno
├── CHANGELOG.md
├── CONTRIBUTING.md
├── SECURITY.md
├── LICENSE
└── README.md
```

---

## 6. Variables de entorno

El repositorio **no contiene credenciales reales**. Los valores por defecto son
locales y están documentados en `.env.example`.

```bash
cp .env.example .env      # Linux / macOS / Git Bash
copy .env.example .env    # Windows CMD
```

`.env` está en el `.gitignore`, así que nunca se sube al repositorio.

### Backend

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskdb` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `taskuser` | Usuario de la base de datos |
| `DB_PASSWORD` | `taskpassword` | Contraseña de la base de datos |
| `SERVER_PORT` | `8080` | Puerto de la API |
| `DDL_AUTO` | `update` | `update` en desarrollo, `validate` en producción |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Orígenes permitidos (separados por comas) |

### Frontend

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `VITE_API_BASE_URL` | *(vacío)* | Ruta base de la API. Si se deja vacía se usa el proxy de Vite (`/api`) |

> Las variables de Vite se leen **en tiempo de compilación**. Si cambias
> `VITE_API_BASE_URL`, reinicia `npm run dev`.

---

## 7. Base de datos

```bash
docker compose up -d          # arranca PostgreSQL 17
docker compose ps             # estado
docker compose logs -f postgres
docker compose down           # detiene y conserva los datos
docker compose down -v        # detiene y BORRA los datos
```

El contenedor publica el puerto `5432` en el host, crea la base de datos
`taskdb` y el usuario `taskuser`. El `healthcheck` confirma que PostgreSQL
acepta conexiones antes de considerarlo listo.

**Verificación manual:**

```bash
docker compose exec postgres psql -U taskuser -d taskdb -c "\d tasks"
```

El esquema lo crea Hibernate al arrancar (`ddl-auto=update`). El modelo
completo, con el motivo de cada tipo y cada restricción, está en
[`docs/DATABASE.md`](docs/DATABASE.md).

---

## 8. Ejecutar el backend

```bash
cd backend

# Con las variables por defecto (coinciden con docker-compose.yml)
mvn spring-boot:run

# O con variables de entorno propias
DB_URL=jdbc:postgresql://localhost:5432/taskdb \
DB_USERNAME=taskuser \
DB_PASSWORD=taskpassword \
mvn spring-boot:run

# Empaquetar el JAR ejecutable
mvn clean package
java -jar target/task-manager-backend-1.0.0.jar
```

### Endpoints

Todos cuelgan de `/api/tasks`.

| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| `GET` | `/api/tasks` | Página de tareas, con filtros, búsqueda, orden y paginación | `200` · `PagedResponse<TaskResponse>` · `400` |
| `GET` | `/api/tasks/{id}` | Obtiene una tarea | `200` · `404` |
| `GET` | `/api/tasks/statistics` | Cifras agregadas de toda la lista | `200` |
| `POST` | `/api/tasks` | Crea una tarea | `201` + `Location` · `400` |
| `PUT` | `/api/tasks/{id}` | Actualiza una tarea | `200` · `400` · `404` |
| `PATCH` | `/api/tasks/{id}/complete` | Alterna el estado, o lo fija con un cuerpo | `200` · `404` |
| `DELETE` | `/api/tasks/{id}` | Elimina una tarea | `204` · `404` |

### Parámetros del listado

| Parámetro | Valores | Por defecto | Descripción |
| --- | --- | --- | --- |
| `status` | `ALL`, `PENDING`, `COMPLETED` | `ALL` | Filtra por estado. |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | *(sin filtro)* | Filtra por prioridad. |
| `search` | texto | *(sin filtro)* | Busca en el título y la descripción. |
| `sort` | `CREATED_AT`, `UPDATED_AT`, `DUE_DATE`, `TITLE`, `PRIORITY` | `CREATED_AT` | Criterio de orden. |
| `page` | número | `0` | Página, empezando por 0. |
| `size` | número | `20` | Tamaño de la página; máximo `100`. |

Los valores de `status`, `priority` y `sort` se aceptan en mayúsculas o en
minúsculas, y se combinan entre sí con **AND**:

```bash
curl "http://localhost:8080/api/tasks?status=PENDING&priority=high&search=leche&sort=title"
```

`PATCH /api/tasks/{id}/complete` **alterna** el estado si se llama sin cuerpo.
Si se envía un cuerpo, fija el estado de forma explícita:

```json
{ "completed": true }
```

### Modelo de una tarea

```json
{
  "id": 1,
  "title": "Comprar leche",
  "description": "Desnatada",
  "dueDate": "2030-01-15",
  "priority": "MEDIUM",
  "completed": false,
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-01T10:00:00Z"
}
```

| Campo | Tipo | Obligatorio | Notas |
| --- | --- | --- | --- |
| `id` | number | generado | Auto-incremental |
| `title` | string | **sí** | Máximo 200 caracteres, no puede estar en blanco |
| `description` | string \| null | no | Máximo 2000 caracteres; una cadena en blanco se guarda como `null` |
| `dueDate` | string \| null | no | `YYYY-MM-DD`; puede estar en pasado (la tarea queda *vencida*) |
| `priority` | string | no | `LOW`, `MEDIUM` o `HIGH`; por defecto `MEDIUM` |
| `completed` | boolean | no | Por defecto `false` |
| `createdAt` | string | generado | ISO-8601 en UTC |
| `updatedAt` | string | generado | ISO-8601 en UTC; cambia en cada modificación |

### Formato de una página

```json
{
  "content": [ /* tareas */ ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

### Formato de error

Todos los errores devuelven la misma estructura:

```json
{
  "status": 400,
  "message": "Validation failed for: title",
  "path": "/api/tasks",
  "timestamp": "2026-01-01T10:00:00Z",
  "fieldErrors": {
    "title": "The title is mandatory"
  }
}
```

La referencia completa de cada endpoint, con todas sus respuestas, está en
[`docs/API.md`](docs/API.md).

---

## 9. Ejecutar el frontend

```bash
cd frontend

npm install
npm run dev
```

- Aplicación: <http://localhost:5173>

El servidor de Vite hace de proxy de `/api` hacia `http://localhost:8080`
(ver `vite.config.ts`), por lo que en desarrollo no hay problemas de CORS.

### Otros comandos

| Comando | Qué hace |
| --- | --- |
| `npm run build` | Compila TypeScript y genera el bundle de producción en `dist/` |
| `npm run preview` | Sirve localmente el build de producción |
| `npm run lint` | ESLint |
| `npm run typecheck` | Comprobación de tipos sin emitir ficheros |
| `npm test` | Pruebas unitarias (Vitest), una sola ejecución |
| `npm run test:watch` | Pruebas en modo watch |

---

## 10. Ejecutar las pruebas

### Backend (JUnit 5 + Mockito)

```bash
cd backend
mvn test
```

El perfil de test usa **H2 en memoria**, así que la suite se ejecuta en
cualquier máquina **sin Docker ni PostgreSQL**:

| Suite | Qué cubre |
| --- | --- |
| `TaskServiceImplTest` | Reglas de negocio con el repositorio mockeado: CRUD, alternar estado, estadísticas, tarea inexistente |
| `TaskControllerTest` | Capa REST con `MockMvc`: códigos HTTP, contrato JSON, validación, filtros, parámetros inválidos, 404 y `Location` en el 201 |
| `TaskRepositoryTest` | Persistencia, filtros, búsqueda y **orden** contra una base de datos real (H2) |

**62 pruebas**, 3 suites.

### Frontend (Vitest + React Testing Library)

```bash
cd frontend
npm test
```

| Fichero | Qué cubre |
| --- | --- |
| `taskService.test.ts` | URLs, parámetros y cuerpos de cada llamada HTTP |
| `useTasks.test.tsx` | Estados de carga, guardado y error; recarga al cambiar filtros, orden o página |
| `useDebouncedValue.test.tsx` | Retardo de la búsqueda |
| `TaskForm.test.tsx` | Validación del formulario y datos enviados |
| `TaskItem.test.tsx` | Insignias de prioridad, *Overdue* y *Completed*, y botones |
| `TaskList.test.tsx` | Estados de carga, error y vacío |
| `TaskSearch.test.tsx`, `TaskSort.test.tsx`, `Pagination.test.tsx` | Controles de búsqueda, orden y paginación |
| `TaskStats.test.tsx` | Panel de estadísticas |
| `TasksPage.test.tsx` | Página completa: crear, editar, completar, borrar con confirmación, filtrar y paginar |
| `dateUtils.test.ts` | Helpers de fecha |

**96 pruebas**, 12 ficheros.

### Scripts de verificación end-to-end

Con el backend, el frontend y PostgreSQL levantados:

```bash
bash scripts/api-smoke-test.sh   # 50 comprobaciones sobre la API
bash scripts/e2e-test.sh         # 26 comprobaciones del flujo completo
```

Ambos scripts crean sus propias tareas y las borran al terminar, así que se
pueden ejecutar tantas veces como se quiera.

---

## 11. Decisiones técnicas

Un resumen; el razonamiento completo está en [`docs/adr/`](docs/adr/README.md).

**Backend**

- **Arquitectura por capas** (`controller` → `service` → `repository`): cada
  capa tiene una responsabilidad y se puede probar por separado. El
  controlador no contiene lógica de negocio ([ADR-0001](docs/adr/0001-layered-architecture.md)).
- **DTOs en los bordes de la API**: la entidad JPA nunca sale del servicio, así
  que el modelo de datos puede cambiar sin romper el contrato HTTP
  ([ADR-0002](docs/adr/0002-dtos-on-api-boundaries.md)).
- **Inyección de dependencias por constructor**: las dependencias son
  explícitas, `final` y fáciles de sustituir en los tests
  ([ADR-0004](docs/adr/0004-constructor-injection.md)).
- **Bean Validation** (`@NotBlank`, `@Size`) en el DTO de entrada: las reglas
  se declaran una sola vez y Spring las aplica automáticamente.
- **`@RestControllerAdvice`** para centralizar los errores y devolver siempre
  el mismo formato `ApiError`.
- **Filtros, orden y paginación en la base de datos**, con
  `JpaSpecification`: el número de filas que viajan por la red está acotado por
  la página, no por el tamaño de la tabla
  ([ADR-0010](docs/adr/0010-pagination-and-filtering-in-the-repository.md)).
- **Credenciales por variables de entorno**, con valores de desarrollo
  documentados en `.env.example`
  ([ADR-0005](docs/adr/0005-environment-variables-for-secrets.md)).
- **H2 en memoria para los tests**, para que la suite no dependa de Docker
  ([ADR-0006](docs/adr/0006-h2-for-tests.md)).

**Frontend**

- **TypeScript estricto**: los errores de la API se detectan al compilar.
- **Las llamadas HTTP fuera de los componentes**: `services/taskService.ts`
  concentra el acceso a la API y `hooks/useTasks.ts` el estado. Los componentes
  solo renderizan y despachan acciones
  ([ADR-0007](docs/adr/0007-custom-hook-over-query-library.md)).
- **Un hook propio en lugar de TanStack Query o SWR**: con una sola lista y una
  sola caché no compensa la dependencia, y el hook deja ver el ciclo
  `cargando → datos → error` sin caja negra
  ([ADR-0011](docs/adr/0011-no-react-query-client.md)).
- **Validación en dos capas**: el formulario valida para dar feedback
  inmediato, y el backend valida de nuevo porque el cliente siempre se puede
  saltar ([ADR-0003](docs/adr/0003-validation-in-client-and-server.md)).
- **Vite con proxy `/api`**: en desarrollo no hay CORS ni URLs absolutas
  ([ADR-0008](docs/adr/0008-vite-proxy-for-development.md)).

**Base de datos**

- **Hibernate `ddl-auto=update`** solo en desarrollo. El paso a Flyway con
  `validate` está decidido y documentado en
  [ADR-0009](docs/adr/0009-flyway-over-ddl-auto.md).

---

## 12. Estado y trabajo futuro

Lo que está hecho se puede consultar en el [`CHANGELOG.md`](CHANGELOG.md).
Lo que viene después, por orden de prioridad:

1. **Migraciones con Flyway** y `ddl-auto=validate`, para que el esquema deje
   de depender del arranque de la aplicación (ADR-0009).
2. **Tests de integración con Testcontainers** contra un PostgreSQL real, para
   validar el SQL que Hibernate genera y que H2 no siempre reproduce.
3. **`Dockerfile` multi-stage** para el backend y el frontend, y un
   `docker-compose` completo con los tres servicios.
4. **Integración continua**: compilar, pasar las dos suites y los scripts en
   cada *push*.
5. **Rutas en el frontend** (`react-router`) para que los filtros vivan en la
   URL y se puedan compartir.
6. **Modo oscuro** y persistencia de los filtros entre sesiones.
7. **Autenticación y usuarios**: cada tarea asociada a una persona
   (`Spring Security` + JWT). La tabla `tasks` ganaría la columna `user_id`.

Nada de esto hace falta para usar la aplicación: son los siguientes pasos
razonables, no deuda técnica urgente.

---

## 13. Contribuir

Las instrucciones para preparar el entorno, el estilo de los commits y el
proceso de revisión están en [`CONTRIBUTING.md`](CONTRIBUTING.md).

Para informar de un problema de seguridad, sigue lo indicado en
[`SECURITY.md`](SECURITY.md).

---

## 14. Licencia

MIT. Ver [`LICENSE`](LICENSE).
