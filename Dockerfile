# Step 1: Use Maven image to build the application
FROM maven:3.8.6-eclipse-temurin-17 AS build

# Set maven package proxy
ARG MAVEN_PACKAGE_PROXY=http://10.174.18.94:8081

# Set the working directory inside the container
WORKDIR /app

# Copy the pom.xml and download dependencies (cache this step if possible)
COPY pom.xml .

## Download project dependencies (this step will be cached until pom.xml changes)
#RUN mvn dependency:go-offline

# Copy the entire project source code
COPY src ./src

# Copy the entire project source code
COPY src ./src

# Build the application
RUN mvn package -DskipTests

# Step 2: Use a JRE image to run the application and install PostgreSQL client tools
FROM --platform=linux/amd64  debian:12.5

# Установка OpenJDK, smartmontools + Garbage collecting stage
RUN apt update && apt install -y openjdk-17-jre smartmontools && \
    rm -rf /var/cache/apt/archives /var/lib/apt/lists/* &&\
    apt-get clean

# Create data configuration and logs directories
RUN mkdir -p /app/data /app/configuration /app/logs

# Set the working directory inside the container
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/docker-raid-metrics-prometheus-exporter-*-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

# Copy the default configuration
COPY configuration/default.configuration.xml /app/configuration/configuration.xml

# Copy the docker-entrypoint
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

ENTRYPOINT ["sh", "/app/docker-entrypoint.sh"]
