FROM openjdk:21-jdk-slim

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Crear directorio de trabajo
WORKDIR /app

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente
COPY src ./src

# Construir la aplicación
RUN mvn clean package -DskipTests

# Puerto que expone la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación con perfil de producción
CMD ["java", "-jar", "-Dspring.profiles.active=production", "-Dserver.port=${PORT:-8080}", "target/api-gateway-0.0.1-SNAPSHOT.jar"]
