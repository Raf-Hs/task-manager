# ADR-0008 — Proxy de Vite en desarrollo

- **Estado:** Aceptada
- **Fecha:** 2026-10-21
- **Ámbito:** frontend, infraestructura

## Contexto

Durante el desarrollo, el frontend corre en `http://localhost:5173` y la API
en `http://localhost:8080`. Son **orígenes distintos** (distinto puerto =
distinto origen para el navegador), así que cualquier petición `fetch`/Axios
desde `5173` a `8080` es una petición **cross-origin** y el navegador la
bloquea salvo que el servidor envíe cabeceras CORS.

El backend ya configura CORS correctamente
(`CorsConfig` + `CORS_ALLOWED_ORIGINS`), pero hay dos formas de resolverlo en
desarrollo:

1. **CORS real**: el frontend llama a `http://localhost:8080/api/tasks`
   directamente y el navegador pide una preflight `OPTIONS`.
2. **Proxy**: el frontend llama a `/api/tasks` (ruta relativa) y el servidor de
   Vite reenvía la petición a `http://localhost:8080` por el servidor Node. El
   navegador solo ve un origen: `localhost:5173`.

## Decisión

**En desarrollo se usa el proxy de Vite.** El frontend llama siempre a la ruta
relativa `/api`.

```ts
// vite.config.ts
server: {
  port: 5173,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

En Axios, la base URL por defecto es relativa:

```ts
const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api'
```

La variable `VITE_API_BASE_URL` permite apuntar a otro host (por ejemplo, un
backend desplegado) sin tocar el código.

**La configuración de CORS del backend se mantiene y es obligatoria** para el
caso de producción, donde el frontend se sirve como estático y la API vive en
otro origen. El proxy es una comodidad del desarrollo, no el sustituto de
CORS.

## Alternativas consideradas

| Alternativa | Por qué se descartó |
| --- | --- |
| **Llamar a `http://localhost:8080` directamente** | Funciona, pero añade una preflight `OPTIONS` en cada petición y hace que el frontend dependa de un host y un puerto compilados. También rompe con `file://` o con `preview` en otro puerto. |
| **Desactivar CORS en el backend** | Inaceptable: el navegador lo rechazaría, y en producción sería un agujero de seguridad. |
| **`vite-plugin-proxy` u otros plugins** | El proxy es una función nativa de Vite desde la versión 4. Un plugin solo añadiría una dependencia. |
| **Usar `localhost` en todas partes en lugar de `127.0.0.1`** | `localhost` y `127.0.0.1` son orígenes distintos para el navegador. Ambos están en la lista de permitidos de CORS. |

## Consecuencias

### Positivas
- Cero peticiones cross-origin en desarrollo: ni preflights ni errores de CORS
  en la consola.
- El frontend no conoce el puerto ni el host del backend: la URL base es
  relativa y se puede cambiar con una variable de entorno.
- El mismo código funciona en `npm run dev` y en `npm run preview`.
- El proxy está definido en un único sitio (`vite.config.ts`).

### Negativas
- Un salto extra en cada petición (Vite → backend). Irrelevante en desarrollo.
- Si alguien abre el frontend con `vite preview` **sin** el proxy, las
  peticiones fallan. Mitigación: la documentación explica que en producción
  hay que configurar `VITE_API_BASE_URL` o un proxy en el servidor estático.
- La configuración de CORS del backend y la del proxy tienen que coincidir.
  Ambas están listas en `.env.example` y en el README.
