# Build stage for frontend
FROM eclipse-temurin:17-jdk-jammy AS frontend-build

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
    cp frontend/dist/main.js /usr/share/nginx/html/ && \
    cp frontend/dist/main.js.map /usr/share/nginx/html/ && \
    cp frontend/src/main/resources/index.html /usr/share/nginx/html/

# Debug: List files in the build stage
RUN ls -la /usr/share/nginx/html/

# Final nginx stage
FROM nginx:alpine

# Create necessary directories
RUN mkdir -p /usr/share/nginx/html

# Copy the built files from the frontend build stage
COPY --from=frontend-build /usr/share/nginx/html/ /usr/share/nginx/html/

# Debug: List files in the final stage
RUN ls -la /usr/share/nginx/html/

# Copy Nginx configuration
COPY docker/nginx/nginx.conf /etc/nginx/conf.d/default.conf

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"] 