# Multi-stage build
# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy parent pom.xml
COPY pom.xml ./pom.xml

# Copy tickets module pom.xml
COPY tickets/pom.xml ./tickets/pom.xml

# Copy source code
COPY tickets/src ./tickets/src

# Build the application
RUN cd tickets && mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/tickets/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]