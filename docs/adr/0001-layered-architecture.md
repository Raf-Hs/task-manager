# ADR-0001 — Arquitectura por capas en el backend

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend

## Contexto

El backend expone un CRUD sobre tareas. La decisión que hay que tomar es cómo
organizar el código para que:

1. La lógica de negocio no dependa del protocolo HTTP (se pueda testear sin
   levantar un servidor).
2. El acceso a datos no se mezcle con las reglas de negocio.
3. El código siga siendo legible por alguien junior que abra el repositorio
   por primera vez.

La alternativa obvia —un único controlador que hace de todo— es más corta de
escribir pero mezcla cuatro responsabilidades en el mismo archivo.

## Decisión

Se adopta una arquitectura por capas con dependencias unidireccionales:

```
controller  →  service  →  repository  →  (JPA / PostgreSQL)
     ↓            ↓
    dto        mapper
```

| Capa | Responsabilidad | Puede conocer |
| --- | --- | --- |
| `controller` | Traducir HTTP ⇄ aplicación. Validar el formato de la petición. | `service`, `dto` |
| `service` | Reglas de negocio. Transacciones. Orquestación. | `repository`, `mapper`, `entity` |
| `repository` | Acceso a datos. | `entity` |
| `entity` | Estado persistente. | — |
| `dto` | Contrato de la API. | — |
| `mapper` | Conversión entidad ⇄ DTO. | `entity`, `dto` |
| `exception` | Traducción de errores a respuestas HTTP. | `dto` |

**Regla inviolable:** las flechas nunca se invierten. El `repository` no sabe
nada de HTTP; el `service` no sabe nada de `HttpServletRequest`; la `entity`
no sabe nada de la API.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Controlador único (scripting)** | Mezcla validación, negocio y persistencia. Un cambio en el modelo de datos obliga a tocar la capa HTTP. No es testeable de forma aislada. |
| **Domain Driven Design completo** (agregados, repositorios de dominio, eventos de dominio) | Sobreingeniería para un único agregado con seis operaciones. El coste de complejidad supera el beneficio con creces. |
| **Hexagonal / puertos y adaptadores** | Los puertos son prácticamente los mismos que las interfaces del `service`. La separación real ya la aporta la interfaz `TaskService`. |
| **Lombok** | Oculta los getters/setters, que son la parte más legible de una entidad JPA. Además añade un procesador de anotaciones al build, que es un punto de fallo habitual. Se usa Java explícito. |

## Consecuencias

### Positivas
- Las reglas de negocio se prueban con JUnit y Mockito, sin base de datos ni
  servidor web (16 tests en `TaskServiceImplTest`).
- La capa HTTP se prueba con `MockMvc` sin base de datos (18 tests en
  `TaskControllerTest`).
- Cambiar la API no obliga a tocar la persistencia, y viceversa.
- La estructura de carpetas documenta por sí sola dónde está cada cosa.

### Negativas
- Más archivos que en un script único: una operación CRUD toca al menos cinco.
- El mapeo entidad ⇄ DTO se repite en `mapper`, `service` y `controller`. Se
  acepta: es el precio de mantener el contrato de la API estable.
- Reglas implícitas: un revisor nuevo debe leer este ADR para saber que las
  dependencias no se invierten.
