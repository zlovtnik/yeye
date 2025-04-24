# Build stage for frontend
FROM eclipse-temurin:17-jdk-jammy AS frontend-build

# Install sbt only
RUN apt-get update && \
    apt-get install -y wget unzip && \
    wget -O sbt.zip https://github.com/sbt/sbt/releases/download/v1.10.11/sbt-1.10.11.zip && \
    unzip sbt.zip && \
    rm sbt.zip && \
    mv sbt /usr/local/ && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy project files necessary for frontend build
COPY build.sbt .
COPY project ./project
COPY frontend ./frontend
COPY shared ./shared

# Create empty CSS file directly
WORKDIR /app/frontend
RUN mkdir -p /app/dist/css
RUN echo '/* Tailwind CSS placeholder */' > /app/dist/css/tailwind.css

# Build the Scala.js frontend
WORKDIR /app
RUN /usr/local/sbt/bin/sbt "frontend/fastLinkJS"

# --- Final Nginx stage ---
FROM nginx:alpine

# Copy nginx configuration
COPY docker/nginx/nginx.conf /etc/nginx/conf.d/default.conf

# Create necessary directories in the final image
# Nginx root is /usr/share/nginx/html
RUN mkdir -p /usr/share/nginx/html/target/scala-3.3.1/yeye-frontend-fastopt
RUN mkdir -p /usr/share/nginx/html/css

# Copy built artifacts from the frontend-build stage
COPY --from=frontend-build /app/frontend/target/scala-3.3.1/yeye-frontend-fastopt /usr/share/nginx/html/target/scala-3.3.1/yeye-frontend-fastopt/
COPY --from=frontend-build /app/dist/css/tailwind.css /usr/share/nginx/html/css/
COPY --from=frontend-build /app/frontend/src/main/resources/index.html /usr/share/nginx/html/

# Debug: List files in the final stage to verify copies
RUN ls -laR /usr/share/nginx/html/

# Expose port 80
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"] 