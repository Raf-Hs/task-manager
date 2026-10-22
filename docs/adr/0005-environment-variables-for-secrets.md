# ADR-0005 — Credenciales por variables de entorno

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend, infraestructura

## Contexto

La aplicación se conecta a PostgreSQL con usuario y contraseña. Esas
credenciales no deben aparecer en el repositorio: si están en el código, viajan
en cada `git clone`, quedan en el historial para siempre y se filtran en cada
copia que se comparte.

Además, las credenciales de desarrollo, de integración continua y de
producción son **distintas**, así que el código no puede tenerlas fijadas.

## Decisión

Las credenciales se inyectan como **variables de entorno** con un valor por
defecto pensado para desarrollo local:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/taskdb}
    username: ${DB_USERNAME:taskuser}
    password: ${DB_PASSWORD:taskpassword}
```

Convenciones del proyecto:

| Elemento | Regla |
| --- | --- |
| `.env.example` | Versionado. Documenta **todas** las variables con su valor por defecto. |
| `.env` | **Nunca** versionado. Está en el `.gitignore`. |
| Valores por defecto | Solo credenciales de desarrollo local, sin valor de producción. |
| Formato | `NOMBRE_EN_MAYUSCULAS` con guiones bajos. |
| Commit accidental | La CI comprueba que `.env` no está versionado (ver `ci.yml`). |

El backend **no** lee el fichero `.env` por su cuenta: eso lo hace
`docker compose`. Spring Boot lee directamente las variables de entorno del
sistema, de modo que el mismo JAR funciona en Docker, en CI y en local.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Credenciales en `application.yml`** | Se versionan. Solo funciona con GitHub Secrets o similar, que complica el desarrollo local. |
| **Fichero `application-local.yml` versionado** | El mismo problema: las credenciales acabarían en el repositorio. |
| **`spring.config.import=optional:file:.env[.properties]`** | Permite que el backend lea `.env` directamente, lo cual es cómodo, pero añade una dependencia implícita del orden de carga de propiedades. Se documenta como alternativa válida para CI. |
| **Vault / AWS Secrets Manager** | Sobreingeniería para un proyecto de un solo usuario. Es la opción correcta en producción y aparece en el roadmap. |
| **Cifrar el secreto en el repositorio** | La clave de descifrado acaba en la máquina del desarrollador: no aporta seguridad real. |

## Consecuencias

### Positivas
- Ningún secreto en el repositorio, verificable de forma automática.
- El mismo artefacto sirve para todos los entornos: solo cambian las variables.
- `.env.example` funciona como documentación ejecutable del proyecto.
- El desarrollo local funciona sin configuración adicional: los valores por
  defecto coinciden con los que crea `docker-compose.yml`.

### Negativas
- Hay que recordar copiar `.env.example` a `.env` para cambiar los valores.
  Se mitiga con un paso explícito en el README y con los valores por defecto.
- En Windows, las variables de entorno se configuran de forma distinta a
  Unix. Se documentan ambas formas en el README.
- Un valor por defecto equivocado puede ser un bug silencioso. Se mitiga con
  el `healthcheck` de Docker y con los tests de integración, que fallan de
  forma ruidosa si la base de datos no responde.
