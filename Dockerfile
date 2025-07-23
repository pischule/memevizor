FROM docker.io/eclipse-temurin:17
RUN apt-get update \
    && apt-get install -y ffmpeg \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*
RUN addgroup -S spring && adduser -S spring -G spring