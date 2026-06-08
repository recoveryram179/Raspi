# Use the Gradle image for the build stage
FROM gradle:8.7-jdk17-alpine AS build

# Set the working directory
WORKDIR /app

# Copy all files to the working directory
COPY . .

# Ensure the gradlew script is executable
RUN chmod +x ./gradlew

# Build the project, skipping tests
RUN ./gradlew clean build -x test

# Use the OpenJDK image for the final stage
FROM openjdk:17.0.1-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the build stage
COPY --from=build /app/build/libs/Raspi-0.0.1-SNAPSHOT.jar Raspi.jar

# Expose the application port
EXPOSE 8090

# Define the command to run the application
ENTRYPOINT ["java", "-jar", "Raspi.jar"]
