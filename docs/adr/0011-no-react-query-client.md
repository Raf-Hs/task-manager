# ADR-0011 — Sin librería de cliente HTTP (React Query)

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** frontend

## Contexto

La aplicación necesita obtener, crear, actualizar y eliminar tareas, con
estados de carga y de error.

La opción habitual hoy es usar **TanStack Query** (antes React Query): cachea
las respuestas, invalida las consultas al mutar, reintenta los fallos,
deduplica las peticiones y sincroniza entre varias pestañas.

La alternativa es gestionar ese estado a mano, como hace el hook `useTasks` de
este proyecto.

## Decisión

**No se añade una librería de cliente HTTP. El estado de la lista se gestiona
con un hook propio (`useTasks`).**

## Razones

1. **El proyecto es pequeño.** Cinco endpoints y una sola colección. La
   caché en memoria, la deduplicación de peticiones y la sincronización entre
   pestañas resuelven problemas que esta aplicación todavía no tiene.

2. **Comprensibilidad.** El objetivo declarado del proyecto es que un
   desarrollador junior pueda leer, entender y defender cada línea. Un hook de
   60 líneas que hace `load()` tras cada mutación se puede explicar entero
   en una entrevista. Una librería de cliente HTTP exige conocer su modelo de
   caché (`staleTime`, `gcTime`, `queryKey`, invalidación por tags) para
   poder razonar sobre el comportamiento de la aplicación.

3. **Sin dependencias extra.** React y Axios cubren el caso de uso. La
   dependencia total del frontend se queda en dos.

4. **El coste de la alternativa.** React Query requiere `QueryClient` en el
   provider, `QueryClientProvider` en el árbol, decisiones sobre las
   `queryKey` y estrategia de invalidación. Para una aplicación con una
   colección, es mucha superficie conceptual a cambio de muy poco.

## Cuándo reconsiderarlo

En cuanto aparezca **una de estas condiciones**:

- Varias pantallas consultando los mismos datos y necesitando que se
  sincronicen entre ellas.
- Paginación con precarga de la página siguiente (*infinite scroll*).
- Reintentos automáticos con *backoff*.
- Optimismo en las actualizaciones (que la respuesta llegue más tarde que la
  interfaz).
- Sincronización entre pestañas.

En ese momento la respuesta correcta es **añadir TanStack Query**, no
reinventar su lógica de caché dentro de un hook propio. La decisión está
documentada aquí precisamente para que la migración sea consciente y no un
refactor a ciegas cuando el proyecto crezca.

## Nota

La lógica de red está **aislada** en `services/taskService.ts` (una función
por endpoint) y el estado, en `hooks/useTasks.ts`. Sustituir TanStack Query
después afecta a esos dos ficheros y a la capa de presentación, **no** a los
componentes: la separación por capas que ya existe hace la migración
acotada.
