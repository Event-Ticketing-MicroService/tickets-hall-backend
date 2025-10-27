# Multi-stage build
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom.xml
COPY pom.xml ./pom.xml

# Copy notification module pom.xml
COPY notification/pom.xml ./notification/pom.xml

# Copy source code
COPY notification/src ./notification/src

# Build the application
RUN cd notification && mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/notification/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]