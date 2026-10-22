# ADR-0007 — Hook propio en lugar de una librería de queries

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** frontend

## Contexto

El frontend necesita gestionar el estado de una lista que viene de una API:
cargando, con datos, con error, y recargarla tras cada mutación.

La opción por defecto en el ecosistema React moderno es una librería de
gestión de estado remoto como **TanStack Query** o **SWR**. Ambas resuelven
muchos problemas reales: caché, reintentos, deduplicación de peticiones,
invalidación de queries, sincronización entre componentes, `refetch` automático
al recuperar la conexión, y cuota de peticiones por segundo.

La alternativa es escribir la lógica a mano en un hook propio, como ya se ha
hecho en `useTasks`.

## Decisión

**Se usa un hook propio (`useTasks`) en lugar de TanStack Query o SWR**, y esta
decisión es consciente, no accidental.

Motivos:

1. **Es el objetivo pedagógico del proyecto.** El lector tiene que entender
   qué hace cada línea. Un hook de 80 líneas que se puede leer entero vale más
   que una librería que hay que conocer por dentro para saber qué está
   happening.
2. **El conjunto de operaciones es pequeño.** Hay un único recurso (`tasks`),
   sin caché compartida entre vistas, sin sincronización entre componentes ni
   con otras pestañas. La mayor parte de la complejidad de TanStack Query
   resolvía problemas que aquí no existen.
3. **Sin dependencias extra.** Menos superficie de ataque, menos versiones que
   actualizar y un `package.json` legible.

El hook **sí** encapsula lo que la interfaz necesita: `isLoading`, `isSaving`,
`error`, el filtro activo y las operaciones de crear, actualizar, alternar y
borrar. Los componentes nunca llaman a Axios.

### Límite explícito de esta decisión

Si en el futuro la aplicación crece a **más de un consumidor del mismo recurso**
(por ejemplo, una vista de detalle y una vista de lista mostrando la misma
tarea y necesitando sincronizarse), esta decisión **queda obsoleta** y hay que
migrar a TanStack Query. El punto de inflexión está documentado aquí para que
la migración sea una decisión informada y no un accidente.

## Alternativas consideradas

| Alternativa | Por qué no se eligió |
| --- | --- |
| **TanStack Query** | Excelente librería. Se descarta solo por el objetivo pedagógico y porque el caso de uso no justifica su complejidad en la v1.0. Es la opción recomendada en la fase 2 (ver roadmap). |
| **SWR** | Más simple que TanStack Query, pero con menos control sobre las mutaciones y la invalidación, que es justo lo que este proyecto necesita. |
| **Redux Toolkit / RTK Query** | RTK Query resolvería el problema, pero Redux solo aporta valor con varios dominios o estado complejo compartido. Aquí hay un único recurso. |
| **Estado global con Context** | Para compartir el estado entre componentes. Aquí el estado no se comparte: cada vista tiene el suyo y el hook lo encapsula. Añadir un Context sería ceremonia sin benefit. |
| **Fetching directo en los componentes con `useEffect`** | El error más común: cada componente repite el mismo `useState` + `useEffect` + `try/catch`. El hook existe justamente para evitar esa duplicación. |

## Consecuencias

### Positivas
- Cero dependencias extra para la gestión de estado remoto.
- El flujo completo (carga → mutación → recarga) se lee en un solo fichero.
- Comportamiento predecible: cada mutación recarga la lista con el filtro
  activo, sin sorpresas de invalidación.
- Los tests del hook son deterministas y rápidos (7 tests).

### Negativas
- **No hay caché entre montajes.** Cada visita a la vista hace una petición.
  Aceptable: el conjunto de datos es pequeño y la petición es local.
- **No hay reintento automático** ante un fallo de red. El usuario ve el error
  y puede recargar manualmente.
- **No hay deduplicación** de peticiones concurrentes.
- **No hay sincronización** entre pestañas.
- Más código propio que mantener.

### Mitigación

Todas estas carencias son aceptables para el alcance definido en
`docs/SCOPE.md` (ver `NO-5` y `S-3`). Si el alcance crece, el hook se
sustituye por TanStack Query **sin tocar los componentes**, porque la interfaz
pública del hook se mantiene.
