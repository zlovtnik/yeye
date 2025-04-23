# Use Eclipse Temurin 17 JDK for Scala.js build
FROM eclipse-temurin:17-jdk-jammy AS build

# Install sbt
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget -O sbt.zip https://github.com/sbt/sbt/releases/download/v1.10.11/sbt-1.10.11.zip && \
    unzip sbt.zip && \
    rm sbt.zip && \
    mv sbt /usr/local/ && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project files
COPY build.sbt .
COPY project ./project
COPY frontend ./frontend
COPY shared ./shared

# Build the frontend
RUN /usr/local/sbt/bin/sbt "frontend/fullLinkJS"

# Create the directory structure for nginx
RUN mkdir -p /usr/share/nginx/html && \
    cp frontend/target/scala-3.3.1/frontend-opt/main.js /usr/share/nginx/html/ && \
    cp frontend/target/scala-3.3.1/frontend-opt/main.js.map /usr/share/nginx/html/ && \
    cp frontend/src/main/resources/index.html /usr/share/nginx/html/

# Use Nginx to serve the built files
FROM nginx:alpine

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"] 