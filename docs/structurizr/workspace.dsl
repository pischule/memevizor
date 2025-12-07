workspace "memevizor" "A meme sharing service" {
    model {
        // Actors
        u = person "User" "Meme submitter."
        a = person "Approver" "Moderates/Approves memes."
        tv = person "Viewer (Any Web Browser)" "Opens the S3-hosted UI on a browser (e.g., on a TV screen) to view the current approved meme."

        // External Systems
        tg = softwareSystem "Telegram" "Messaging platform used for content submission and moderation."

        // The Software System under development
        ss = softwareSystem "Memevizor" "A Telegram-based service for submitting, approving, and displaying memes." {

            // Core Application Container (Renamed and specified technology)
            bot = container "Memevizor Bot Backend" "Kotlin/Spring Application." "Processes Telegram updates, handles media conversion, and manages S3 content."

            // Storage Container (Clarified role)
            s3 = container "Content S3 Bucket" "AWS S3." "Stores static UI assets (HTML/CSS/JS) and approved media content."
        }

        // Actor Relationships
        u -> tg "Sends memes and content to"
        a -> tg "Moderates/Approves content by replying to messages in"

        // Backend Interactions
        // 1. Core Telegram Communication (Polling for messages and sending replies)
        bot -> tg "Polls for updates and sends reactions/replies/Downloads media" "HTTPS/Telegram Bot API"

        // 3. S3 Interactions
        bot -> s3 "Uploads UI assets/media" "HTTPS"

        // Viewer/Client Interactions
        tv -> s3 "Fetches UI and polls for media files" "HTTPS"
    }

    views {
        systemContext ss "SystemContext" {
            // Focus on the Memevizor system and its neighbors
            include *
            autolayout lr
            //
        }

        container ss "ContainerDiagram" {
            // Focus on the containers within Memevizor and their external dependencies
            include *
            autolayout tb
        }
    }
}
