package com.pischule.memestv

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.photos
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

val log = KotlinLogging.logger {}

@Profile("!test")
@EnableConfigurationProperties(BotProps::class)
@Service
class BotService(
    private val botProps: BotProps,
    private val fileUploaderService: FileUploaderService,
) {
    private lateinit var bot: Bot

    @PostConstruct
    fun start() {
        bot = bot {
            token = botProps.token
            dispatch {
                message {
                    try {
                        val chatId = message.chat.id
                        val replyToPhotos = message.replyToMessage
                            ?.photo
                            ?.takeIf { it.isNotEmpty() }
                        if (chatId == botProps.destinationChatId
                            && message.text?.lowercase() == "this"
                            && replyToPhotos != null
                        ) {
                            val maxResPhoto = replyToPhotos.last().fileId
                            val fileBytes = bot.downloadFileBytes(maxResPhoto)
                            fileBytes?.let {
                                log.info { "Downloaded a file $maxResPhoto from telegram" }
                                fileUploaderService.uploadFile(it)
                                log.info { "Uploaded a file $maxResPhoto to s3" }
                                bot.setMessageReaction(
                                    chatId = ChatId.fromId(message.chat.id),
                                    messageId = message.messageId,
                                    reaction = listOf(ReactionType.Emoji("👍"))
                                ).onError { error ->
                                    log.warn { "Failed to react to message: $error" }
                                }
                            }
                        }
                    } catch (e: Error) {
                        log.error(e) { "Error while handling message" }
                    }
                }
                photos {
                    val message = this.message

                    if (message.chat.id != botProps.destinationChatId) {
                        bot.forwardMessage(
                            chatId = ChatId.fromId(botProps.destinationChatId),
                            fromChatId = ChatId.fromId(message.chat.id),
                            messageId = message.messageId,
                        ).fold(
                            {
                                log.info { "Forwarded pictures message: $it" }
                            },
                            {
                                log.error { "Failed to forward message: $it" }
                            }
                        )
                    }

                    bot.setMessageReaction(
                        chatId = ChatId.fromId(message.chat.id),
                        messageId = message.messageId,
                        reaction = listOf(ReactionType.Emoji("👀"))
                    ).onError { error ->
                        log.warn { "Failed to react to message: $error" }
                    }

                }
            }
        }

        Thread {
            bot.startPolling()
        }.start()

        log.info { "Initialized bot" }
    }

    @PreDestroy
    fun stop() {
        bot.stopPolling()
        log.info { "Stopped bot" }
    }
}