# Usar imagen estable y confiable
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copiar archivos de configuración
COPY pom.xml .
COPY src ./src

# Build de producción
RUN mvn clean package -Pproduction -DskipTests

# Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiar JAR
COPY --from=build /app/target/rama-*.jar app.jar

# Puerto
EXPOSE 8080

# Comando de inicio
CMD ["java", "-Dserver.port=${PORT:-8080}", "-jar", "app.jar"]
