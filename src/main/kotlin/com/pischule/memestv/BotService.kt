package com.pischule.memestv

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
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
class BotService(val botProps: BotProps) {
    private lateinit var bot: Bot

    @PostConstruct
    fun start() {
        bot = bot {
            token = botProps.token
            dispatch {
                photos {
                    val message = this.message

                    bot.setMessageReaction(
                        chatId = ChatId.fromId(message.chat.id),
                        messageId = message.messageId,
                        reaction = listOf(ReactionType.Emoji("👀"))
                    )

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