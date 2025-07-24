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

# Build the application
RUN mvn package -DskipTests

# Step 2: Use a JRE image to run the application and install PostgreSQL client tools
FROM 10.174.18.249:5000/system/debian:12.5

# Установка OpenJDK, wget и dpkg
RUN apt update && apt install -y openjdk-17-jre

## (installation from the Internet) Загрузка и установка MegaCLI
#RUN apt update && apt install -y libncurses5 wget  && \
#    mkdir -p /binary && cd /binary && \
#    wget -O /binary/megacli.deb "http://hwraid.le-vert.net/debian/pool-stretch/megacli/megacli_8.07.14-2%2BDebian.stretch.9.9_amd64.deb" && \
#    dpkg -i ./megacli.deb && \
#    apt-get install -f -y && \
#    apt-get autoremove wget -y && \
#    (dpkg -L megacli || (echo "Package megacli not found" && exit 1)) && megacli -v &&\
#    rm -rf /binary

# (installation from local files) Copy the MegaCLI binary
COPY MegaCLI_binary/megacli.deb /binary/megacli.deb

# (installation from local files) Установка MegaCLI из загруженного пакета (also u can see it MegaCLI_binary)
RUN apt update && apt install -y libncurses5 wget  && \
    cd /binary && \
    dpkg -i megacli.deb && \
    apt-get install -f -y && \
    apt-get autoremove wget -y && \
    (dpkg -L megacli || (echo "Package megacli not found" && exit 1)) && megacli -v &&\
    rm -rf /binary

# Garbage collecting stage
RUN rm -rf /var/cache/apt/archives /var/lib/apt/lists/* &&\
    apt-get clean

# Create data directory
RUN mkdir -p /app/data

# Set the working directory inside the container
WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/docker-raid-metrics-prometheus-exporter-*-SNAPSHOT-jar-with-dependencies.jar /app/app.jar

# Copy the docker-entrypoint
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

# TODO: CRON_EXPRESSION_STRING interprets with bash in docker-entrypoint.sh
## Set the environment variables
#ENV CRON_EXPRESSION_STRING="*/5 * * * * ?"
#ENV PROMETHEUS_ENDPOINT_URL="http://0.0.0.0:8080/metrics"
#ENV MEGACLI_CONTENT_FILE_PATH="data/megacli.log"
#ENV MEGACLI_LOCK_FILE_PATH="data/megacli.lock"
#
## Run the application
#ENTRYPOINT ["sh", "/app/docker-entrypoint.sh", "-DcronExpressionString=${CRON_EXPRESSION_STRING}", "-DprometheusEndpointURL=${PROMETHEUS_ENDPOINT_URL}", "-DmegacliContentFilePath=${MEGACLI_CONTENT_FILE_PATH}", "-DmegacliLockFilePath=${MEGACLI_LOCK_FILE_PATH}"]

ENTRYPOINT ["sh", "/app/docker-entrypoint.sh"]
