FROM openjdk:23-jdk-slim

# Set the working directory
WORKDIR /app
ENV SPRING_PROFILES_ACTIVE=docker

# Copy the jar file into the container
COPY target/pfm-dashboard-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080
EXPOSE 8080

# Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
