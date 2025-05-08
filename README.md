# Microservices Architecture with JWT Authentication, MinIO, and PostgreSQL

Este proyecto implementa una arquitectura de microservicios utilizando Spring Boot, JWT para la autenticación, MinIO para almacenamiento de archivos y PostgreSQL como base de datos. El sistema está compuesto por cuatro servicios:

- `auth_service` - Servicio para la autenticación de usuarios con JWT.
- `file_service` - Servicio para gestionar archivos utilizando MinIO.
- `frontend_service` - Frontend del sistema (aplicación web).
- `gateway_service` - Servicio de Gateway que orquesta las peticiones a los demás microservicios.

## Arquitectura

El sistema está basado en una arquitectura de microservicios con los siguientes componentes:

1. **JWT Authentication**: El `auth_service` se encarga de la autenticación de usuarios mediante JSON Web Tokens (JWT).
2. **MinIO**: El `file_service` interactúa con MinIO para el almacenamiento de archivos.
3. **PostgreSQL**: Los servicios `auth_service` y `file_service` utilizan PostgreSQL como base de datos para almacenar los datos de usuarios y metadatos de los archivos.
4. **Gateway**: El `gateway_service` actúa como punto de entrada para las peticiones de los clientes, redirigiendo a los servicios correspondientes.

## Tecnologías

- **Spring Boot**: Framework para los microservicios.
- **JWT**: Autenticación segura basada en JSON Web Tokens.
- **MinIO**: Solución de almacenamiento de objetos compatible con S3.
- **PostgreSQL**: Base de datos relacional.
- **Docker**: Contenerización de servicios.
- **JUnit**: Framework para pruebas unitarias.
- **Docker Compose**: Orquestación de los servicios en contenedores Docker.

## Servicios

### 1. `auth_service`

- **Descripción**: Servicio de autenticación que permite registrar y autenticar usuarios mediante JWT.
- **Endpoints**:
  - `POST /register`: Registra un nuevo usuario.
  - `POST /login`: Inicia sesión y genera un JWT para el usuario autenticado.

### 2. `file_service`

- **Descripción**: Servicio que gestiona los archivos, utilizando MinIO para el almacenamiento.
- **Endpoints**:
  - `POST /files/upload`: Sube un archivo a MinIO.
  - `GET /files/{filename}`: Obtiene un archivo de MinIO.
  
### 3. `frontend_service`

- **Descripción**: Frontend de la aplicación que interactúa con los servicios de backend a través del API Gateway.

### 4. `gateway_service`

- **Descripción**: API Gateway que redirige las peticiones a los microservicios correspondientes.
  
## Configuración

### Docker Compose

El archivo `docker-compose.yml` contiene la configuración de los servicios, incluida la red de microservicios, MinIO y PostgreSQL. Aquí está la configuración básica:

```yaml
version: '3.8'

services:
  consul:
    image: hashicorp/consul:1.16.1
    container_name: consul
    ports:
      - "1049:8500"
      - "8600:8600/udp"
    command: "consul agent -server -bootstrap -ui -client=0.0.0.0 -data-dir=/consul/data"
    volumes:
      - consul_data:/consul/data
    networks:
      - microservices_network

  auth-service:
    build: ./auth_service
    container_name: auth-service
    ports:
      - 1051:8080
    environment:
      - SPRING_PROFILES_ACTIVE=docker  
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/antique_store
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin.47*
    depends_on:
      - consul
      - postgres
      - minio
    networks:
      - microservices_network

  file-service:
    build: ./file_service
    container_name: file-service
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - MINIO_URL=http://minio:9000
      - MINIO_ACCESS_KEY=UuQZD7ZHeARRARAJQyCX
      - MINIO_SECRET_KEY=YlKG5NsbpzPSBleFVkiXdRi6T11AiaI6c0MotTKg
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/antique_store
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin.47*
    depends_on:
      - consul
      - postgres
      - minio
    networks:
      - microservices_network

  gateway-service:
    build: ./gateway-service
    container_name: gateway-service
    ports:
      - "1050:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/antique_store
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin.47*
    depends_on:
      - consul
      - postgres
      - minio
    networks:
      - microservices_network
