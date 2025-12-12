# Stage 1: Build the application with Maven and Temurin JDK 21
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY src /app/src
COPY pom.xml /app
RUN mvn clean install -U

# Stage 2: Create the final, lightweight runtime image with Temurin JRE 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copy the packaged JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
