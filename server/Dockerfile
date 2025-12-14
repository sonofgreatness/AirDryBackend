# Use a lightweight Java 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory in the container
WORKDIR /app

# Copy your server JAR into the container
COPY build/libs/server-all.jar .

# Expose the port your server listens on
EXPOSE 9090

# Run the server
CMD ["java", "-jar", "server-all.jar"]
