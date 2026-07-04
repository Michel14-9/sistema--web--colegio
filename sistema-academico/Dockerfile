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

# Cambiar a usuario no-root
USER appuser

EXPOSE 8080

# Variables de entorno (sin credenciales hardcodeadas)
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://academico-db:5432/sistema_academico
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=admin
ENV SPRING_PROFILES_ACTIVE=docker
# SPRING_MAIL_USERNAME y SPRING_MAIL_PASSWORD se pasan desde docker-compose.yml

ENTRYPOINT ["java", "-jar", "app.jar"]