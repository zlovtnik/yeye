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

# Explicitly create directories
RUN mkdir -p /app/project /app/src/main /app/src/test /app/shared/src /tmp/main_src /tmp/test_src /tmp/shared_src

# --- Diagnostic: Verify directory creation ---
RUN ls -la /app/src

# Copy build files (paths relative to context 'yeye/')
COPY backend/build.sbt /app/
COPY backend/project/ /app/project/

# --- Workaround: Copy to temp dir first ---
COPY backend/src/main /tmp/main_src/
COPY backend/src/test /tmp/test_src/
COPY shared/src /tmp/shared_src/

# --- Workaround: Move from temp dir to final destination ---
RUN mv /tmp/main_src/* /app/src/main/ && rm -rf /tmp/main_src
RUN mv /tmp/test_src/* /app/src/test/ && rm -rf /tmp/test_src
RUN mv /tmp/shared_src/* /app/shared/src/ && rm -rf /tmp/shared_src

# Build the application
# Run sbt from the root of the backend project setup in /app
RUN sbt assembly

# Use Eclipse Temurin JRE 17 for the final stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/scala-3.3.1/backend.jar ./app.jar

# Expose port 8080
EXPOSE 8080

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"] 