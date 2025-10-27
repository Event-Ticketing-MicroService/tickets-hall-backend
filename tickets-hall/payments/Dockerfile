# Multi-stage build
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom.xml
COPY pom.xml ./pom.xml

# Copy payments module pom.xml
COPY payments/pom.xml ./payments/pom.xml

# Copy source code
COPY payments/src ./payments/src

# Build the application
RUN cd payments && mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/payments/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]