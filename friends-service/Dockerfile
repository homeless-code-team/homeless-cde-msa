# Step 1: Use the base image for OpenJDK
FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y curl

# Step 2: Set the working directory in the container
WORKDIR /app

# Step 3: Copy the JAR file into the container
COPY build/libs/*.jar /app.jar

# Step 4: Set the default command to run the application
ENTRYPOINT ["java", "-jar", "/app.jar"]
