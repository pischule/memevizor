package com.pischule.memevizor.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.ParseMode
import com.pischule.memevizor.bot.handler.MediaHandlerService
import com.pischule.memevizor.bot.handler.ThisCommandHandlerService
import com.pischule.memevizor.util.getMedia
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val logger = KotlinLogging.logger {}

@EnableConfigurationProperties(BotProps::class)
@Configuration
class BotConfiguration(
    private val botProps: BotProps,
    private val thisCommandHandlerService: ThisCommandHandlerService,
    private val mediaHandlerService: MediaHandlerService,
) {

    @Bean
    fun telegramBot(): Bot {
        return bot {
            token = botProps.token
            setupDispatchers()
        }
    }

    private fun Bot.Builder.setupDispatchers() = dispatch {
        message { withLogAndErrorHandling(message) { thisCommandHandlerService.create(this) } }
        photos { handleMedia() }
        video { handleMedia() }
        videoNote { handleMedia() }
        command("whoami") {
            withLogAndErrorHandling(message) {
                bot.sendMessage(
                        chatId = ChatId.fromId(message.chat.id),
                        text = "chatId: `${message.chat.id}`\nuserId: `${message.from?.id}`",
                        parseMode = ParseMode.MARKDOWN_V2,
                    )
                    .onError { logger.warn { "Failed to reply to whoami command: $it" } }
            }
        }
    }

    private suspend fun MediaHandlerEnvironment<*>.handleMedia() {
        withLogAndErrorHandling(message) { mediaHandlerService.create(this) }
    }

    private suspend fun withLogAndErrorHandling(message: Message, block: suspend () -> Unit) {
        withLoggingContext(
            "message_id" to message.messageId.toString(),
            "chat_id" to message.chat.id.toString(),
            "from_user_id" to message.from?.id.toString(),
            "from_user_username" to message.from?.username.toString(),
            "file_id" to (message.getMedia()?.fileId),
        ) {
            try {
                block.invoke()
            } catch (e: Exception) {
                logger.error(e) { "Error while handling message" }
            }
        }
    }
}
