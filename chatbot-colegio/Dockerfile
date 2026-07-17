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

# Todas las variables de entorno se pasan desde docker-compose.yml o .env
# GROQ_API_KEY se pasa desde docker-compose.yml o variable de entorno
# NO hardcodear credenciales aquí

ENTRYPOINT ["java", "-jar", "app.jar"]