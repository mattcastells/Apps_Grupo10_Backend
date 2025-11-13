# RitmoFit API Backend - Grupo 10

API Rest para la aplicación de gestión de gimnasios RitmoFit. Este backend maneja la lógica de negocio, la persistencia de datos y la comunicación con servicios externos para la aplicación móvil del TPO de la materia Desarrollo de Aplicaciones I.

## Tecnologías Utilizadas
- Java 17
- Spring Boot 3.5.5
- Apache Maven 3.8+
- MongoDB Atlas
- Elastic Email API (envío de emails)
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

#### **ELASTIC_EMAIL_API_KEY**
API Key de Elastic Email para envío de correos electrónicos.

**Cómo obtenerla:**
1. Inicia sesión en https://elasticemail.com
2. Ve a **"Settings"** (Configuración) en el menú lateral
3. Haz clic en **"Create Additional Apikey"** (Crear clave API adicional)
4. Dale un nombre descriptivo (ej: "RitmoFit Backend API")
5. Selecciona permisos: marca **"Send Emails"** (Enviar emails)
6. Copia la API Key generada (formato: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)

**IMPORTANTE:** Guarda esta clave de forma segura, no podrás verla nuevamente.

```env
ELASTIC_EMAIL_API_KEY=xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
```

---

#### **ELASTIC_EMAIL_FROM**
Email del remitente (debe estar verificado en tu cuenta de Elastic Email).

**Cómo verificarlo:**
1. En Elastic Email, ve a **"Settings"** > **"Sending Domains"** o **"Sender & SMTP"**
2. **Opción 1 - Verificar dominio completo (recomendado para producción):**
   - Si tienes un dominio propio (ej: `ritmofit.com`), agrégalo y verifica con registros DNS
   - Luego puedes usar cualquier email de ese dominio: `no-reply@ritmofit.com`
3. **Opción 2 - Verificar email personal (para desarrollo):**
   - Agrega tu email personal (ej: `tu_email@gmail.com`)
   - Verifica haciendo clic en el enlace que te envían

```env
ELASTIC_EMAIL_FROM=no-reply@ritmofit.com.ar
# O si usas email personal para testing:
# ELASTIC_EMAIL_FROM=tu_email@gmail.com
```

---

#### **ELASTIC_EMAIL_FROM_NAME**
Nombre del remitente (nombre visible en el email).

Este es el nombre que aparecerá en la bandeja de entrada del destinatario.

```env
ELASTIC_EMAIL_FROM_NAME=RitmoFit
```

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
export ELASTIC_EMAIL_API_KEY="tu_api_key"
export ELASTIC_EMAIL_FROM="no-reply@ritmofit.com.ar"
export ELASTIC_EMAIL_FROM_NAME="RitmoFit"
export JWT_SECRET_KEY="tu_clave_secreta_jwt"
export JWT_EXPIRATION_MS="1000000000"
```

#### Opción 3: En la terminal (Windows PowerShell)
```powershell
$env:MONGO_PASSWORD="tu_contraseña"
$env:ELASTIC_EMAIL_API_KEY="tu_api_key"
$env:ELASTIC_EMAIL_FROM="no-reply@ritmofit.com.ar"
$env:ELASTIC_EMAIL_FROM_NAME="RitmoFit"
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

Para verificar que el servicio de Elastic Email está configurado correctamente:

1. **Verifica las variables de entorno:**
   - Asegúrate de que `ELASTIC_EMAIL_API_KEY` esté configurada
   - Verifica que `ELASTIC_EMAIL_FROM` esté verificado en tu cuenta de Elastic Email

2. **Prueba el registro de usuario:**
   - Haz una petición POST a `/api/v1/auth/register`
   - Deberías recibir un email con el código OTP

3. **Revisa los logs:**
   - Si hay algún error, aparecerá en la consola
   - Los logs indicarán si el email se envió correctamente

---

## Limitaciones de Elastic Email (Plan Free)

**IMPORTANTE:** El plan gratuito de Elastic Email tiene las siguientes limitaciones:

- ❌ **Solo puedes enviar emails a tu propia dirección registrada** (la cuenta con la que te registraste en Elastic Email)
- ⚠️ **No puedes enviar a otros usuarios** sin comprar un plan pago
- ✅ **Sirve para testing y desarrollo** enviando emails a ti mismo

**Para producción:**
- Necesitarás un plan pago de Elastic Email (desde $0.10 por cada 1000 emails)
- Debes verificar tu dominio (ej: `ritmofit.com.ar`)
- Después de verificar, podrás enviar a cualquier destinatario

**Alternativa para testing con múltiples usuarios:**
- Considera usar **SendGrid** (100 emails/día gratis a cualquier destinatario)
- O mantén **Mailtrap** para desarrollo y usa Elastic Email solo en producción

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

### Error: "ELASTIC_EMAIL_API_KEY no está configurada correctamente"
**Solución:** Verifica que hayas configurado la variable de entorno `ELASTIC_EMAIL_API_KEY` correctamente en tu archivo `.env` o en las variables de sistema.

### Error al enviar emails: "Error 401 Unauthorized"
**Solución:** Tu API Key de Elastic Email es inválida o ha expirado. Genera una nueva desde el panel de Elastic Email.

### Error: "Email address not verified"
**Solución:** Debes verificar el email del remitente (`ELASTIC_EMAIL_FROM`) en tu cuenta de Elastic Email antes de poder enviar correos.

### Los emails no llegan
**Solución:**
1. Verifica que el destinatario sea tu email registrado en Elastic Email (limitación del plan free)
2. Revisa la carpeta de SPAM
3. Revisa los logs de la aplicación para ver si hay errores

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
