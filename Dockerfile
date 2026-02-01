FROM docker.io/eclipse-temurin:21

ARG USER_ID=10001
ARG GROUP_ID=10001

RUN apt-get update \
    && apt-get install -y ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

RUN groupadd -g ${GROUP_ID} app \
    && useradd --no-log-init -u ${USER_ID} -g app --shell /sbin/nologin app

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY --chown=app:app ${JAR_FILE} app.jar
COPY --chown=app:app build/resources/main/static BOOT-INF/classes/static

USER ${USER_ID}:${GROUP_ID}

ENTRYPOINT ["java","-jar","app.jar"]

