# Start with a base image that has Java 17 installed
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy the project's POM file to download dependencies
COPY pom.xml .

# Download the dependencies
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application into a JAR file
RUN ./mvnw package -DskipTests

# Expose the port the application runs on (8081 as per application.properties)
EXPOSE 8081

# The command to run the application when the container starts
ENTRYPOINT ["java", \
            "-Dspring.datasource.url=${SPRING_DATASOURCE_URL}", \
            "-Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME}", \
            "-Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD}", \
            "-Dspring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS}", \
            "-jar", "target/price-streamer-0.0.1-SNAPSHOT.jar"]