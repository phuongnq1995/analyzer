# Use the Eclipse Temurin JDK 21 image as the base
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy your application's JAR file into the container
COPY target/analyzer-0.0.1-SNAPSHOT.jar app.jar

# Command to run the application when the container starts
CMD ["java", "-jar", "app.jar"]
