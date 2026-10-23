# ADR-0010 — El filtrado y la paginación se resuelven en el repositorio

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend

## Contexto

La lista de tareas admite un filtro por estado (`ALL`, `PENDING`,
`COMPLETED`), un filtro de búsqueda por texto y un orden configurable. Además,
cuando hay muchas tareas, la respuesta debe ser paginada en lugar de devolver
las miles de filas de golpe.

Existen dos formas de resolverlo:

**A) Filtrar en memoria.** Se cargan todas las tareas del repositorio y se
filtran y ordenan en Java con Streams.

**B) Delegar en la base de datos.** Se declara una consulta derivada
(`findByCompletedTrueOrderByCreatedAtDesc`) o se usa `JpaSpecification` /
`Criteria` para construir la consulta en tiempo de ejecución.

## Decisión

**Se delega en la base de datos** (opción B).

- Los métodos derivados de Spring Data cubren los tres filtros por estado con
  una consulta declarativa y legible, sin escribir JPQL.
- La búsqueda por texto y el orden variable se construyen con
  `JpaSpecification`, que permite componer criterios de forma componible.
- La paginación la resuelve Spring Data con `Pageable`, aplicando `LIMIT` y
  `OFFSET` en SQL.

El punto clave: **el número de filas que viajan por la red está acotado por
la paginación**, independientemente del tamaño de la tabla.

## Consecuencias

### Positivas

- La carga inicial de la aplicación no sufre cuando la tabla crece.
- El filtrado y el orden se ejecutan en la base de datos, que es donde están
  los índices.
- La especificación de la búsqueda se puede componer con otros criterios
  (`and`, `or`) sin condiciones bifurcadas en el código.
- Las consultas se generan a partir de un criterio en lugar de interpolar
  cadenas, lo que evita la inyección de SQL por concatenación.

### Negativas

- `JpaSpecification` es más verbosa que un método derivado: para un filtro
  simple, un método derivado se lee mejor. Se acepta ese coste porque el
  filtro y el orden se combinan libremente y el mismo código sirve para todas
  las combinaciones.
- Las especificaciones con criterios dinámicos requieren más cuidado al
  probarlas: hay que verificar la consulta generada, no solo el resultado.
  Por eso el orden se prueba contra una base de datos real
  (`TaskRepositoryTest`).
- `Pageable` es un concepto más rico que un `List` simple: hay que decidir
  explícitamente el número de página y de elementos.
- El orden no se puede expresar con un `Sort` de Spring Data cuando algún
  criterio no es una columna. Ordenar por prioridad necesita una expresión
  `CASE` (el texto `HIGH`, `MEDIUM`, `LOW` no se ordena como uno espera), así
  que el `ORDER BY` se construye con la API de `Criteria` dentro de la
  especificación. La consecuencia práctica es que el `Pageable` viaja **sin**
  orden: un `Sort` en el `Pageable` reemplazaría el orden de la
  especificación.

## Alternativas consideradas

- **Filtrar en memoria:** sencillo, pero incorrecto. Transferiría toda la
  tabla al servidor de aplicación para descartar la mayor parte, y el
  consumo de memoria crecería sin límite.
- **JPQL escrito a mano:** más verboso que los métodos derivados y menos
  seguro frente a errores de concatenación.
- **`@Query` con parámetros opcionales:** no permite componer criterios de
  forma dinámica sin consultas muy enrevesadas.
- **Ordenar por prioridad con `JpaSort.unsafe`:** Spring Data lo admite para
  consultas JPQL, pero no cuando la consulta se construye con `Criteria`
  (`findAll(Specification, Pageable)`), que es el caso. Por eso la expresión
  se escribe con `CriteriaBuilder`.
