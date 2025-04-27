package com.pischule.memestv.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.HandlePhotos
import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memestv.bot.BotProps
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
class PhotoHandlerService(private val botProps: BotProps) {

    fun create(): HandlePhotos = {
        if (shouldForwardMessage(this)) {
            forwardPhotoMessage(this)
        }

        reactToMessage(this, "👀")
    }

    private fun shouldForwardMessage(env: MediaHandlerEnvironment<List<PhotoSize>>): Boolean {
        return env.message.chat.id != botProps.destinationChatId
    }

    private suspend fun forwardPhotoMessage(env: MediaHandlerEnvironment<List<PhotoSize>>) {
        env.bot
            .forwardMessage(
                chatId = ChatId.fromId(botProps.destinationChatId),
                fromChatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
            )
            .fold(
                { log.info { "Forwarded picture message: $it" } },
                { log.error { "Failed to forward message: $it" } },
            )
    }

    private suspend fun reactToMessage(
        env: MediaHandlerEnvironment<List<PhotoSize>>,
        emoji: String,
    ) {
        env.bot
            .setMessageReaction(
                chatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> log.atWarn { "Failed to react to message: $error" } }
    }
}
