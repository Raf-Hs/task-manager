# Cómo contribuir

Gracias por querer colaborar en **Task Manager**. Este documento explica cómo
trabajar en el proyecto, qué se espera de cada cambio y cómo se revisa.

La idea del proyecto es que sirva para **aprender y explicar**: el código
tiene que ser legible por alguien que no lo ha escrito. Eso es más importante
que la cantidad de funcionalidad.

---

## 1. Antes de empezar

1. Lee [`docs/SCOPE.md`](docs/SCOPE.md). Es la fuente de verdad del proyecto:
   objetivos, lo que **no** entra y los criterios de aceptación.
2. Revisa [`docs/adr/README.md`](docs/adr/README.md) para entender las
   decisiones que ya están tomadas y no volver a discutirlas sin un buen motivo.
3. Si tu propuesta es grande, abre primero una *issue* describiendo el
   problema, la solución y los riesgos.

### Antes de abrir una issue

- ¿El problema ya está descrito? Búscalo.
- ¿La propuesta cabe en el alcance? Si no, dilo y propón cómo encajaría.
- ¿Existe ya una decisión registrada (ADR) que lo cubra? Léela antes de
  descartarla.

---

## 2. Preparar el entorno

```bash
git clone https://github.com/Raf-Hs/task-manager.git
cd task-manager

cp .env.example .env

# Terminal 1 - base de datos
docker compose up -d

# Terminal 2 - API
cd backend && mvn spring-boot:run

# Terminal 3 - interfaz
cd frontend && npm install && npm run dev
```

Comprueba que todo responde:

```bash
curl http://localhost:8080/api/tasks
```

---

## 3. Flujo de trabajo

1. Crea una rama desde `main`:

   ```bash
   git switch -c feat/nombre-corto-de-la-feature
   ```

2. Haz el cambio, con commits pequeños y atómicos.
3. Actualiza la documentación afectada por el cambio.
4. Abre un *pull request* contra `main` describiendo el **qué** y el **por qué**.

### Nombres de rama

| Prefijo | Uso |
| --- | --- |
| `feat/` | Nueva funcionalidad |
| `fix/` | Corrección de un error |
| `docs/` | Solo documentación |
| `refactor/` | Reestructuración sin cambio de comportamiento |
| `test/` | Solo pruebas |
| `chore/` | Mantenimiento (dependencias, configuración) |

### Nombres de commit

Formato **Conventional Commits** en inglés:

```
<tipo>(<ámbito>): <descripción en imperativo>
```

Tipos válidos: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`,
`build`, `ci`, `chore`.

Ejemplos:

```
feat(backend): add the priority filter to the list endpoint
fix(frontend): keep the search text when the list reloads
docs(scope): define the acceptance criteria of the statistics panel
```

La primera línea no debería pasar de 72 caracteres y no debe terminar en punto.

---

## 4. Reglas de código

### 4.1 Comunes

- **Idioma del código**: identificadores, nombres de fichero y comentarios en
  inglés. **La documentación** (`README`, `docs/`, mensajes de commit) en
  español, que es el idioma del proyecto.
- Los comentarios explican el **por qué**, no el **qué**: el código ya dice lo
  que hace.
- Nada de código muerto: si no se usa, se borra en el mismo commit.
- Nada de secretos en el repositorio. Las credenciales van por variable de
  entorno (ver `docs/adr/0005-environment-variables-for-secrets.md`).

### 4.2 Backend (Java)

- Java 21, Spring Boot 3.3, Maven.
- Arquitectura por capas: `controller` → `service` → `repository`. El
  controlador **no** contiene lógica de negocio.
- **Inyección de dependencias por constructor**, con dependencias `final`.
- Los DTOs de entrada se validan con **Bean Validation**; los de salida no
  exponen la entidad JPA.
- Los errores se traducen en `GlobalExceptionHandler`. No hay `try/catch` con
  `printStackTrace()`.
- Se prefiere una consulta derivada o una `Specification` antes que JPQL a
  mano.
- Formato: 4 espacios, líneas de 120 caracteres como máximo.

### 4.3 Frontend (TypeScript)

- TypeScript estricto. Ningún `any` sin una justificación escrita.
- Los componentes **no** llaman a Axios: pasan por `services/taskService.ts`.
- El estado que se comparte entre vistas vive en un hook (`hooks/`), no en el
  componente.
- Los componentes se prueban con React Testing Library, consultando por texto
  o por rol, nunca por clases CSS.
- CSS en un único fichero (`index.css`), con variables CSS para los colores.

### 4.4 Tests

- Cada error corregido llega con un test que falla antes del arreglo.
- Cada funcionalidad nueva llega con sus tests.
- Nombres de test que describen el comportamiento esperado:
  `"create persists the task and returns it"`.

---

## 5. Commits

Un commit es **atómico**: hace una cosa y la hace bien. Si el mensaje dice
"add the search", el commit no debería tocar también el modelo de datos.

- El historial debe ser legible sin `git show`.
- No se reescribe historia ya publicada en `main` (salvo acuerdo explícito).
- Un commit nunca deja el proyecto roto: los tests pasan en cada commit.

---

## 6. Pull requests

Antes de pedir la revisión:

```bash
# Backend
cd backend && mvn clean package

# Frontend
cd frontend && npm run lint && npm run typecheck && npm test && npm run build
```

La descripción del PR debe responder:

1. **Qué** cambia.
2. **Por qué** (qué problema resuelve).
3. **Cómo** se ha comprobado (tests, capturas, `curl`).
4. **Qué queda fuera** (alcance de la propuesta).

Si el cambio afecta al modelo de datos, a la API o a la configuración, hay que
actualizar:

- `docs/SCOPE.md` si cambia el alcance.
- `docs/adr/` si cambia una decisión registrada.
- `docs/API.md` y `docs/DATABASE.md` si cambian los contratos.
- `CHANGELOG.md`, bajo la sección *Sin publicar*.

---

## 7. Reportar un error

Abre una issue con:

- Qué esperabas y qué ha pasado.
- Pasos para reproducirlo.
- Versión de Java, Node, PostgreSQL y el sistema operativo.
- Traza o respuesta de la API, si la hay.
- Capturas, si ayuda.

Un buen informe de error es uno que cualquiera puede reproducir sin hablar
contigo.

---

## 8. Preguntas

Si algo de esta guía no está claro o te bloquea, abre una *discussion*. La
guía también se mejora con las preguntas que llegan.
