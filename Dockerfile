# Multi-stage build para optimizar el tamaño de la imagen
FROM maven:3.9.4-openjdk-17-slim AS build

# Instalar Node.js para Vaadin frontend build
RUN apt-get update && apt-get install -y curl \
    && curl -fsSL https://deb.nodesource.com/setup_18.x | bash - \
    && apt-get install -y nodejs \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Build de producción con frontend optimizado
RUN mvn clean package -Pproduction -DskipTests

# Imagen de runtime más ligera
FROM openjdk:17-jre-slim

WORKDIR /app

# Instalar curl para health check y crear usuario
RUN apt-get update && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/* \
    && addgroup --system spring && adduser --system spring --ingroup spring

# Copiar el JAR construido ANTES de cambiar de usuario
COPY --from=build /app/target/rama-*.jar app.jar

# Cambiar a usuario no-root DESPUÉS de copiar archivos
USER spring:spring

# Configuración de memoria para containers
ENV JVM_OPTS="-Xmx512m -Xms256m"

# Puerto de la aplicación
EXPOSE 8080

# Health check mejorado
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando de inicio optimizado para Render
ENTRYPOINT ["sh", "-c", "java $JVM_OPTS -Dserver.port=${PORT:-8080} -jar app.jar"]
