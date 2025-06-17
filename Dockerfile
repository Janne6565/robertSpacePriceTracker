# Use Amazon Corretto 21 JDK as base image
FROM amazoncorretto:21

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR into the container
COPY target/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]