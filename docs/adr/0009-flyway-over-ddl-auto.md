# ADR-0009 — Flyway en lugar de `ddl-auto=update`

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend, infraestructura

## Contexto

El esquema de la base de datos puede gestionarse de dos formas:

- **`ddl-auto=update`**: Hibernate compara el modelo de Java con el esquema real
  y genera los `ALTER TABLE` que falten en el arranque. Cero mantenimiento,
  cero ficheros de migración.
- **Herramienta de migraciones** (Flyway o Liquibase): los cambios de esquema
  son ficheros SQL versionados que se aplican en orden.

`ddl-auto=update` es cómodo, pero tiene un problema serio: **no sabe lo que
tiene que deshacer**. Si un campo se renombra en Java, Hibernate no renombra
la columna: añade una nueva y deja la vieja. Si un tipo cambia de `varchar` a
`text`, la operación puede fallar o truncar datos. Y el esquema resultante
depende del historial de despliegues, no del código.

Además, en producción el arranque de la aplicación no debería decidir la forma
del esquema: eso debería ser un paso explícito y auditable.

## Decisión

**Se adopta Flyway.** El esquema se define en ficheros SQL versionados que
Flyway aplica y verifica en el arranque.

```
backend/src/main/resources/db/migration/
├── V1__create_tasks_table.sql
├── V2__add_priority_to_tasks.sql
├── V3__add_updated_at_to_tasks.sql
└── V4__add_task_indexes.sql
```

Y la configuración:

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate    # comprueba, no modifica
  flyway:
    enabled: true
    locations: classpath:db/migration
```

Con `ddl-auto: validate`, Hibernate **comprueba** que el esquema coincide con
el modelo de Java y **falla el arranque** si no coincide. Es una red de
seguridad: si alguien cambia la entidad y olvida la migración, la aplicación
no arranca en silencio con un esquema desalineado.

### La base de datos de desarrollo

Para no romper el flujo de trabajo, se ofrece un perfil `local` que mantiene
`ddl-auto=update`. Se usa solo en desarrollo y está documentado como tal.

### Estrategia de nombres

Flyway versiona por número (`V1`, `V2`...). Los ficheros **nunca** se editan
una vez aplicados: se añade uno nuevo. Esta regla es la que garantiza que el
historial sea reproducible.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **`ddl-auto=update` siempre** | No deshace cambios, genera un esquema no reproducible y deja el control del esquema en manos de la aplicación en producción. |
| **`ddl-auto=validate` + Liquibase** | Liquibase usa XML/YAML en vez de SQL. El SQL es más legible para un revisor y más fácil de generar con herramientas. Flyway es más sencillo de configurar. |
| **Scripts SQL aplicados a mano** | No hay registro de qué se aplicó, ni verificación de que el esquema es el esperado, y el proceso depende de la memoria humana. |
| **Hibernate Schema Export** | Genera DDL no versionado. Es una herramienta de desarrollo, no de despliegue. |

## Consecuencias

### Positivas
- El esquema es **reproducible**: el mismo conjunto de migraciones produce el
  mismo esquema en cualquier entorno.
- Los cambios de esquema son revisables en un pull request, como cualquier otro
  código.
- El arranque valida el esquema y falla de forma ruidosa si no encaja.
- Se puede consultar el historial: la tabla `flyway_schema_history` registra
  qué migración se aplicó y cuándo.

### Negativas
- Hay que escribir la migración **antes** de cambiar la entidad, o el arranque
  falla. Es exactamente el comportamiento buscado.
- Un `ALTER TABLE` sobre una tabla grande puede tardar. No es un problema con
  el volumen de este proyecto.
- Hay que aprender la nomenclatura de Flyway (`V<n>__<descripcion>.sql`).

### Reglas de la casa

1. Una migración por cambio logical.
2. Nunca editar una migración ya aplicada.
3. Las migraciones deben ser reversibles a mano cuando sea posible, o
   documentar en el fichero cómo revertirla.
