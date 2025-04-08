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
        if (shouldForwardMessage()) {
            forwardPhotoMessage()
        }

        reactToMessage("👀")
    }

    private fun MediaHandlerEnvironment<List<PhotoSize>>.shouldForwardMessage(): Boolean {
        return message.chat.id != botProps.destinationChatId
    }

    private suspend fun MediaHandlerEnvironment<List<PhotoSize>>.forwardPhotoMessage() {
        bot.forwardMessage(
                chatId = ChatId.fromId(botProps.destinationChatId),
                fromChatId = ChatId.fromId(message.chat.id),
                messageId = message.messageId,
            )
            .fold(
                { log.info { "Forwarded picture message: $it" } },
                { log.error { "Failed to forward message: $it" } },
            )
    }

    private suspend fun MediaHandlerEnvironment<List<PhotoSize>>.reactToMessage(emoji: String) {
        bot.setMessageReaction(
                chatId = ChatId.fromId(message.chat.id),
                messageId = message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> log.warn { "Failed to react to message: $error" } }
    }
}
