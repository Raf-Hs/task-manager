# ADR-0006 — H2 en memoria para las pruebas

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend, infraestructura

## Contexto

Las pruebas del backend tienen que levantar una base de datos real para poder
ejecutar las consultas de Spring Data JPA. Hay dos formas habituales:

1. **Testcontainers**: se descarga la imagen oficial de PostgreSQL y se levanta
   un contenedor efímero por ejecución. Fidelity alta, pero exige un demonio
   Docker funcionando.
2. **Base de datos embebida (H2)**: una biblioteca en el classpath de test que
   arranca en memoria en milisegundos.

La pregunta es qué debe exigir la suite de tests a un desarrollador que acaba
de clonar el repositorio.

## Decisión

**La suite por defecto usa H2 en memoria** y no requiere ningún servicio
externo:

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:taskdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
```

El modo `MODE=PostgreSQL` hace que H2 acepte el dialecto SQL de PostgreSQL,
de modo que el SQL generado por Hibernate es prácticamente el mismo.

**Además**, se incluye una suite de integración real con **Testcontainers**
en `src/test/java/.../integration/`, que **solo** se ejecuta cuando se pide
explícitamente:

```bash
mvn verify -Pintegration          # requiere Docker
```

La suite por defecto y la de integración cubren lo mismo desde dos ángulos: la
primera valida la lógica, la segunda valida el SQL real contra PostgreSQL.

## Alternativas consideradas

| Alternativa | Por qué se descartó como opción por defecto |
| --- | --- |
| **Testcontainers siempre** | Exige Docker. En CI sin Docker, o en una máquina con la quota de contenedores agotada, la suite no arranca. Falla el requisito de «`mvn test` funciona en cualquier máquina». |
| **PostgreSQL local instalado a mano** | No es reproducible y cada desarrollador tendría que instalarlo y configurarlo. |
| **Base de datos en memoria de Spring (`spring-boot-starter-data-jpa` con H2 por defecto)** | Es exactamente lo que se hace, pero conviene ser explícito con el perfil `test` y la URL, para que quede documentado por qué. |
| **Base de datos en memoria de Spring Boot (`spring.datasource.url` con `MODE=PostgreSQL`)** | Sin soporte de `MODE=PostgreSQL`, el SQL generado por Hibernate puede no funcionar en H2, lo que produciría falsos negativos. Se usa el modo explícito. |

## Consecuencias

### Positivas
- `mvn test` funciona en una máquina recién configurada, sin Docker y sin
  PostgreSQL. Es el requisito que se priorizó.
- La suite completa tarda menos de 30 segundos.
- Los tests no pueden contaminar una base de datos de desarrollo: la de test
  vive en memoria y desaparece al terminar.
- La suite de Testcontainers permite validar el SQL real contra PostgreSQL sin
  que eso bloquee el flujo normal.

### Negativas
- H2 **no** es PostgreSQL. Hay diferencias de SQL que H2 acepta y PostgreSQL
  no (o al revés). Un test puede pasar en H2 y fallar en producción.
- La suite por defecto no valida que el esquema generado por Hibernate sea
  válido en PostgreSQL.
- Las dos suites pueden divergir.

### Mitigaciones

1. El dialecto de H2 es `PostgreSQL` (`MODE=PostgreSQL`), lo que reduce mucho la
   divergencia.
2. Existe la suite de integración con Testcontainers para validar el SQL real
   cuando hay Docker disponible.
3. `docker-compose.yml` levanta PostgreSQL real y `scripts/api-smoke-test.sh`
   ejecuta 32 comprobaciones contra la API con la base de datos real, de modo
   que el flujo completo se puede verificar en cualquier momento.
