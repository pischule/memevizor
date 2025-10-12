FROM docker.io/eclipse-temurin:21

RUN apt-get update \
    && apt-get install -y ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd --system app \
    && useradd --no-log-init --system --shell /sbin/nologin --gid app app

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --chown=app:app ${JAR_FILE} app.jar
COPY --chown=app:app build/resources/main/static BOOT-INF/classes/static

USER app:app

ENTRYPOINT ["java","-jar","app.jar"]

