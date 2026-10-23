# Referencia de la API

Contrato HTTP del backend de **Task Manager**.

- **Base URL en desarrollo**: `http://localhost:8080`
- **Documentación interactiva**: <http://localhost:8080/swagger-ui.html>
- **Especificación OpenAPI**: <http://localhost:8080/v3/api-docs>

La especificación se genera del propio código con `springdoc-openapi`, así que
nunca puede quedarse desactualizada respecto a la implementación. Este
documento explica el contrato con ejemplos; para el detalle exhaustivo de
cada campo, Swagger UI es la referencia.

---

## 1. Convenciones

| Tema | Regla |
| --- | --- |
| Base de las rutas | `/api/tasks` |
| Formato de las fechas | ISO-8601, `AAAA-MM-DD` para fechas y `AAAA-MM-DDTHH:MM:SSZ` para marcas de tiempo |
| Zona horaria | UTC en todas las marcas de tiempo |
| Codificación | `UTF-8` |
| Content-Type | `application/json` en las peticiones con cuerpo y en las respuestas |
| Identificadores | `Long` en el backend, `number` en el frontend |
| Valores de los `enum` | Se aceptan en cualquier caso (`high`, `HIGH`, `High` son equivalentes) |

### Códigos de estado

| Código | Significado |
| --- | --- |
| `200 OK` | La operación se ha realizado y devuelve un cuerpo. |
| `201 Created` | Se ha creado un recurso. La cabecera `Location` apunta al nuevo recurso. |
| `204 No Content` | La operación se ha realizado y no devuelve cuerpo. |
| `400 Bad Request` | Los datos de la petición no son válidos. |
| `404 Not Found` | El recurso solicitado no existe. |
| `500 Internal Server Error` | Error inesperado. El detalle nunca se expone al cliente. |

---

## 2. Modelo de datos

### `TaskResponse`

Es lo que devuelve la API. Refleja la entidad `Task`, pero es un DTO
independiente: la entidad JPA nunca sale de la capa de servicio.

```json
{
  "id": 1,
  "title": "Comprar leche",
  "description": "Desnatada",
  "dueDate": "2030-01-15",
  "priority": "HIGH",
  "completed": false,
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-04T18:30:00Z"
}
```

| Campo | Tipo | Obligatorio | Notas |
| --- | --- | --- | --- |
| `id` | number | generado | Auto-incremental. Lo asigna la base de datos. |
| `title` | string | **sí** | Entre 1 y 200 caracteres. No puede estar en blanco. |
| `description` | string \| null | no | Hasta 2000 caracteres. Una cadena en blanco se guarda como `null`. |
| `dueDate` | string \| null | no | `AAAA-MM-DD`. Puede estar en pasado: la tarea queda *vencida*. |
| `priority` | string | no | `LOW`, `MEDIUM` o `HIGH`. Por defecto `MEDIUM`. |
| `completed` | boolean | no | Por defecto `false`. |
| `createdAt` | string | generado | Lo fija el servidor en la creación. |
| `updatedAt` | string | generado | Lo fija el servidor en cada modificación. |

### `TaskRequest`

Es lo que acepta la API en `POST` y en `PUT`. Solo se validan y se exponen los
campos que el cliente puede enviar.

```json
{
  "title": "Comprar leche",
  "description": "Desnatada",
  "dueDate": "2030-01-15",
  "priority": "HIGH",
  "completed": false
}
```

Si `priority` no viene, se aplica `MEDIUM`. Si `description` viene en blanco,
se guarda como `null`.

Los valores de los `enum` se aceptan **en cualquier caso**: `priority: "high"`
funciona igual que `priority: "HIGH"`, igual que ocurre con los parámetros de
la URL.

### `TaskStatistics`

Respuesta de `GET /api/tasks/statistics`.

```json
{
  "total": 12,
  "pending": 8,
  "completed": 4,
  "overdue": 2,
  "completionPercentage": 33
}
```

| Campo | Tipo | Significado |
| --- | --- | --- |
| `total` | number | Tareas que hay en total. |
| `pending` | number | Tareas sin completar. |
| `completed` | number | Tareas completadas. |
| `overdue` | number | Tareas pendientes con fecha límite en pasado. |
| `completionPercentage` | number | Porcentaje de completitud, redondeado al entero más cercano. |

> Las estadísticas describen **toda** la lista, no solo lo que hay visible en
> la pantalla. Los contadores se calculan en la base de datos.

### `PagedResponse<T`

Respuesta de los endpoints paginados.

```json
{
  "content": [ { "id": 1, "title": "Comprar leche" } ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

| Campo | Tipo | Significado |
| --- | --- | --- |
| `content` | array | Los elementos de la página. |
| `page` | number | Número de página, empezando por 0. |
| `size` | number | Tamaño de la página. |
| `totalElements` | number | Número total de elementos que cumplen el filtro. |
| `totalPages` | number | Número total de páginas. |
| `first` | boolean | `true` si es la primera página. |
| `last` | boolean | `true` si es la última página. |

> Se usa un DTO propio en lugar del `Page` de Spring Data, porque la forma
> serializada de `Page` no es parte de su API pública y ha cambiado entre
> versiones.

---

## 3. Formato de error

Todos los errores devuelven la misma estructura, gestionada por
`GlobalExceptionHandler`:

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

| Campo | Tipo | Descripción |
| --- | --- | --- |
| `status` | number | Código HTTP, repetido en el cuerpo para que el cliente no tenga que mirar la cabecera. |
| `message` | string | Descripción legible del error. |
| `path` | string | Ruta que produjo el error. |
| `timestamp` | string | Momento del error. |
| `fieldErrors` | object | *(opcional)* Errores por campo, con el mensaje concreto de cada regla incumplida. |

Ejemplos de `message`:

| Situación | `message` | `status` |
| --- | --- | --- |
| Payload inválido | `Validation failed for: title` | `400` |
| JSON mal formado o cuerpo vacío | `The request body is missing or has an invalid format` | `400` |
| Parámetro que no se puede convertir | `Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'` | `400` |
| Filtro de estado desconocido | `Unknown status 'UNKNOWN'. Allowed values: ALL, PENDING, COMPLETED.` | `400` |
| Filtro de prioridad desconocido | `Unknown priority 'URGENT'. Allowed values: LOW, MEDIUM, HIGH.` | `400` |
| Criterio de orden desconocido | `Unknown sort option 'random'. Allowed values: CREATED_AT, UPDATED_AT, DUE_DATE, TITLE, PRIORITY.` | `400` |
| Tarea inexistente | `Task with id 999 was not found` | `404` |
| Error inesperado | `Unexpected server error. Please try again later.` | `500` |

---

## 4. Endpoints

### `GET /api/tasks`

Devuelve una página de tareas. Todos los parámetros son opcionales y se
combinan entre sí con **AND**.

| Parámetro | Valores | Por defecto | Descripción |
| --- | --- | --- | --- |
| `status` | `ALL`, `PENDING`, `COMPLETED` | `ALL` | Filtra por estado de completitud. |
| `priority` | `LOW`, `MEDIUM`, `HIGH` | *(sin filtro)* | Filtra por prioridad. |
| `search` | texto | *(sin filtro)* | Busca en el título y en la descripción, ignorando mayúsculas. |
| `sort` | `CREATED_AT`, `UPDATED_AT`, `DUE_DATE`, `TITLE`, `PRIORITY` | `CREATED_AT` | Criterio de orden. |
| `page` | número | `0` | Número de página, empezando por 0. Un valor negativo se trata como `0`. |
| `size` | número | `20` | Tamaño de la página. Se limita a un máximo de `100`; un valor menor o igual a `0` usa el valor por defecto. |

```bash
curl "http://localhost:8080/api/tasks?status=PENDING&priority=HIGH&size=5"
curl "http://localhost:8080/api/tasks?search=leche"
curl "http://localhost:8080/api/tasks?sort=TITLE&page=2&size=10"
```

**Respuesta `200`**

```json
{
  "content": [ /* ... */ ],
  "page": 0,
  "size": 20,
  "totalElements": 12,
  "totalPages": 1,
  "first": true,
  "last": true
}
```

**Errores**

| Código | Causa |
| --- | --- |
| `400` | `status`, `priority` o `sort` con un valor que no existe. |

> **Cómo se ordena.** Las tareas pendientes se muestran siempre antes que las
> completadas, sea cual sea el criterio. Después se aplica el criterio pedido:
>
> | `sort` | Orden |
> | --- | --- |
> | `CREATED_AT` | Más reciente primero. |
> | `UPDATED_AT` | Modificada más recientemente primero. |
> | `DUE_DATE` | Fecha límite más cercana primero. |
> | `TITLE` | Alfabético. |
> | `PRIORITY` | De más urgente a menos urgente (`HIGH`, `MEDIUM`, `LOW`). |
>
> El identificador actúa como último desempate, así que dos peticiones iguales
> devuelven siempre el mismo orden.

---

### `GET /api/tasks/statistics`

Devuelve las cifras agregadas de toda la lista. No acepta parámetros.

```bash
curl http://localhost:8080/api/tasks/statistics
```

**Respuesta `200`**: ver [`TaskStatistics`](#taskstatistics).

---

### `GET /api/tasks/{id}`

Devuelve una tarea.

| Parámetro | Tipo | Descripción |
| --- | --- | --- |
| `id` | number | Identificador de la tarea. |

```bash
curl http://localhost:8080/api/tasks/1
```

**Respuesta `200`**: ver [`TaskResponse`](#taskresponse).

**Errores**

| Código | Causa |
| --- | --- |
| `400` | El identificador no es numérico. |
| `404` | No existe ninguna tarea con ese identificador. |

---

### `POST /api/tasks`

Crea una tarea.

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Comprar leche",
        "description": "Desnatada",
        "dueDate": "2030-01-15",
        "priority": "HIGH",
        "completed": false
      }'
```

**Respuesta `201 Created`**, con la cabecera `Location` apuntando a la tarea
creada:

```json
{
  "id": 1,
  "title": "Comprar leche",
  "description": "Desnatada",
  "dueDate": "2030-01-15",
  "priority": "HIGH",
  "completed": false,
  "createdAt": "2026-01-01T10:00:00Z",
  "updatedAt": "2026-01-01T10:00:00Z"
}
```

**Errores**

| Código | Causa |
| --- | --- |
| `400` | El título falta, está en blanco o supera los 200 caracteres; la descripción supera los 2000; el `priority` no existe; el cuerpo no es JSON válido. |

---

### `PUT /api/tasks/{id}`

Actualiza una tarea **completa**. Los campos que no se envían se guardan con su
valor por defecto, así que conviene enviar el objeto entero.

```bash
curl -X PUT http://localhost:8080/api/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{
        "title": "Comprar leche de avena",
        "description": "Desnatada",
        "dueDate": "2030-02-20",
        "priority": "LOW",
        "completed": false
      }'
```

**Respuesta `200`**: la tarea actualizada.

**Errores**

| Código | Causa |
| --- | --- |
| `400` | El payload no cumple las reglas de validación. |
| `404` | No existe ninguna tarea con ese identificador. |

---

### `PATCH /api/tasks/{id}/complete`

Marca la tarea como completada o como pendiente.

**Sin cuerpo**, el estado **alterna**: una tarea pendiente pasa a completada y
al revés.

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/complete
```

**Con cuerpo**, el estado se fija de forma explícita:

```bash
curl -X PATCH http://localhost:8080/api/tasks/1/complete \
  -H "Content-Type: application/json" \
  -d '{ "completed": true }'
```

**Respuesta `200`**: la tarea con el estado nuevo.

**Errores**

| Código | Causa |
| --- | --- |
| `404` | No existe ninguna tarea con ese identificador. |

---

### `DELETE /api/tasks/{id}`

Elimina la tarea. No hay vuelta atrás: por eso el frontend pide confirmación
antes de llamar a este endpoint.

```bash
curl -X DELETE http://localhost:8080/api/tasks/1
```

**Respuesta `204 No Content`**, sin cuerpo.

**Errores**

| Código | Causa |
| --- | --- |
| `404` | No existe ninguna tarea con ese identificador. |

---

## 5. Ejemplo completo

```bash
# 1. Crear
ID=$(curl -sS -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Comprar leche","priority":"HIGH"}' \
  | sed -n 's/.*"id":\([0-9]*\).*/\1/p')

# 2. Listar lo pendiente y urgente
curl "http://localhost:8080/api/tasks?status=PENDING&priority=HIGH"

# 3. Editar
curl -X PUT "http://localhost:8080/api/tasks/$ID" \
  -H "Content-Type: application/json" \
  -d '{"title":"Comprar leche de avena","priority":"LOW"}'

# 4. Completar
curl -X PATCH "http://localhost:8080/api/tasks/$ID/complete"

# 5. Ver las cifras
curl http://localhost:8080/api/tasks/statistics

# 6. Borrar
curl -X DELETE "http://localhost:8080/api/tasks/$ID"
```

---

## 6. Reglas de negocio

Estas reglas están implementadas en la capa de servicio y cubiertas por tests.

| Regla | Detalle |
| --- | --- |
| El título es obligatorio | Entre 1 y 200 caracteres, no puede estar en blanco. |
| La descripción es opcional | Hasta 2000 caracteres; en blanco se guarda como `null`. |
| La prioridad tiene valor por defecto | `MEDIUM` cuando el cliente no la envía. |
| Las fechas límite pueden estar en pasado | La tarea se marca como *vencida* en la interfaz. |
| Las marcas de tiempo las fija el servidor | El cliente nunca puede elegir `createdAt` ni `updatedAt`. |
| No hay tarea sin título | El título es la identidad de la tarea. |
| Las completadas van al final | En cualquier criterio de orden. |

---

## 7. Verificación

Con el backend levantado:

```bash
bash scripts/api-smoke-test.sh   # 50 comprobaciones sobre esta referencia
```

El script cubre los siete endpoints, los códigos de estado, los mensajes de
error, los filtros, la búsqueda, el orden, la paginación y las estadísticas.
