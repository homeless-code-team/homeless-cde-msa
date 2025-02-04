# Step 1: Use OpenJDK as base image
FROM openjdk:17-jdk-slim

# Step 2: Set working directory
WORKDIR /app

# Step 3: Copy the JAR file into the container
COPY build/libs/*.jar /app.jar

# Step 4: Define the entry point
ENTRYPOINT ["java", "-jar", "/app.jar"]
