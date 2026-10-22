# Scope del proyecto — Task Manager

> **Versión:** 1.0 · **Estado:** aprobado · **Última actualización:** 2026-10-21
>
> Este documento es la fuente de verdad sobre **qué es** el proyecto y **qué no es**.
> Cualquier funcionalidad que no esté descrita aquí queda fuera de alcance, y
> cualquier cambio de alcance debe pasar por una actualización de este documento
> (ver `CHANGELOG.md` y el proceso de Pull Request en `CONTRIBUTING.md`).

---

## 1. Resumen

**Task Manager** es una aplicación web para organizar tareas personales. Permite
crear, consultar, editar, completar y eliminar tareas, con filtro por estado,
prioridad y fecha límite, búsqueda por texto, ordenación, paginación y
estadísticas de progreso.

El proyecto existe con un doble objetivo:

1. **Producto**: ser una herramienta útil y usable en el navegador.
2. **Formación**: ser un caso práctico completo de arquitectura por capas en Java
   con un frontend moderno en React, con pruebas automatizadas y documentación
   suficiente para explicar y defender cada decisión en una entrevista técnica.

---

## 2. Problema que resuelve

Las listas de tareas se dispersan entre notas, papel y herramientas
fragmentadas. El problema habitual no es *registrar* una tarea, sino
**encontrarla**, saber **qué está pendiente** y saber **qué se ha atrasado**.

Task Manager centraliza ese inventario personal con tres garantías:

- **Una única fuente de verdad**: toda tarea vive en PostgreSQL.
- **Estado siempre visible**: cada tarea muestra su estado, su prioridad y si
  está vencida, sin necesidad de abrirla.
- **Coste de entrada cero**: no hay registro, ni login, ni configuración.

---

## 3. Objetivos

### 3.1 Objetivos del producto

| # | Objetivo | Cómo se mide |
| --- | --- | --- |
| OP-1 | Gestionar el ciclo de vida completo de una tarea (crear → editar → completar → eliminar) | Todos los flujos verificados por pruebas end-to-end |
| OP-2 | Saber en un vistazo qué está pendiente y qué está vencido | Insignias de estado visibles en cada tarjeta |
| OP-3 | Encontrar rápido una tarea concreta | Búsqueda por título y descripción |
| OP-4 | Priorizar el trabajo | Campo `priority` con tres niveles, ordenable y filtrable |
| OP-5 | Medir el progreso | Panel de estadísticas (total, pendientes, completadas, vencidas) |

### 3.2 Objetivos técnicos

| # | Objetivo | Criterio |
| --- | --- | --- |
| OT-1 | Arquitectura por capas estricta y legible | El controlador no contiene lógica de negocio; el service no conoce HTTP |
| OT-2 | Validación en cliente **y** servidor | Las reglas se declaran una vez y se aplican en ambos lados |
| OT-3 | Errores con contrato estable | Todas las respuestas de error comparten la misma estructura JSON |
| OT-4 | Pruebas automáticas que se ejecutan sin infraestructura | `mvn test` y `npm test` funcionan sin Docker ni PostgreSQL |
| OT-5 | Documentación que no se queda obsoleta | OpenAPI generado desde el código + README + ADRs |
| OT-6 | Entorno reproducible | `docker compose up` levanta la aplicación completa |

---

## 4. No-objetivos (fuera de alcance)

Esta sección es **deliberadamente explícita**. Un proyecto de alcance pequeño se
defiende mejor cuando dice claramente qué **no** hace.

| # | No-objetivo | Motivo |
| --- | --- | --- |
| NO-1 | **Autenticación y multiusuario** | No hay cuentas ni sesiones. El modelo es de un único usuario implícito. Añadirlo exige un rediseño del modelo de datos (`user_id`) y de la capa de seguridad. Es la primera opción del *roadmap*, no una omisión. |
| NO-2 | **Microservicios, Kafka, Kubernetes** | Sobreingeniería para un único dominio. Un monolito modular es más fácil de explicar y de desplegar. |
| NO-3 | **Subtareas, dependencias o proyectos** | El modelo es una lista plana. Añadir un grafo de tareas cambia el tipo de dato y las consultas. |
| NO-4 | **Notificaciones push / email / recordatorios** | Requieren un planificador, una cola y credenciales de terceros. |
| NO-5 | **Colaboración en tiempo real** | Sin WebSocket ni multiusuario. |
| NO-6 | **Adjuntos o archivos** | Requieren almacenamiento de objetos y validación de contenido. |
| NO-7 | **Sincronización offline** | La aplicación asume conexión para escribir. La lectura diferida se resuelve con caché del cliente. |
| NO-8 | **i18n / multi-idioma** | La interfaz está en un solo idioma. |
| NO-9 | **Auditoría completa (quién cambió qué y cuándo)** | Se conserva `createdAt` y `updatedAt`, pero no un historial de cambios. |

> **Regla de decisión:** si una funcionalidad necesita una tabla de usuarios, un
> broker de mensajes o un clúster, está fuera de alcance hasta que se apruebe
> explícitamente en este documento.

---

## 5. Personas y necesidades

### 5.1 Persona principal — «El planificador»

- **Contexto:** una persona gestiona entre 10 y 200 tareas personales.
- **Objetivo:** saber qué hacer hoy y qué se está atrasando.
- **Dolor:** las tareas se le escapan entre aplicaciones.
- **Expectativa:** abrir la aplicación y entender el estado en menos de 5 segundos,
  sin formación ni configuración previa.

### 5.2 Persona secundaria — «El estudiante de programación»

- **Contexto:** usa el repositorio como material de aprendizaje y portfolio.
- **Objetivo:** entender cada decisión técnica y poder reproducir el proyecto.
- **Expectativa:** README con comandos exactos, código comentado y ADRs que
  expliquen el *porqué*, no solo el *qué*.

### 5.3 Persona terciaria — «El entrevistador técnico»

- **Contexto:** revisa el repositorio antes de una entrevista.
- **Objetivo:** evaluar criterio técnico en pocos minutos.
- **Expectativa:** commits atómicos y descriptivos, pruebas que pasan, y un
  alcance declarado de forma explícita.

---

## 6. Historias de usuario y criterios de aceptación

> Notación: **Dado** (precondición) · **Cuando** (acción) · **Entonces** (resultado).

### US-01 — Listar tareas
**Como** usuario, **quiero** ver todas mis tareas, **para** conocer mi inventario.

- **Dado** que existen tareas, **cuando** abro la aplicación, **entonces** veo
  la lista de tareas con título, estado, prioridad y fecha límite.
- **Dado** que no existen tareas, **when** abro la aplicación, **entonces** veo
  un estado vacío con una llamada a la acción.
- **Dado** que hay más de `size` tareas, **when** se carga la lista, **entonces**
  veo los controles de paginación con el total de resultados.

### US-02 — Crear una tarea
**Como** usuario, **quiero** registrar una tarea, **para** no olvidarla.

- **Dado** un título no vacío, **when** envío el formulario, **entonces** la
  tarea se crea y aparece en la lista.
- **Dado** un título vacío o de más de 200 caracteres, **when** envío el
  formulario, **entonces** veo un error junto al campo y **no** se envía nada.
- **Dado** un formulario válido, **when** la API responde con error, **entonces**
  veo un mensaje de error y el formulario conserva los datos introducidos.

### US-03 — Editar una tarea
- **Dado** una tarea existente, **when** pulso «Editar», **entonces** el
  formulario se rellena con sus valores.
- **Dado** el formulario de edición, **when** guardo los cambios, **entonces** la
  lista refleja la nueva información.
- **Dado** que cancelo la edición, **entonces** el formulario vuelve a su estado
  inicial sin guardar cambios.

### US-04 — Completar / reabrir una tarea
- **Dado** una tarea pendiente, **when** la marco como completada, **entonces**
  aparece con el estilo de completada y la insignia «Completada».
- **Dado** una tarea completada, **when** la reabro, **entonces** vuelve al
  estado pendiente.
- **Dado** una tarea vencida, **when** la marco como completada, **entonces**
  deja de mostrarse como vencida.

### US-05 — Eliminar una tarea
- **Dado** una tarea, **when** pulso «Eliminar», **entonces** aparece un diálogo
  de confirmación con su título.
- **Dado** el diálogo abierto, **when** confirmo, **entonces** la tarea se
  elimina de la base de datos.
- **Dado** el diálogo abierto, **when** cancelo, **entonces** no se borra nada.

### US-06 — Filtrar tareas
- **Como** usuario, **quiero** filtrar por estado, **para** concentrarme en un
  subconjunto.
- **Dado** el filtro «Pendientes», **when** lo selecciono, **entonces** la API
  devuelve solo tareas pendientes.
- **Dado** un filtro por prioridad, **cuando** lo combino con el de estado,
  **entonces** ambos criterios se aplican.

### US-07 — Buscar tareas
- **Como** usuario, **quiero** buscar por texto, **para** encontrar una tarea
  concreta sin recorrer la lista.
- **Dado** un término de búsqueda, **when** lo escribo, **entonces** los
  resultados se actualizan sin pulsar ningún botón.
- **Dado** que el término no coincide con nada, **entonces** veo un estado
  vacío con el texto del término.

### US-08 — Ordenar tareas
- **Como** usuario, **quiero** ordenar por fecha, título o prioridad, **para**
  ver primero lo que me interesa.
- **Dado** un criterio de ordenación, **when** lo cambio, **entonces** la lista
  se reordena de forma estable.

### US-09 — Ver estadísticas
- **Como** usuario, **quiero** ver totales, **para** medir mi progreso.
- **Dado** la aplicación cargada, **entonces** veo el total de tareas, cuántas
  están pendientes, cuántas completadas y cuántas vencidas.

### US-10 — Recibir feedback
- **Como** usuario, **quiero** saber si mis acciones se guardaron, **para** no
  quedarme con la duda.
- **Dado** una acción exitosa, **entonces** aparece una notificación de éxito.
- **Dado** una acción fallida, **entonces** aparece una notificación de error
  con el motivo devuelto por la API.
- **Dado** una carga en curso, **entonces** veo un indicador de carga.

### US-11 — Usar la aplicación en el móvil
- **Dado** una pantalla de móvil, **cuando** uso la aplicación, **entonces** la
  interfaz se adapta a una columna y los botones siguen siendo alcanzables.

---

## 7. Alcance funcional

### 7.1 Módulo «Tareas» (CRUD completo)

| Función | Reglas de negocio |
| --- | --- |
| Crear | Título obligatorio (1–200). Descripción opcional (≤ 2000). Fecha límite opcional. Prioridad opcional (`LOW`, `MEDIUM`, `HIGH`, por defecto `MEDIUM`). |
| Listar | Orden por defecto: pendientes primero y, dentro de cada grupo, más recientes primero. |
| Leer | Devuelve la tarea completa o 404. |
| Actualizar | Sustitución completa (PUT). Respeta las mismas validaciones que la creación. |
| Completar | Alterna el estado, o lo fija de forma explícita si se envía el cuerpo. |
| Eliminar | Borrado físico. Devuelve 204. |

### 7.2 Módulo «Consultas»

| Función | Reglas |
| --- | --- |
| Filtro por estado | `ALL` (por defecto), `PENDING`, `COMPLETED` |
| Filtro por prioridad | `ALL`, `LOW`, `MEDIUM`, `HIGH` |
| Búsqueda | Case-insensitive, sobre título **y** descripción |
| Ordenación | `createdAt`, `dueDate`, `title`, `priority`; ascendente o descendente |
| Paginación | `page` (base 0) y `size` (1–100, por defecto 20) |
| Estadísticas | Totales de tareas, pendientes, completadas y vencidas |

### 7.3 Módulo «Interfaz»

| Elemento | Comportamiento |
| --- | --- |
| Cabecera | Nombre de la aplicación y subtítulo |
| Formulario | Crear y editar en el mismo componente; validación en el cliente |
| Lista | Tarjetas con título, descripción, prioridad, fecha límite y fecha de creación |
| Estados visuales | Insignia «Completada», insignia «Vencida», estilo tachado |
| Filtros | Estado, prioridad, búsqueda y ordenación |
| Paginación | Controles de página anterior/siguiente con el total |
| Estadísticas | Panel con los cuatro totales |
| Confirmación | Diálogo modal antes de eliminar |
| Feedback | Notificaciones de éxito y de error, dismissibles |
| Carga | Indicador mientras se consultan los datos |
| Responsive | Dos columnas en escritorio, una columna en móvil |

---

## 8. Alcance no funcional

### 8.1 Rendimiento
- La API responde en menos de 200 ms con datasets de hasta 10 000 tareas.
- La primera respuesta de la lista por debajo de 500 ms en desarrollo local.
- Las consultas usan índices sobre las columnas filtradas y ordenadas.

### 8.2 Usabilidad
- Validación inmediata en el cliente con mensajes en español.
- Ninguna acción destructiva ocurre sin confirmación.
- La interfaz es utilizable solo con teclado (foco visible, `aria-label` en los
  botones de icono).

### 8.3 Accesibilidad
- Contraste de color conforme a WCAG AA en el texto principal y en las insignias.
- Uso de roles ARIA (`alertdialog`, `status`, `group`, `alert`).
- Navegación por teclado en todos los controles.

### 8.4 Mantenibilidad
- Cero duplicación de reglas de validación entre capas.
- Ningún controlador contiene lógica de negocio.
- Los nombres de clases y métodos describen la intención.
- Cero TODO/FIXME pendientes en el código publicado.

### 8.5 Seguridad
- Las credenciales de la base de datos solo viajan por variables de entorno.
- El repositorio no contiene secretos (verificado en el pipeline de CI).
- CORS con lista blanca explícita de orígenes.
- Sin exposición de trazas de pila al cliente en errores de producción.

### 8.6 Portabilidad
- Java 21, Maven 3.9+, Node 18+.
- El proyecto se levanta en Windows, Linux y macOS con los mismos comandos.
- La suite de pruebas se ejecuta sin Docker ni base de datos externa.

---

## 9. Modelo de datos

```
┌──────────────────────────────┐
│            tasks             │
├──────────────────────────────┤
│ id           bigint   PK     │  Auto-incremental
│ title        varchar(200)    │  NOT NULL
│ description  varchar(2000)   │  NULL
│ due_date     date            │  NULL
│ priority     varchar(10)     │  NOT NULL, por defecto MEDIUM
│ completed    boolean         │  NOT NULL, por defecto false
│ created_at   timestamptz     │  NOT NULL
│ updated_at   timestamptz     │  NOT NULL
└──────────────────────────────┘
```

**Invariantes**

1. `title` nunca es `NULL` ni una cadena en blanco.
2. `completed` y `priority` nunca son `NULL`.
3. `created_at` y `updated_at` los genera el servidor, nunca el cliente.
4. `due_date` puede estar en el pasado: una tarea vencida es un estado válido,
   no un error.

**Índices**

| Índice | Propósito |
| --- | --- |
| `idx_tasks_completed` | Filtro por estado |
| `idx_tasks_due_date` | Filtro y ordenación por fecha límite |
| `idx_tasks_priority` | Filtro por prioridad |
| `idx_tasks_created_at` | Ordenación por fecha de creación |

---

## 10. Contrato de la API

Prefijo: `/api/tasks`. Detalle completo en `docs/API.md` y en Swagger UI
(`/swagger-ui.html`).

| Método | Ruta | Respuesta |
| --- | --- | --- |
| `GET` | `/api/tasks` | `200` lista paginada |
| `GET` | `/api/tasks/{id}` | `200` · `404` |
| `POST` | `/api/tasks` | `201` + `Location` · `400` |
| `PUT` | `/api/tasks/{id}` | `200` · `400` · `404` |
| `PATCH` | `/api/tasks/{id}/complete` | `200` · `404` |
| `DELETE` | `/api/tasks/{id}` | `204` · `404` |
| `GET` | `/api/tasks/statistics` | `200` |

**Formato de error (único para toda la API)**

```json
{
  "status": 400,
  "message": "Validation failed for: title",
  "path": "/api/tasks",
  "timestamp": "2026-01-01T10:00:00Z",
  "fieldErrors": { "title": "The title is mandatory" }
}
```

---

## 11. Restricciones y supuestos

### Restricciones
- **R-1** El entorno de ejecución es Java 21 (LTS).
- **R-2** La base de datos es PostgreSQL 14 o superior.
- **R-3** El navegador destino es cualquier navegador evergreen moderno.
- **R-4** El proyecto se despliega como un monolito: un backend y un frontend
  estático.

### Supuestos
- **S-1** Existe un único usuario implícito. La aplicación no pide identificación.
- **S-2** Los datos son de carácter personal y no requieren cifrado en reposo más
  allá del que ofrece el sistema de archivos o el del proveedor de la base de
  datos.
- **S-3** El volumen esperado está entre 10 y 10 000 tareas: no hace falta
  particionado ni archivo por antigüedad.

---

## 12. Definición de «Hecho» (Definition of Done)

Una funcionalidad está terminada cuando **todas** estas condiciones se cumplen:

- [ ] El código compila (`mvn clean package` y `npm run build`).
- [ ] ESLint y `tsc --noEmit` no reportan errores.
- [ ] Hay pruebas que cubren el camino feliz **y** el de error.
- [ ] Los endpoints afectados devuelven los códigos HTTP documentados.
- [ ] Swagger UI refleja el cambio sin tocar la documentación a mano.
- [ ] El README se actualiza si cambia un comando o una variable de entorno.
- [ ] El commit es atómico y el mensaje explica el *porqué*.
- [ ] La CI en verde.

---

## 13. Métricas de éxito

| Métrica | Objetivo |
| --- | --- |
| Cobertura de pruebas del backend | ≥ 80 % de las clases de `service` y `controller` |
| Pruebas de la suite | 100 % en verde en cada commit |
| Errores de consola del navegador | 0 en los flujos principales |
| Lighthouse (accesibilidad) | ≥ 90 |
| Tiempo hasta el primer render útil | < 2 s en local |

---

## 14. Roadmap

> Ninguna de estas historias está iniciada. Verifican el alcance definido arriba.

### Fase 1 — solidly (entregada en la v1.0)
CRUD completo, filtro por estado, búsqueda, ordenación, paginación,
prioridades, estadísticas, notificaciones, tema oscuro, pruebas
unitarias y de integración, CI, Docker, documentación completa.

### Fase 2 — Multiusuario (v2.0)
Autenticación (Spring Security + JWT), tabla de usuarios, `user_id` en `tasks`,
autorización por recurso. Requiere revisar **NO-1**.

### Fase 3 — Dominio más rico (v3.0)
Categorías y etiquetas, subtareas, recordatorios, exportación a CSV/ICS.
Requiere revisar **NO-3** y **NO-4**.

---

## 15. Riesgos

| Riesgo | Probabilidad | Impacto | Mitigación |
| --- | --- | --- | --- |
| El alcance se infla con funcionalidades secundarias | Alta | Medio | Este documento + ADRs; cualquier ampliación se documenta antes de codificarse |
| La validación del cliente y la del servidor divergen | Media | Alto | Las reglas se declaran en un único sitio por lado y ambas suites las cubren |
| `ddl-auto` genera un esquema divergente entre entornos | Media | Alto | Migraciones versionadas (Flyway) antes de producción |
| La lista se degrada con volúmenes grandes | Baja | Medio | Índice + paginación ya contemplados |
| Falta de Docker en el entorno de desarrollo | Media | Bajo | La suite de pruebas usa H2 en memoria y no requiere contenedores |

---

## 16. Glosario

| Término | Significado en este proyecto |
| --- | --- |
| Tarea | Unidad de trabajo con título, descripción opcional, fecha límite opcional, prioridad y estado. |
| Pendiente | Tarea con `completed = false`. |
| Vencida | Tarea pendiente cuya `dueDate` es anterior a hoy. |
| Filtro | Criterio que reduce el conjunto de tareas devuelto por la API. |
| Criterio de ordenación | Columna y sentido con el que se ordenan los resultados. |

---

## 17. Referencias

- `README.md` — instalación, ejecución y comandos exactos.
- `docs/API.md` — referencia completa de la API.
- `docs/DATABASE.md` — esquema, índices y migraciones.
- `docs/TESTING.md` — estrategia de pruebas y cómo ejecutarlas.
- `docs/adr/` — decisiones de arquitectura con su contexto y sus consecuencias.
- `CHANGELOG.md` — historial de cambios del proyecto.
