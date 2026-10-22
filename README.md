# Task Manager

Aplicación web completa para administrar tareas personales: crear, consultar,
editar, completar y eliminar tareas, con filtro por estado, validaciones en
both sides (frontend y backend) y persistencia real en PostgreSQL.

Proyecto de práctica pensado para explicar y defender en una entrevista
técnica: código pequeño, legible y con una arquitectura que se puede ampliar.

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 3.3, Spring Web, Spring Data JPA, Bean Validation, PostgreSQL, Maven, JUnit 5 + Mockito, springdoc (Swagger) |
| Frontend | React 18, TypeScript, Vite, Axios, CSS puro (responsive) |
| Infraestructura | Docker Compose (PostgreSQL), Git, variables de entorno |

---

## 1. Requisitos previos

| Herramienta | Versión mínima | Comprobación |
| --- | --- | --- |
| JDK | 21 | `java -version` |
| Maven | 3.9 | `mvn -version` |
| Node.js | 18 (recomendado 20+) | `node -v` |
| npm | 9+ | `npm -v` |
| Docker + Docker Compose | recente | `docker compose version` |
| Git | cualquiera | `git --version` |

> Docker es necesario **solo** para PostgreSQL. Si ya tienes PostgreSQL
> instalado en local, puedes saltarte el paso 4 y configurar las variables
> `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` con tus propias credenciales.

---

## 2. Estructura del proyecto

```
task-manager/
├── backend/                         # API REST (Spring Boot)
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/taskmanager/
│       │   │   ├── TaskManagerApplication.java
│       │   │   ├── config/          # CorsConfig, OpenApiConfig
│       │   │   ├── controller/      # TaskController (endpoints REST)
│       │   │   ├── dto/             # TaskRequest, TaskResponse, TaskStatus, ApiError…
│       │   │   ├── entity/          # Task (JPA)
│       │   │   ├── exception/       # TaskNotFoundException, GlobalExceptionHandler
│       │   │   ├── mapper/          # TaskMapper (entidad <-> DTO)
│       │   │   ├── repository/      # TaskRepository (Spring Data JPA)
│       │   │   └── service/         # TaskService, TaskServiceImpl
│       │   └── resources/
│       │       └── application.yml
│       └── test/
│           ├── java/com/taskmanager/  # TaskServiceImplTest, TaskControllerTest, TaskRepositoryTest
│           └── resources/application-test.yml
├── frontend/                        # UI (React + TypeScript + Vite)
│   ├── index.html
│   ├── vite.config.ts
│   ├── package.json
│   └── src/
│       ├── components/              # TaskForm, TaskList, TaskItem, TaskFilters, ConfirmDialog, NotificationList
│       ├── hooks/                   # useTasks, useNotifications
│       ├── pages/                   # TasksPage
│       ├── services/                # api.ts (Axios), taskService.ts
│       ├── types/                   # task.ts
│       ├── utils/                   # dateUtils.ts
│       ├── test/                    # setup.ts
│       ├── App.tsx
│       ├── main.tsx
│       └── index.css
├── scripts/
│   ├── api-smoke-test.sh            # Comprueba cada endpoint contra el backend
│   └── e2e-test.sh                  # Comprueba el flujo completo (frontend -> backend -> PostgreSQL)
├── docker-compose.yml               # PostgreSQL 17
├── .env.example                     # Plantilla de variables de entorno
├── .gitignore
└── README.md
```

---

## 3. Configuración de variables de entorno

El repositorio **no contiene credenciales reales**. Los valores por defecto son
locales y están documentados en `.env.example`.

```bash
cp .env.example .env      # Linux / macOS / Git Bash
copy .env.example .env    # Windows CMD
```

`.env` está en el `.gitignore`, así que nunca se sube al repositorio.

### Variables del backend

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/taskdb` | URL JDBC de PostgreSQL |
| `DB_USERNAME` | `taskuser` | Usuario de la base de datos |
| `DB_PASSWORD` | `taskpassword` | Contraseña de la base de datos |
| `SERVER_PORT` | `8080` | Puerto de la API |
| `DDL_AUTO` | `update` | `update` en desarrollo, `validate` en producción |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173,http://127.0.0.1:5173` | Orígenes permitidos (separados por comas) |

### Variables del frontend

| Variable | Por defecto | Descripción |
| --- | --- | --- |
| `VITE_API_BASE_URL` | *(vacío)* | Ruta base de la API. Si se deja vacía se usa el proxy de Vite (`/api`) |

> Las variables de Vite se leen **en tiempo de compilación**. Si cambias
> `VITE_API_BASE_URL`, reinicia `npm run dev`.

---

## 4. Iniciar PostgreSQL con Docker Compose

```bash
# En la raíz del proyecto
docker compose up -d

# Ver el estado y los logs
docker compose ps
docker compose logs -f postgres

# Detener (los datos se conservan en el volumen `postgres_data`)
docker compose down

# Detener y BORRAR los datos
docker compose down -v
```

El contenedor publica el puerto `5432` en el host, crea la base de datos
`taskdb` y el usuario `taskuser`. El `healthcheck` confirma que PostgreSQL
acepta conexiones antes de considerarlo listo.

**Verificación manual:**

```bash
docker compose exec postgres psql -U taskuser -d taskdb -c "\dt"
```

---

## 5. Ejecutar el backend

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

El arranque crea la tabla `tasks` automáticamente (Hibernate `ddl-auto=update`).

- API: <http://localhost:8080>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

---

## 6. Ejecutar el frontend

```bash
cd frontend

npm install
npm run dev
```

- Aplicación: <http://localhost:5173>

El servidor de Vite hace de proxy de `/api` hacia `http://localhost:8080`
(ver `vite.config.ts`), por lo que en desarrollo no hay problemas de CORS.

### Otros comandos del frontend

```bash
npm run build     # compila TypeScript y genera el bundle de producción en dist/
npm run preview   # sirve localmente el build de producción
npm run lint      # ESLint
npm test          # pruebas unitarias (Vitest)
```

---

## 7. Endpoints disponibles

Todos cuelgan de `/api/tasks`.

| Método | Ruta | Descripción | Respuesta |
| --- | --- | --- | --- |
| `GET` | `/api/tasks` | Lista las tareas. `?status=ALL\|PENDING\|COMPLETED` | `200` · `List<TaskResponse>` |
| `GET` | `/api/tasks/{id}` | Obtiene una tarea | `200` · `404` |
| `POST` | `/api/tasks` | Crea una tarea | `201` + `Location` · `400` |
| `PUT` | `/api/tasks/{id}` | Actualiza una tarea | `200` · `400` · `404` |
| `PATCH` | `/api/tasks/{id}/complete` | Completa o marca como pendiente | `200` · `404` |
| `DELETE` | `/api/tasks/{id}` | Elimina una tarea | `204` · `404` |

`PATCH /api/tasks/{id}/complete` **alterna** el estado si se llama sin cuerpo.
Si se envía un cuerpo, fija el estado de forma explícita:

```json
{ "completed": true }
```

### Modelo de datos

```json
{
  "id": 1,
  "title": "Comprar leche",
  "description": "Desnatada",
  "dueDate": "2030-01-15",
  "completed": false,
  "createdAt": "2026-01-01T10:00:00Z"
}
```

| Campo | Tipo | Obligatorio | Notas |
| --- | --- | --- | --- |
| `id` | number | generado | Auto-incremental |
| `title` | string | **sí** | Máximo 200 caracteres, no puede estar en blanco |
| `description` | string \| null | no | Máximo 2000 caracteres |
| `dueDate` | string \| null | no | `YYYY-MM-DD`; puede estar en pasado (la tarea queda *vencida*) |
| `completed` | boolean | no | Por defecto `false` |
| `createdAt` | string | generado | ISO-8601 en UTC |

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

---

## 8. Ejemplos de solicitudes HTTP

```bash
# Listar todas las tareas
curl http://localhost:8080/api/tasks

# Listar solo las pendientes
curl "http://localhost:8080/api/tasks?status=PENDING"

# Crear una tarea
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Comprar leche",
        "description": "Desnatada",
        "dueDate": "2030-01-15",
        "completed": false
      }'

# Obtener una tarea
curl http://localhost:8080/api/tasks/1

# Actualizar una tarea
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Comprar leche de avena",
        "description": "Desnatada",
        "dueDate": "2030-02-20",
        "completed": false
      }'

# Marcar como completada (alterna el estado)
curl -X PATCH http://localhost:8080/api/tasks/1/complete

# Fijar el estado de forma explícita
curl -X PATCH http://localhost:8080/api/tasks/1/complete \
  -H "Content-Type: application/json" \
  -d '{ "completed": true }'

# Eliminar una tarea
curl -X DELETE http://localhost:8080/api/tasks/1

# Error: título en blanco -> 400
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{ "title": "   " }'

# Error: tarea inexistente -> 404
curl -i http://localhost:8080/api/tasks/999999
```

---

## 9. Ejecutar las pruebas

### Backend (JUnit 5 + Mockito)

```bash
cd backend
mvn test
```

El perfil de test usa **H2 en memoria**, así que la suite se ejecuta en
cualquiera máquina **sin Docker ni PostgreSQL**:

| Suite | Qué cubre |
| --- | --- |
| `TaskServiceImplTest` | Reglas de negocio con el repositorio mockeado (crear, leer, actualizar, borrar, alternar estado, tarea inexistente) |
| `TaskControllerTest` | Capa REST completa con `MockMvc`: códigos HTTP, contrato JSON, validaciones, 404, `Location` en el 201 |
| `TaskRepositoryTest` | Persistencia y consultas derivadas contra una base de datos real (H2) |

### Frontend (Vitest + React Testing Library)

```bash
cd frontend
npm test            # una sola ejecución
npm run test:watch  # modo watch
```

| Fichero | Qué cubre |
| --- | --- |
| `taskService.test.ts` | URLs, parámetros y cuerpos de cada llamada HTTP |
| `useTasks.test.tsx` | Estados de carga, guardado y error; recarga al cambiar el filtro |
| `TaskForm.test.tsx` | Validación del formulario y datos enviados |
| `TaskItem.test.tsx` | Renderizado, insignias *Overdue* / *Completed* y botones |
| `TaskList.test.tsx` | Estado de carga y estado vacío |
| `dateUtils.test.ts` | Helpers de fecha |

### Scripts de verificación end-to-end

Con el backend, el frontend y PostgreSQL levantados:

```bash
bash scripts/api-smoke-test.sh   # 32 comprobaciones sobre la API
bash scripts/e2e-test.sh         # 19 comprobaciones del flujo completo
```

---

## 10. Decisiones técnicas

**Backend**

- **Arquitectura por capas** (`controller` → `service` → `repository`): cada
  capa tiene una responsabilidad y se puede testear por separado. El
  controlador no contiene lógica de negocio.
- **DTOs en los bordes de la API**: la entidad JPA nunca sale del service. Así
  se puede cambiar el modelo de datos sin romper el contrato HTTP.
- **Inyección de dependencias por constructor**: las dependencias son
  explícitas y `final`, y los tests pueden inyectar dobles sin Mockito
  reflexivo.
- **Bean Validation** (`@NotBlank`, `@Size`) en el DTO de entrada: las reglas
  se declaran una sola vez y Spring las aplica automáticamente.
- **`@RestControllerAdvice`** para centralizar los errores y devolver siempre
  el mismo formato `ApiError`.
- **Transacciones** declaradas en la capa de servicio (`@Transactional`), no en
  los controladores.
- **CORS configurable** por variable de entorno, con los orígenes de
  desarrollo por defecto.
- **Swagger / OpenAPI** con `springdoc-openapi`: la documentación se genera
  desde el código, así que no puede quedarse desactualizada.

**Frontend**

- **TypeScript estricto** en todo el proyecto: los errores de la API se
  detectan al compilar, no en producción.
- **Las llamadas HTTP fuera de los componentes**: `services/taskService.ts`
  concentra todo el acceso a la API y `hooks/useTasks.ts` el estado. Los
  componentes solo renderizan y despachan acciones.
- **Validación en dos capas**: el formulario valida para dar feedback
  inmediato, y el backend valida de nuevo porque siempre se puede saltarse el
  cliente.
- **Vite con proxy `/api`**: en desarrollo no hay CORS ni URLs absolutas.

**Base de datos**

- **Hibernate `ddl-auto=update`** solo en desarrollo. En producción se
  recommend `validate` + Flyway/Liquibase (ver *Mejoras futuras*).
- **Consultas derivadas** de Spring Data (`findByCompletedTrueOrderBy…`) en vez
  de JPQL a mano: menos SQL escrito a mano, más legible.

---

## 11. Mejoras futuras

1. **Autenticación y usuarios**: cada tarea asociada a un usuario
   (`Spring Security` + JWT). La tabla `tasks` ganaría la columna `user_id`.
2. **Migraciones de esquema** con Flyway o Liquibase, sustituyendo
   `ddl-auto=update`.
3. **Paginación** (`Pageable`) para listas grandes, con scroll infinito en el
   frontend.
4. **Categorías y prioridades** en la tarea, con filtros adicionales.
5. **Tests de integración con Testcontainers** contra un PostgreSQL real en
   lugar de H2, para validar el SQL generado por Hibernate.
6. **Despliegue** con un `Dockerfile` multi-stage por aplicación y un
   `docker-compose` completo (API + base de datos + frontend).
7. **Modo oscuro** y persistencia del filtro seleccionado.

---

## 12. Licencia

MIT.
