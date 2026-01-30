# Use a base image with Java 23
FROM eclipse-temurin:23-jdk-alpine

# Add metadata
LABEL authors="HIDAYA"
LABEL description="Reserve Fund Service for LidCoin Project"

# Create a volume for temporary files
VOLUME /tmp

# Set the working directory
WORKDIR /app

# Copy the application JAR file
COPY target/reserveFound_service-0.0.1-SNAPSHOT.jar app.jar

# Expose the port the application runs on
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]