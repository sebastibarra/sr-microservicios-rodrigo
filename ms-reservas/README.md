# ms-reservas

## Seguridad

El servicio valida JWT Bearer emitidos por `ms-users`. Configura `JWT_SECRET`
con el mismo valor en ambos servicios (mínimo 32 bytes) y `JWT_ISSUER` con el
mismo emisor; el valor predeterminado es `reservas-ms-users`. No se proporciona
un secreto por defecto.

- Leer reservas y tipos de consulta requiere cualquier rol autenticado.
- Crear reservas permite `STUDENT` y `ADMINISTRATOR`.
- Modificar o cancelar reservas permite `STUDENT`, `PROFESSOR` y
  `ADMINISTRATOR`.
- Crear, modificar o desactivar tipos de consulta requiere `ADMINISTRATOR`.
- La autorización se aplica por rol y ruta; no comprueba propiedad individual
  de una reserva. La documentación OpenAPI y el health check son públicos.

## Caché Redis

El servicio usa la instancia Redis compartida configurada por `REDIS_HOST`,
`REDIS_PORT` y `REDIS_PASSWORD`. Define un prefijo `CACHE_KEY_PREFIX` único por
servicio para aislar sus dominios. Los TTL por dominio se configuran con
`CACHE_TTL_RESERVATIONS`, `CACHE_TTL_RESERVATION_DETAILS`,
`CACHE_TTL_CONSULTATION_TYPES`, `CACHE_TTL_ACTIVE_CONSULTATION_TYPES`,
`CACHE_TTL_SETTINGS`; `CACHE_DEFAULT_TTL` aplica a
dominios sin TTL propio.

Se almacenan lecturas individuales por identificador, no resultados completos
de búsquedas ni páginas. Las escrituras invalidan únicamente el dominio
afectado. Las operaciones de caché se registran en el log sin imprimir valores.
No se almacenan en caché entidades que contienen credenciales.
