# Start with a base image that has Java 17 installed
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven wrapper files
COPY mvnw .
COPY .mvn .mvn

# Copy the project's POM file to download dependencies
COPY pom.xml .

# Download the dependencies. This creates a separate layer that is cached
# and only re-downloaded if the pom.xml changes.
RUN ./mvnw dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Package the application into a JAR file
RUN ./mvnw package -DskipTests

# Expose the port the application runs on (default for Spring Boot is 8080)
EXPOSE 8080

# The command to run the application when the container starts
ENTRYPOINT ["java", \
            "-Dspring.kafka.bootstrap-servers=${SPRING_KAFKA_BOOTSTRAP_SERVERS}", \
            "-Dspring.kafka.properties.security.protocol=SASL_SSL", \
            "-Dspring.kafka.properties.sasl.mechanism=SCRAM-SHA-256", \
            "-Dspring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.scram.ScramLoginModule required username='${KAFKA_USERNAME}' password='${KAFKA_PASSWORD}';", \
            "-jar", "target/price-producer-0.0.1-SNAPSHOT.jar"]