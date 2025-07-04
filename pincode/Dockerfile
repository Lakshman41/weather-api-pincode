# --- Stage 1: The Build Environment ---
# We start with a base image that has both Java (JDK) and Maven installed.
# Using a specific, stable tag is best practice.
FROM eclipse-temurin:17-jdk-jammy as builder

# Install Maven inside this temporary build container.
RUN apt-get update && apt-get install -y maven

# Set a working directory inside the container.
WORKDIR /app

# Copy only the pom.xml file first. Docker's caching is smart: if this
# file doesn't change, it won't re-download all the dependencies, saving time.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Now copy the rest of your application's source code.
COPY src ./src

# Run the Maven package command to compile the code and build the JAR file.
# We skip the tests because they have already been run and passed.
RUN mvn clean package -DskipTests -B


# --- Stage 2: The Final, Lean Runtime Environment ---
# We start with a much smaller base image that only contains the Java Runtime (JRE),
# which is all that's needed to run the application.
FROM eclipse-temurin:17-jre-jammy

# Set the working directory.
WORKDIR /app

# Copy the final JAR file that was created in the 'builder' stage into this new image.
# We rename it to app.jar for a simple, consistent name.
COPY --from=builder /app/target/*.jar app.jar

# Tell Docker that the container will listen on port 8080 at runtime.
EXPOSE 8080

# This is the command that will be executed when the container starts.
# It simply runs our Spring Boot application.
ENTRYPOINT ["java", "-jar", "app.jar"]