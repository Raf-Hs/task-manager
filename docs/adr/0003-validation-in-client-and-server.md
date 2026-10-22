# ADR-0003 — Validación duplicada en cliente y servidor

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend, frontend

## Contexto

La misma regla de validación —«el título es obligatorio y no puede superar 200
caracteres»— se puede aplicar en un solo sitio:

- **Solo en el servidor**: el usuario escribe 300 caracteres, pulsa guardar y
  ve un error genérico. La otra mitad del formulario se pierde.
- **Solo en el cliente**: el backend acepta cualquier cosa. Un `curl` o un
  cliente futuro puede insertar una tarea sin título. La base de datos acaba
  con datos sucios.

## Decisión

Se valida en **ambos lados**, con responsabilidades distintas:

| Capa | Responsabilidad | Mensaje |
| --- | --- | --- |
| Frontend (`TaskForm`) | Feedback inmediato, antes de la red. | En el campo, en rojo, junto al input. |
| Backend (`TaskRequest` + `@Valid`) | Garantía de integridad. Última palabra. | En el cuerpo de la respuesta, en `fieldErrors`. |

**La regla se declara una vez por lado**, nunca tres veces:

- Backend: la regla vive en las anotaciones del DTO (`@NotBlank`, `@Size`).
  Spring la aplica automáticamente al invocar el controlador con `@Valid`.
- Frontend: la regla vive en la función `validate()` del formulario, que
  replica las anotaciones del backend.

La duplicación es **deliberada y consciously asumida**. No es un descuido: es
el precio de una buena experiencia de usuario sin renunciar a la integridad de
los datos.

Para que las dos copias no diverjan:

1. Las reglas del backend se documentan en `docs/API.md`.
2. El frontend tiene una constante con los límites, y su test comprueba que
   coinciden con el contrato.
3. El backend responde con `fieldErrors` por campo, de modo que el frontend
   puede mapear el error del servidor al input correspondiente.

### Regla de oro

> El cliente valida para **ayudar**; el servidor valida para **proteger**.
> Si el cliente dice que algo es válido, el servidor puede decir lo contrario:
> eso no es un bug, es la arquitectura funcionando.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Validar solo en el servidor** | Mala experiencia: ida y vuelta al servidor para errores que el navegador ya podía detectar. |
| **Validar solo en el cliente** | Inseguro. El cliente es código del usuario, no una garantía. |
| **Generar los tipos del frontend a partir del backend** (OpenAPI Generator) | Garantiza la sincronía, pero añade un paso de generación y una dependencia de Java para el flujo de trabajo del frontend. Se documenta como opción de la fase 2 en el roadmap. |
| **JSON Schema compartido** | Más ligero que OpenAPI, pero obliga a mantener el esquema aparte del código Java. |

## Consecuencias

### Positivas
- El usuario recibe el error en el instante, sinlatencia perceptible.
- La base de datos nunca recibe datos que violen las reglas.
- El servidor sigue siendo la autoridad: un cliente modificado no rompe nada.
- Los `fieldErrors` por campo permiten highlighting preciso en el formulario.

### Negativas
- Las reglas están duplicadas y pueden divergir con el tiempo.
- Cambiar un límite exige tocar dos sitios y actualizar la documentación.
- El mensaje de error aparece en dos formatos distintos (texto en el cliente,
  `fieldErrors` en el servidor). Se mitiga con un mensaje de error genérico en
  el servidor que el frontend puede mostrar tal cual.

## Mitigación de la divergencia

`TaskForm.test.tsx` cubre los dos límites del título (vacío y > 200 caracteres),
y `TaskControllerTest` cubre exactamente los mismos casos contra la API. Si
alguien cambia una regla en un lado y olvida el otro, uno de los dos suites
falla y la CI lo detecta.
