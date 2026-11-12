# ms-notificaciones

## Seguridad

El servicio valida JWT Bearer emitidos por `ms-users`. Configura `JWT_SECRET`
con el mismo valor en ambos servicios (mínimo 32 bytes) y `JWT_ISSUER` con el
mismo emisor; el valor predeterminado es `reservas-ms-users`. No se proporciona
un secreto por defecto.

- `POST /contact` es público para recibir consultas del formulario.
- Leer contactos, y cualquier operación de plantillas o notificaciones,
  requiere el rol `ADMINISTRATOR`.
- La documentación OpenAPI y el health check son públicos.
- El resto de rutas requiere un JWT válido.

## Caché Redis

El servicio usa la instancia Redis compartida configurada por `REDIS_HOST`,
`REDIS_PORT` y `REDIS_PASSWORD`. Define un prefijo `CACHE_KEY_PREFIX` único por
servicio para aislar sus dominios. Los TTL por dominio se configuran con
`CACHE_TTL_EMAIL_TEMPLATES`, `CACHE_TTL_EMAIL_TEMPLATES_BY_CODE`,
`CACHE_TTL_NOTIFICATIONS` y `CACHE_TTL_CONTACT_INQUIRIES`;
`CACHE_DEFAULT_TTL` aplica a dominios sin TTL propio.

Se almacenan lecturas individuales por ID o código, no resultados completos de
búsquedas ni páginas. Las escrituras invalidan únicamente el dominio afectado.
Las operaciones de caché se registran en el log sin imprimir valores. Las
credenciales y entidades de usuario no se almacenan en caché.
