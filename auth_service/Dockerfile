# Usar una imagen de OpenJDK con JARs optimizados para Spring Boot
FROM eclipse-temurin:21-jdk-alpine

# Configurar el directorio de trabajo
WORKDIR /app

# Copiar el JAR generado al contenedor
COPY target/usuarios-service-0.0.1-SNAPSHOT.jar app.jar

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
