# RitmoFit API Backend - Grupo 10

API Rest para la aplicación de gestión de gimnasios RitmoFit. Este backend maneja la lógica de negocio, la persistencia de datos y la comunicación con servicios externos para la aplicación móvil del TPO de la materia Desarrollo de Aplicaciones I.

## Tecnologías Utilizadas
- Java 17
- Spring Boot 3
- Apache Maven 3.8
- MongoDB


## Configuración del Entorno
El proyecto requiere la configuración de variables de entorno para manejar credenciales de servicios externos de forma segura. Sin estas variables, la aplicación no podrá iniciarse correctamente.

### Variables de Entorno Requeridas
Debes configurar las siguientes dos variables en tu sistema o en la configuración de tu IDE:

- MONGO_PASSWORD: La contraseña para la conexión a la instancia de MongoDB Atlas.
- SMTP_PASSWORD: La contraseña de usuario para el servidor SMTP.

Puedes configurar estas variables de la siguiente manera:

#### Opción 1: En el IDE (IntelliJ IDEA)
Ve a la configuración de ejecución (Run/Debug Configurations).

1. Busca la sección Environment variables.
2. Añade las dos variables con sus respectivos valores:
    - MONGO_PASSWORD=tu_contraseña_de_mongo
    - SMTP_PASSWORD=tu_contraseña_de_smtp

#### Opción 2: En la terminal (Linux/macOS)

Ejecutar los siguientes comandos con Bash:
```
export MONGO_PASSWORD="tu_contraseña_de_mongo"
export SMTP_PASSWORD="tu_contraseña_de_smtp"
```


#### Opción 3: En la terminal (Windows)
Ejecutar los siguientes comandos con Powershell:
```
$env:MONGO_PASSWORD="tu_contraseña_de_mongo"
$env:SMTP_PASSWORD="tu_contraseña_de_smtp"
```


### Cómo Levantar el Proyecto
Una vez que las variables de entorno estén configurados, puedes levantar el proyecto utilizando el Maven Wrapper incluido en el repositorio.
```
./mvnw spring-boot:run
```

Si todo ha salido bien, verás en la consola los logs de Spring Boot indicando que la aplicación se ha iniciado. Por defecto, el servidor se ejecutará en http://localhost:8080.
