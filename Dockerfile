FROM docker.io/eclipse-temurin:21
RUN apt-get update \
    && apt-get install -y ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN groupadd -r spring && useradd --no-log-init -r -g spring spring
USER spring:spring
ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar
COPY build/resources/main/static BOOT-INF/classes/static
ENTRYPOINT ["java","-jar","/app.jar"]