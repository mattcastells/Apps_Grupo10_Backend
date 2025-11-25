# RitmoFit API Backend - Grupo 10

API Rest para la aplicación de gestión de gimnasios RitmoFit. Este backend maneja la lógica de negocio, la persistencia de datos y la comunicación con servicios externos para la aplicación móvil del TPO de la materia Desarrollo de Aplicaciones I.

## Tecnologías Utilizadas
- Java 17
- Spring Boot 3.5.5
- Apache Maven 3.8+
- MongoDB Atlas
- Gmail SMTP (envío de emails vía JavaMail)
- JWT (autenticación)

---

## Configuración del Entorno

El proyecto requiere la configuración de variables de entorno para manejar credenciales de servicios externos de forma segura. Sin estas variables, la aplicación no podrá iniciarse correctamente.

### 1. Crear archivo de variables de entorno

Copia el archivo de ejemplo `.env.example` y renómbralo a `.env` en la raíz del proyecto:

```bash
cp .env.example .env
```

**IMPORTANTE:** El archivo `.env` está en `.gitignore` y **NO debe subirse a Git** bajo ninguna circunstancia.

### 2. Variables de Entorno Requeridas

Edita el archivo `.env` y completa las siguientes variables:

#### **MONGO_PASSWORD**
Contraseña de MongoDB Atlas.

**Dónde conseguirla:**
1. Ve a https://cloud.mongodb.com
2. Inicia sesión en tu cuenta
3. Ve a "Database Access" > Selecciona tu usuario
4. Si no recuerdas la contraseña, puedes resetearla desde el panel de MongoDB Atlas

```env
MONGO_PASSWORD=tu_password_de_mongodb_aqui
```

---

#### **GMAIL_USERNAME**
Tu dirección de email de Gmail.

```env
GMAIL_USERNAME=tucorreo@gmail.com
```

---

#### **GMAIL_APP_PASSWORD**
Contraseña de aplicación generada desde tu cuenta de Google (NO tu contraseña normal).

**IMPORTANTE:** Debes generar una **"Contraseña de aplicación"** específica para que el backend pueda enviar emails a través de Gmail SMTP.

**Cómo obtenerla paso a paso:**

1. **Ve a tu cuenta de Google:**
   - Visita https://myaccount.google.com/security

2. **Activa la verificación en 2 pasos:**
   - Si no la tienes activada, busca **"Verificación en 2 pasos"** y actívala
   - Sigue los pasos de Google para configurarla (SMS, llamada, o app Authenticator)

3. **Genera una contraseña de aplicación:**
   - Una vez activada la verificación en 2 pasos, busca **"Contraseñas de aplicaciones"** (App passwords)
   - Si no la encuentras, ve directamente a: https://myaccount.google.com/apppasswords
   - Haz clic en **"Seleccionar app"** y elige **"Otra (nombre personalizado)"**
   - Escribe: `RitmoFit Backend`
   - Haz clic en **"Generar"**

4. **Copia la contraseña:**
   - Google te mostrará una contraseña de 16 caracteres (formato: `xxxx xxxx xxxx xxxx`)
   - **Cópiala inmediatamente** (solo se muestra una vez)
   - Pégala en el `.env` **SIN ESPACIOS** (ej: `abcdefghijklmnop`)

**Documentación oficial:**
- https://support.google.com/accounts/answer/185833

```env
GMAIL_APP_PASSWORD=abcdefghijklmnop
```

**NOTA:** Esta contraseña es diferente a tu contraseña de Gmail. Es específica para aplicaciones que necesitan acceder a tu cuenta.

---

#### **JWT_SECRET_KEY**
Clave secreta para firmar los tokens JWT.

**Cómo generarla:**

Opción 1 - Usando OpenSSL (Linux/macOS):
```bash
openssl rand -base64 64
```

Opción 2 - Usando generador online:
- Ve a https://generate-secret.vercel.app/64
- Copia la clave generada

**IMPORTANTE:** Debe ser una cadena larga y compleja (mínimo 64 caracteres). Nunca compartas esta clave ni la subas a Git.

```env
JWT_SECRET_KEY=cambia_esto_por_una_clave_secreta_muy_larga_y_compleja_de_minimo_64_caracteres
```

---

#### **JWT_EXPIRATION_MS**
Tiempo de expiración del token JWT en milisegundos.

Valores comunes:
- `86400000` = 24 horas (1 día)
- `604800000` = 7 días (1 semana)
- `2592000000` = 30 días (1 mes)
- `1000000000` = ~11.5 días (valor por defecto)

```env
JWT_EXPIRATION_MS=1000000000
```

---

### 3. Configuración Alternativa (Variables de Sistema)

Si prefieres no usar archivo `.env`, puedes configurar las variables de entorno directamente en tu sistema:

#### Opción 1: En el IDE (IntelliJ IDEA)
1. Ve a la configuración de ejecución (Run/Debug Configurations)
2. Busca la sección **Environment variables**
3. Añade todas las variables mencionadas arriba

#### Opción 2: En la terminal (Linux/macOS)
```bash
export MONGO_PASSWORD="tu_contraseña"
export GMAIL_USERNAME="tucorreo@gmail.com"
export GMAIL_APP_PASSWORD="tu_app_password"
export JWT_SECRET_KEY="tu_clave_secreta_jwt"
export JWT_EXPIRATION_MS="1000000000"
```

#### Opción 3: En la terminal (Windows PowerShell)
```powershell
$env:MONGO_PASSWORD="tu_contraseña"
$env:GMAIL_USERNAME="tucorreo@gmail.com"
$env:GMAIL_APP_PASSWORD="tu_app_password"
$env:JWT_SECRET_KEY="tu_clave_secreta_jwt"
$env:JWT_EXPIRATION_MS="1000000000"
```

---

## Cómo Levantar el Proyecto

### 1. Verificar configuración

Asegúrate de que todas las variables de entorno estén correctamente configuradas en tu archivo `.env` o en tu sistema.

### 2. Instalar dependencias

```bash
mvn clean install
```

### 3. Ejecutar la aplicación

```bash
mvn spring-boot:run
```

Si todo ha salido bien, verás en la consola los logs de Spring Boot indicando que la aplicación se ha iniciado. Por defecto, el servidor se ejecutará en http://localhost:8080.

---

## Verificación del Servicio de Email

Para verificar que el servicio de Gmail SMTP está configurado correctamente:

1. **Verifica las variables de entorno:**
   - Asegúrate de que `GMAIL_USERNAME` esté configurada con tu email
   - Verifica que `GMAIL_APP_PASSWORD` sea la contraseña de aplicación (NO tu contraseña normal)

2. **Prueba el registro de usuario:**
   - Haz una petición POST a `/api/v1/auth/register`
   - Deberías recibir un email con el código OTP en tu bandeja de entrada

3. **Revisa los logs:**
   - Si hay algún error, aparecerá en la consola
   - Los logs indicarán si el email se envió correctamente
   - Mensaje exitoso: `✅ Email enviado exitosamente a ... a través de Gmail SMTP`

---

## Limitaciones de Gmail SMTP

**Gmail SMTP tiene las siguientes limitaciones:**

- ✅ **Puedes enviar emails a cualquier destinatario** (no solo a ti mismo)
- ⚠️ **Límite diario:** 500 emails por día (suficiente para desarrollo y pequeñas producciones)
- ⚠️ **Límite por hora:** Aproximadamente 100 emails por hora
- ✅ **Gratis:** No tiene costo, solo necesitas una cuenta de Gmail

**Recomendaciones:**
- Para desarrollo y testing: Gmail SMTP es ideal ✅
- Para producción con alto volumen: Considera servicios dedicados como SendGrid, AWS SES, o Mailgun
- El límite de 500 emails/día es más que suficiente para la mayoría de aplicaciones universitarias

**Si superas los límites:**
- Google te bloqueará temporalmente el envío de emails
- Recibirás un error `550 5.4.5 Daily sending quota exceeded`
- El bloqueo se levanta automáticamente después de 24 horas

---

## Endpoints Principales

### Autenticación
- `POST /api/v1/auth/register` - Registro de usuario (envía OTP por email)
- `POST /api/v1/auth/verify-email` - Verificar email con código OTP
- `POST /api/v1/auth/login` - Login de usuario

### Perfil
- `GET /api/v1/users/{id}` - Obtener perfil de usuario
- `PUT /api/v1/users/{id}` - Actualizar perfil
- `PUT /api/v1/users/{id}/photo` - Actualizar foto de perfil

### Clases
- `GET /api/v1/schedule/weekly` - Obtener horario semanal
- `GET /api/v1/schedule/{classId}` - Detalle de una clase

### Reservas
- `POST /api/v1/booking` - Crear reserva
- `GET /api/v1/booking/my-bookings` - Mis reservas
- `DELETE /api/v1/booking/{bookingId}` - Cancelar reserva

### Historial
- `GET /api/v1/history/me?from=YYYY-MM-DD&to=YYYY-MM-DD` - Historial de asistencias
- `GET /api/v1/history/{attendanceId}` - Detalle de asistencia

---

## Troubleshooting

### Error: "Authentication failed" o "Username and Password not accepted"
**Solución:**
- Verifica que estés usando una **contraseña de aplicación** (NO tu contraseña de Gmail)
- Asegúrate de haber activado la verificación en 2 pasos en tu cuenta de Google
- Genera una nueva contraseña de aplicación desde https://myaccount.google.com/apppasswords
- Verifica que no haya espacios en `GMAIL_APP_PASSWORD` (debe ser una cadena continua de 16 caracteres)

### Error: "Connection timed out" o "Could not connect to SMTP host"
**Solución:**
- Verifica tu conexión a internet
- Asegúrate de que el puerto 587 no esté bloqueado por tu firewall
- Si estás detrás de un proxy corporativo, puede que necesites configuración adicional

### Error: "Daily sending quota exceeded"
**Solución:**
- Has superado el límite de 500 emails/día de Gmail
- Espera 24 horas para que se restablezca tu cuota
- Considera usar un servicio de email dedicado si necesitas enviar más emails

### Los emails no llegan
**Solución:**
1. Verifica que el email se haya enviado correctamente revisando los logs (debe aparecer `✅ Email enviado exitosamente`)
2. Revisa la carpeta de SPAM del destinatario
3. Verifica que `GMAIL_USERNAME` esté correctamente configurado
4. Si el destinatario usa Gmail, puede tardar unos segundos en llegar

### Error: "Failed messages: javax.mail.AuthenticationFailedException"
**Solución:**
- Tu cuenta de Gmail puede estar bloqueada temporalmente por intentos fallidos
- Ve a https://accounts.google.com/DisplayUnlockCaptcha y desbloquea tu cuenta
- Genera una nueva contraseña de aplicación

---

## Seguridad

- ✅ Todas las credenciales están en variables de entorno
- ✅ El archivo `.env` está en `.gitignore`
- ✅ Las contraseñas se hashean con BCrypt
- ✅ Autenticación JWT stateless
- ⚠️ **NUNCA** subas el archivo `.env` a Git
- ⚠️ **NUNCA** compartas tus API Keys públicamente

---

## Contribuidores

Grupo 10 - Desarrollo de Aplicaciones I - UADE

---

## Licencia

Este proyecto es parte del Trabajo Práctico Obligatorio de la materia Desarrollo de Aplicaciones I.
