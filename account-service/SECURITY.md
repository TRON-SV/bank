# Seguridad de account-service

## Configuración de JWT

El servicio recibe `Authorization: Bearer <token>` y valida la firma RSA
(RS256), el emisor (`iss`), la audiencia (`aud`), la expiración (`exp`) y el
inicio de vigencia (`nbf`, cuando está presente). Spring Security obtiene las
claves públicas desde JWKS y transforma `scope`/`scp` en autoridades `SCOPE_`.

Antes de arrancar, configura en el entorno del proceso Java:

| Variable | Valor requerido |
| --- | --- |
| `JWT_ISSUER_URI` | Identificador exacto del emisor confiable, igual a `iss` |
| `JWT_JWK_SET_URI` | URL del documento JWKS de ese emisor |
| `JWT_AUDIENCE` | Audiencia destinada a esta API, por ejemplo `account-service` |

Usar HTTPS para el emisor y JWKS en despliegues. No aceptar estas URLs desde
la petición ni almacenar claves privadas en account-service. Un archivo
`.env` no se carga automáticamente en Spring: configura las variables en
PowerShell, IntelliJ o el entorno del contenedor.

Se especifican issuer y JWKS juntos para evitar descubrir metadatos durante
el arranque; la descarga de claves ocurre al validar tokens. Sin configuración
válida el servicio no debe arrancar. No hay un emisor de desarrollo implícito.
`auth-service` todavía no emite tokens: conectar un emisor real es el siguiente
paso de integración.

## Política HTTP

- GET `/actuator/health`: público sin token.
- POST `/api/v1/accounts`: requiere `accounts:create`.
- Otras rutas: denegadas.
- Token ausente o inválido: 401. Token válido sin permiso: 403.
- Sesiones STATELESS, Basic y formulario desactivados; CSRF desactivado para
  este contrato que usa Bearer en cabecera, sin autenticación por cookies.

El permiso de creación no vincula todavía `customerId` con el sujeto (`sub`).
Antes de permitir autoservicio de clientes hay que definir y aplicar esa
regla; actualmente el scope representa permiso para crear cuentas.

## Verificación

Desde esta carpeta, con Java 26 y Docker disponibles:

```powershell
.\mvnw.cmd clean verify
```

El perfil `test` configura un emisor ficticio exclusivamente para las pruebas.
`AccountSecurityTests` publica JWKS en un servidor HTTP de loopback con puerto
dinámico y claves RSA efímeras. Envía tokens firmados por la cadena real de
seguridad: comprueba 201 autorizado, 403 sin scope, 401 sin token, firma ajena,
issuer o audience incorrectos, token expirado y token malformado. El servicio
de negocio se simula en esta clase; las otras pruebas verifican persistencia
y negocio usando PostgreSQL efímero en Testcontainers.
