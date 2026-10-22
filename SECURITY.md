# Política de seguridad

## Qué hacer si encuentras una vulnerabilidad

Agradecemos que nos avises. **No abras una issue pública** para reportar un
problema de seguridad: hazlo en privado.

### Canal de reporte

Usa la sección **Security advisories** de la página del repositorio en GitHub
(*Security* → *Report a vulnerability*). Es un canal privado entre tú y el
mantenedor del proyecto, y permite mantener la conversación separada de las
issues públicas.

Si necesitas otro canal, abre una issue pública que **no describa el
problema**, solo que quieres contactar de forma privada, y el mantenedor te
responderá por la vía privada.

### Qué incluir

- Descripción de la vulnerabilidad y su impacto.
- Pasos para reproducirla.
- Versión afectada (commit, etiqueta o versión).
- Cualquier prueba o captura que ayude.

### Qué esperar

| Plazo | Compromiso |
| --- | --- |
| Confirmación de recepción | 72 horas |
| Evaluación inicial | 7 días |
| Corrección o mitigación | 30 días desde la confirmación |

Te avisaremos cuando el problema esté corregido y cuando se publique la
advisory, indicando tu nombre si te parece bien.

---

## Versiones con soporte

| Versión | Soporte | Recibe parches |
| --- | --- | --- |
| `main` | Sí | Sí |
| Última etiqueta (`v*`) | Sí | Sí |
| Ramas antiguas | No | No |

El proyecto no mantiene versiones numbering separadas: la versión actual es
la de `main` y la última etiqueta publicada.

---

## Alcance

Esta política cubre el código de este repositorio:

- `backend/` — la API REST y su configuración.
- `frontend/` — la interfaz web.
- `scripts/` — los scripts de verificación.
- `docker-compose.yml` y los ficheros de despliegue.

**No cubre** los problemas de las dependencias de terceros, salvo que el
proyecto los configure de forma insegura (por ejemplo, una versión
vulnerable fijada a propósito). En ese caso, el problema sí es nuestro y
reportarlo por aquí es la vía adecuada.

---

## Estado actual de la aplicación

Task Manager es un proyecto de práctica sin autenticación. **No expongas esta
aplicación en una red pública tal como está**: cualquier persona con acceso
podría leer y modificar todas las tareas.

Esta limitación es conocida y está documentada en `docs/SCOPE.md`. La
autenticación es una de las mejoras previstas, pero mientras no exista, la
aplicación debe usarse solo en local o en una red de confianza.

---

## Buenas prácticas que el proyecto ya aplica

Para que no haya que reportarlas, estas decisiones ya están tomadas y
documentadas:

| Práctica | Dónde |
| --- | --- |
| Credenciales por variable de entorno, nunca en el repositorio | `docs/adr/0005-environment-variables-for-secrets.md` |
| CORS limitado a los orígenes configurados | `backend/src/main/java/com/taskmanager/config/CorsConfig.java` |
| Validación de la entrada en el servidor (Bean Validation) | `docs/adr/0003-validation-in-client-and-server.md` |
| Errores sin trazas ni detalles internos en la respuesta | `backend/src/main/java/com/taskmanager/exception/GlobalExceptionHandler.java` |
| Dependencias fijadas a una versión concreta | `backend/pom.xml`, `frontend/package.json` |
