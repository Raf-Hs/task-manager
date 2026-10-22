# ADR-0004 — Inyección de dependencias por constructor

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** backend

## Contexto

Spring ofrece varias formas de inyectar dependencias: por campo (`@Autowired`
sobre el atributo), por setter o por constructor.

La inyección por campo tiene un problema que no se ve hasta que una clase
tiene quince campos: **el objeto se puede construir en un estado inválido**.
Con `@Autowired` por campo, `new TaskServiceImpl()` compila y no falla, pero
`taskRepository` es `null` y el fallo aparece en la primera llamada, lejos de
la causa.

## Decisión

Todas las dependencias se inyectan por constructor, se declaran `final` y las
clases no tienen constructor por defecto:

```java
@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;

    public TaskServiceImpl(TaskRepository taskRepository, TaskMapper taskMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
    }
}
```

Consecuencias directas:

- `final` + constructor único ⇒ **inmutabilidad de las dependencias**: no
  pueden cambiarse por error después de construir el objeto.
- Los tests construyen la clase con dobles explícitos, sin necesidad de
  reflexión ni de annotation processors.
- El objeto nunca está en un estado «a medio construir».

En el frontend se aplica el mismo principio: los hooks reciben sus
dependencias por parámetro y no usan un contenedor de inyección.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Inyección por campo** | Permite objetos inválidos, oculta las dependencias reales de la clase y exige reflexión para testear. |
| **Inyección por setter** | Permite mutar el estado después de construir y deja la puerta abierta a dependencias `null`. |
| **Inyección por constructor con `@Autowired` opcional** | Un constructor con `@Autowired(required = false)` equivale a dejar la dependencia en `null`: es un error silencioso. |
| **Localizador de servicios (service locator)** | Oculta las dependencias, impide el análisis estático y hace imposible saber qué usa una clase. |
| **Lombok `@RequiredArgsConstructor`** | Genera el constructor, pero esconde las dependencias de la lectura del código y añade un procesador de anotaciones. |

## Consecuencias

### Positivas
- Las dependencias de cada clase se leen en la primera línea del fichero.
- Los tests son explícitos: se ve exactamente qué doble se inyecta.
- `final` documenta la intención y activa las optimizaciones del compilador.
- Funciona sin contenedor de inyección: la clase se puede instanciar en un test
  sin contexto de Spring.

### Negativas
- Más líneas que la inyección por campo (el constructor ocupa 4-5 líneas).
- Con muchas dependencias, el constructor se hace largo. En este proyecto el
  máximo es de dos. Si creciera, sería la señal para reconsiderar el diseño,
  no para añadir un contenedor.
