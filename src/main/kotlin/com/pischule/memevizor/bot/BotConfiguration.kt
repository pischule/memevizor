package com.pischule.memevizor.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.photos
import com.github.kotlintelegrambot.entities.Message
import com.pischule.memevizor.bot.handler.PhotoHandlerService
import com.pischule.memevizor.bot.handler.ThisCommandHandlerService
import com.pischule.memevizor.util.getMaxResPhotoId
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
    private val photoHandlerService: PhotoHandlerService,
) {

    @Bean
    fun telegramBot(): Bot {
        return bot {
            token = botProps.token
            setupDispatchers()
        }
    }

    private fun Bot.Builder.setupDispatchers() = dispatch {
        message {
            withLoggingContext(messageContext(message)) {
                try {
                    thisCommandHandlerService.create(this)
                } catch (e: Error) {
                    logger.error(e) { "Error while handling message" }
                }
            }
        }
        photos {
            withLoggingContext(messageContext(message)) {
                try {
                    photoHandlerService.create(this)
                } catch (e: Error) {
                    logger.error(e) { "Error while handling photo" }
                }
            }
        }
    }

    private fun messageContext(message: Message): Map<String, String?> =
        mapOf(
            "message_id" to message.messageId.toString(),
            "chat_id" to message.chat.id.toString(),
            "from_user_id" to message.from?.id.toString(),
            "from_user_username" to message.from?.username.toString(),
            "file_id" to message.getMaxResPhotoId(),
        )
}
