FROM maven:3.9.9-eclipse-temurin-24-alpine AS build

WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# Segunda etapa: imagen más ligera para ejecutar
FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

# Crear usuario no-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Crear directorio para vouchers (si tu app lo necesita)
RUN mkdir -p /app/uploads/vouchers && chown -R appuser:appgroup /app

# Cambiar a usuario no-root
USER appuser

EXPOSE 8090

# Configurar variables de entorno para PostgreSQL
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://chatbot-db:5432/chatbot_colegio
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=admin
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]