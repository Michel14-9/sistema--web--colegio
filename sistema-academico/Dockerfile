FROM maven:3.9.9-amazoncorretto-24 AS build

WORKDIR /app

# Configurar encoding
ENV MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Compilar
RUN mvn clean package -DskipTests

# Segunda etapa
FROM amazoncorretto:24-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el JAR
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para uploads
RUN mkdir -p /app/uploads/vouchers && chown -R appuser:appgroup /app/uploads

# ===== PERMITIR PUERTO 80 =====
USER root
RUN chown -R appuser:appgroup /app
RUN setcap 'cap_net_bind_service=+ep' /opt/java/openjdk/bin/java || true
USER appuser

# EXPOSE 80 (cambia 8080 por 80)
EXPOSE 8080

# Todas las variables de entorno se pasan desde docker-compose.yml o .env
# NO hardcodear credenciales aquí

ENTRYPOINT ["java", "-jar", "app.jar"]