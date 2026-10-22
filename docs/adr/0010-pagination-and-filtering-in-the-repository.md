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

- `JpaSpecification` es más Heavy que un método derivado: para un filtro
  simple, un método derivado se lee mejor. Se usa `Specification` **solo**
  cuando la búsqueda por texto está activa, de modo que el camino común
  (filtro por estado) sigue siendo un método derivado legible.
- Las especificaciones con criterios dinámicos requieren más cuidado al
  probarlas: hay que verificar la consulta generada, no solo el resultado.
- `Pageable` es un concepto más rico que un `List` simple: hay que decidir
  explícitamente el número de página y de elementos.

## Alternativas consideradas

- **Filtrar en memoria:** sencillo, pero incorrecto. Transferiría toda la
  tabla al servidor de aplicación para descartar la mayor parte, y el
  consumo de memoria crecería sin límite.
- **JPQL handwritten:** más verboso que los métodos derivados y menos
  seguro frente a errores de concatenación.
- **`@Query` con parámetros opcionales:** no permite componer criterios de
  forma dinámica sin consultas muyverages.
