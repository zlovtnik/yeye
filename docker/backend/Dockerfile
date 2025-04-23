# Use Eclipse Temurin JDK 17 as the base image (ARM64 compatible)
FROM eclipse-temurin:17-jdk AS build

# Install sbt
RUN apt-get update && \
    apt-get install -y curl && \
    curl -L -o sbt-1.9.8.deb https://repo.scala-sbt.org/scalasbt/debian/sbt-1.9.8.deb && \
    dpkg -i sbt-1.9.8.deb && \
    apt-get update && \
    apt-get install -y sbt && \
    rm sbt-1.9.8.deb && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Set working directory
WORKDIR /app

# Copy build files
COPY build.sbt ./
COPY project/ ./project/
COPY backend/ ./backend/
COPY shared/ ./shared/

# Build the application
RUN sbt backend/assembly

# Use Eclipse Temurin JRE 17 for the final stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/backend/target/scala-3.3.1/backend.jar ./app.jar

# Expose port 8080
EXPOSE 8080

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 