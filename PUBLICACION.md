# Preparación para publicación

La aplicación pública y el backoffice se despliegan juntos como una aplicación Java 21 / Spring Boot.

## Configuración de producción

- `SPRING_PROFILES_ACTIVE=prod`
- `SUPABASE_DB_URL`: URL JDBC del Session pooler de Supabase, puerto 5432, sin incluir la contraseña.
- `SUPABASE_DB_USERNAME`: usuario PostgreSQL proporcionado por Supabase.
- `SUPABASE_DB_PASSWORD`: secreto PostgreSQL guardado exclusivamente en el alojamiento.

La contraseña de PostgreSQL es distinta de las claves API de Supabase.

## Estado

- El perfil local mantiene MariaDB/XAMPP.
- El perfil `prod` usa PostgreSQL cifrado y el esquema privado `connectify`.
- Los datos de demostración y la ruta pública `/setup` se desactivan en producción.
- IZIPay sigue desactivado hasta validar pagos y notificaciones con el dominio final.

## Antes de publicar

1. Convertir y probar el respaldo de MariaDB en PostgreSQL conservando identificadores y relaciones.
2. Crear el esquema `connectify` mediante una migración versionada.
3. Migrar las imágenes de `uploads` a almacenamiento persistente.
4. Revisar CSRF, permisos por rol, aforo, pagos y uso único de entradas.
5. Probar inicio de sesión, eventos, compras y validación de tickets.
6. Configurar `www.showyouplay.com` solo después de verificar el alojamiento con HTTPS.
