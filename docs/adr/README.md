# Registro de decisiones de arquitectura (ADR)

Cada decisión técnica relevante del proyecto está documentada aquí como un
*Architecture Decision Record*: contexto, decisión, alternativas descartadas y
consecuencias.

El objetivo es que cualquier persona (o cualquier entrevistador) pueda
entender **por qué** el proyecto es como es, no solo **qué** hace.

**Formato**

| Campo | Contenido |
| --- | --- |
| **Estado** | `Aceptada`, `Reemplazada`, `Obsoleta`, `Propuesta` |
| **Contexto** | El problema y las restricciones que forzaron la decisión |
| **Decisión** | Lo que se decidió, en una frase |
| **Alternativas** | Lo que se descartó y por qué |
| **Consecuencias** | Lo que se gana y lo que se pierde |

**Índice**

| # | Decisión | Estado |
| --- | --- | --- |
| [0001](0001-layered-architecture.md) | Arquitectura por capas en el backend | Aceptada |
| [0002](0002-dtos-on-api-boundaries.md) | DTOs en los límites de la API | Aceptada |
| [0003](0003-validation-in-client-and-server.md) | Validación duplicada en cliente y servidor | Aceptada |
| [0004](0004-constructor-injection.md) | Inyección de dependencias por constructor | Aceptada |
| [0005](0005-environment-variables-for-secrets.md) | Credenciales por variables de entorno | Aceptada |
| [0006](0006-h2-for-tests.md) | H2 en memoria para las pruebas | Aceptada |
| [0007](0007-custom-hook-over-query-library.md) | Hook propio en lugar de una librería de queries | Aceptada |
| [0008](0008-vite-proxy-for-development.md) | Proxy de Vite en desarrollo | Aceptada |
| [0009](0009-flyway-over-ddl-auto.md) | Flyway en lugar de `ddl-auto=update` | Aceptada |
| [0010](0010-pagination-and-filtering-in-the-repository.md) | El filtrado y la paginación se resuelven en el repositorio | Aceptada |
| [0011](0011-no-react-query-client.md) | Sin librería de cliente HTTP (React Query) | Aceptada |

---

## Plantilla

Al añadir una decisión nueva, copia este bloque en
`docs/adr/NNNN-titulo-corto.md` y actualiza el índice de esta página.

```markdown
# ADR-NNNN — Título de la decisión

- **Estado:** Propuesta
- **Fecha:** YYYY-MM-DD
- **Ámbito:** backend | frontend | infraestructura

## Contexto
...

## Decisión
...

## Alternativas consideradas
...

## Consecuencias
### Positivas
- ...
### Negativas
- ...
```
