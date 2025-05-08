# Usar una imagen de OpenJDK con JARs optimizados para Spring Boot
FROM eclipse-temurin:21-jdk-alpine

# Configurar el directorio de trabajo
WORKDIR /app

# Copiar el JAR generado al contenedor
COPY target/gateway-service-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto del servicio
EXPOSE 8080

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
