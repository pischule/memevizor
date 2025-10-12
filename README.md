# Memevizor 📺

A Telegram bot for meme management with cloud storage integration and a web interface for display.

## Features
- 🤖 Telegram bot that handles image and video memes
- ☁️ Cloud storage integration (AWS S3/Yandex Cloud compatible)
- 🖼️ Web interface with auto-refreshing media display
- 🔐 Approver user system for content moderation
- 📁 Local development mode with dummy storage

## Architecture
The system consists of:
- **Telegram Bot**: Processes commands and media
- **Storage Layer**: S3-compatible storage for memes
- **Web Interface**: Simple HTML page for viewing memes
- **Spring Boot**: Kotlin-based backend with dependency injection

## Setup

The application is configured via Spring Boot properties.

### Local Development

1.  Copy the sample local configuration file:
    ```bash
    cp src/main/resources/application-local.properties.dist src/main/resources/application-local.properties
    ```

2.  Edit `src/main/resources/application-local.properties` and fill in your bot details:
    ```properties
    # Telegram Bot Configuration
    bot.token=your_bot_token
    bot.forward-chat-id=your_forward_chat-id
    bot.approver-user-ids=id1,id2,id3
    ```

3.  Run the application with the `local` profile active:
    ```bash
    ./gradlew bootRun --args='--spring.profiles.active=local'
    ```
    In this mode, memes are stored in memory and are not persisted.

### Production

For a production deployment (e.g., in Docker), you can provide configuration via environment variables or an `application.properties` file.

**Example using environment variables:**

Spring Boot automatically maps environment variables (e.g., `BOT_TOKEN`) to application properties (e.g., `bot.token`).

```bash
export BOT_TOKEN="your_bot_token"
export BOT_FORWARD_CHAT_ID="your_forward_chat_id"
export BOT_APPROVER_USER_IDS="id1,id2,id3"

export S3_ENDPOINT="https://s3.example.com"
export S3_REGION="us-east-1"
export S3_BUCKET="your_bucket_name"
export S3_ACCESS_KEY_ID="your_key"
export S3_SECRET_ACCESS_KEY="your_secret"

java -jar build/libs/memevizor-*.jar
```

**Example `application.properties` file:**

```properties
# Telegram Bot Configuration
bot.token=your_bot_token
bot.forward-chat-id=your_forward_chat_id
bot.approver-user-ids=id1,id2,id3

# S3 Configuration
s3.endpoint=https://s3.example.com
s3.region=us-east-1
s3.bucket=your_bucket_name
s3.access-key-id=your_key
s3.secret-access-key=your_secret
```

## Usage

1.  Send a meme (image or video) to the Telegram bot.
2.  To approve and upload the meme, reply to the message with `this` or `!soxok`.
3.  View the latest meme at `http://localhost:8080` (or your deployed URL).

## Deployment

### Docker

1.  Run the container with environment variables:
    ```bash
    docker run -d \
      -e BOT_TOKEN='your_bot_token' \
      -e BOT_FORWARD_CHAT_ID='your_forward_chat_id' \
      -e BOT_APPROVER_USER_IDS='id1,id2,id3' \
      -e S3_ENDPOINT='https.s3.example.com' \
      -e S3_REGION='us-east-1' \
      -e S3_BUCKET='your_bucket_name' \
      -e S3_ACCESS_KEY_ID='your_key' \
      -e S3_SECRET_ACCESS_KEY='your_secret' \
      ghcr.io/pischule/memevizor:latest
    ```

## License
GNU GPLv3
