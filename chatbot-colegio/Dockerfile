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

# Cambiar a usuario no-root
USER appuser

EXPOSE 8090

# Variables de entorno - LA API KEY SE PASA DESDE FUERA
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://chatbot-db:5432/chatbot_colegio
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=admin
ENV SPRING_PROFILES_ACTIVE=docker
# GROQ_API_KEY se pasa desde docker-compose.yml o variable de entorno

ENTRYPOINT ["java", "-jar", "app.jar"]