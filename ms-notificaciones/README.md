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
