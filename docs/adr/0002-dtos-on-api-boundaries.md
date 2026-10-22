# ADR-0002 — DTOs en los límites de la API

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend

## Contexto

Spring Data y JPA.mapearon automáticamente una entidad a JSON. Podríamos
devolver la entidad `Task` directamente desde el controlador y ahorrar una
conversión.

Pero esa comodidad ata el contrato público de la API al modelo de persistencia.
Cualquier cambio en la base de datos (renombrar una columna, añadir un campo
interno como `deletedAt`, cambiar el tipo de `dueDate`) se convierte en un
cambio incompatible en la API, sin avisar.

Además, la entidad tiene detalles que no deben exponerse: la anotación
`@Column`, la relación con `createdAt` gestionada por `@PrePersist`, o campos
que en el futuro se marquen como internos.

## Decisión

El controlador **nunca** devuelve ni recibe una entidad JPA. Se usan DTOs
explícitos:

| DTO | Dirección | Contenido |
| --- | --- | --- |
| `TaskRequest` | Entrada | `title`, `description`, `dueDate`, `priority`, `completed` |
| `TaskResponse` | Salida | `id`, `title`, `description`, `dueDate`, `priority`, `completed`, `createdAt`, `updatedAt` |
| `ApiError` | Salida (error) | `status`, `message`, `path`, `timestamp`, `fieldErrors` |

La conversión vive en una única clase (`TaskMapper`), no repartida por los
controladores ni por los servicios.

Regla adicional: `TaskRequest` no contiene `id`, `createdAt` ni `updatedAt`.
El cliente **no puede** fijar esos valores; los genera el servidor. Esto
impide que un `POST` con `createdAt` manipulado falsee la fecha de creación.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Devolver la entidad directamente** | Acopla el contrato HTTP al esquema de la base de datos. Un `SELECT *` accidental filtraría campos internos. |
| **MapStruct** | Ahorra código, pero añade un generador de código al build y una dependencia más. Para 4 conversiones, el mapper manual es más claro y más fácil de depurar. |
| **Records de Java** (`record TaskResponse(...)`) | Son inmutables y muy compactos, pero rompen la compatibilidad con Jackson para la deserialización en tests y obligan a cambiar de estilo solo en las respuestas. Se mantienen clases con getters/setters por coherencia con el resto del proyecto. |

## Consecuencias

### Positivas
- El contrato de la API es explícito y documentado en un solo sitio.
- Se puede cambiar el modelo de datos sin romper clientes.
- Los campos internos quedan protegidos por diseño, no por convención.
- El DTO de entrada es el lugar natural donde viven las reglas de Bean
  Validation, así que las reglas se declaran una sola vez (ver
  [ADR-0003](0003-validation-in-client-and-server.md)).

### Negativas
- Código repetitivo: cada DTO tiene getters, setters y constructor.
- Una conversión manual que alguien puede olvidar actualizar. Se mitiga con
  tests que verifican el mapeo de todos los campos.
- Cada campo nuevo obliga a tocar tres sitios (entidad, DTOs, mapper). Es
  intencionado: obliga a decidir conscientemente si el campo es público.
