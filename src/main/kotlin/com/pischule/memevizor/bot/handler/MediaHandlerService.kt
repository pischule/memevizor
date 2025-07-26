package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memevizor.bot.BotProps
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class MediaHandlerService(private val botProps: BotProps) {

    fun create(env: MediaHandlerEnvironment<*>) {
        if (shouldForwardMessage(env)) {
            forwardMessage(env)
        }

        reactToMessage(env, "👀")
    }

    private fun shouldForwardMessage(env: MediaHandlerEnvironment<*>): Boolean {
        return env.message.chat.id != botProps.forwardChatId
    }

    private fun forwardMessage(env: MediaHandlerEnvironment<*>) {
        env.bot
            .forwardMessage(
                chatId = ChatId.fromId(botProps.forwardChatId),
                fromChatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
            )
            .fold(
                { logger.info { "Forwarded picture message" } },
                { logger.error { "Failed to forward message: $it" } },
            )
    }

    private fun reactToMessage(env: MediaHandlerEnvironment<*>, emoji: String) {
        env.bot
            .setMessageReaction(
                chatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> logger.atWarn { "Failed to react to message: $error" } }
    }
}
