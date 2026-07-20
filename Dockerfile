# Etapa 1: Compilación
FROM maven:3.8.8-eclipse-temurin-17 AS builder
WORKDIR /app

# Copiar el POM y descargar dependencias (aprovechando la caché de capas de Docker)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar el empaquetado JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Imagen de ejecución
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el JAR generado en la etapa anterior
COPY --from=builder /app/target/mediturno-0.0.1-SNAPSHOT.jar app.jar

# Exponer el puerto por defecto de Spring Boot
EXPOSE 8080

# Comando de ejecución
ENTRYPOINT ["java", "-jar", "app.jar"]
