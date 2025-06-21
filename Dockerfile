
# Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración
COPY pom.xml .
COPY src ./src

# Build de producción y mostrar qué JARs se generaron
RUN mvn clean package -Pproduction -DskipTests && \
    echo "=== JARs generados ===" && \
    ls -la target/*.jar

# Runtime stage
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiar el JAR (usar comodín para cualquier nombre)
COPY --from=build /app/target/*.jar app.jar

# Verificar que el JAR se copió correctamente
RUN ls -la app.jar

# Puerto
EXPOSE 8080

# Comando de inicio con logging
CMD echo "Iniciando aplicación..." && \
    java -Dserver.port=${PORT:-8080} \
         -Dspring.profiles.active=production \
         -Dvaadin.productionMode=true \
         -jar app.jar