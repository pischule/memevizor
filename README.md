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
1. Create `.env` file with:
```properties
# Telegram Bot Configuration
BOT_TOKEN=your_bot_token
FORWARD_CHAT_ID=your_forward_chat_id
APPROVER_USER_IDS=id1,id2,id3

# S3 Configuration (for production)
S3_ACCESS_KEY_ID=your_key
S3_SECRET_ACCESS_KEY=your_secret
S3_BUCKET=your_bucket_name
```

2. For local development:
```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

## Usage
1. Send a meme (image/video) to the Telegram bot
2. Reply with `this` or `!soxok` to approve and upload
3. View the meme at `http://localhost:8080` (or your deployed URL)

## Deployment
### Docker
```bash
docker build -t memevizor .
docker run -d -p 8080:8080 memevizor
```

### Cloud (Heroku/AWS/etc)
1. Set environment variables
2. Deploy with your preferred provider

## Contributing
Contributions welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Submit a pull request

## License
GNU GPLv3
