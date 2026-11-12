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
