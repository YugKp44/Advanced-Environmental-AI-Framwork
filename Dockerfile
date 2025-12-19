# ================================
# EcoAI Backend Dockerfile
# ================================
FROM eclipse-temurin:17-jdk-alpine as builder

WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src

# Install Maven
RUN apk add --no-cache maven

# Build the application
RUN mvn clean package -DskipTests

# ================================
# Production Stage
# ================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar
COPY --from=builder /app/target/*.jar app.jar

# Create data directory for H2 file database
RUN mkdir -p /app/data

# Expose port
EXPOSE 8080

# Run with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
