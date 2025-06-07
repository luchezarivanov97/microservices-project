# Use Java 21 slim image
FROM openjdk:21-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your app listens on (make sure this matches your app's config)
EXPOSE 8080

# Start the application
ENTRYPOINT ["java", "-jar", "app.jar"]