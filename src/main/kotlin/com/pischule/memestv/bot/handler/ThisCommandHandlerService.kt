package com.pischule.memestv.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.HandleMessage
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memestv.bot.BotProps
import com.pischule.memestv.s3.FileUploaderService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class ThisCommandHandlerService(
    private val botProps: BotProps,
    private val fileUploaderService: FileUploaderService,
) {
    fun create(): HandleMessage = HandleMessage@{
        if (!shouldHandleMessage(this)) return@HandleMessage

        val maxResPhotoId = message.replyToMessage!!.photo!!.last().fileId
        val fileBytes = bot.downloadFileBytes(maxResPhotoId) ?: return@HandleMessage

        log.info { "Downloaded a file $maxResPhotoId from Telegram" }

        fileUploaderService.uploadFile(fileBytes)
        log.info { "Uploaded a file $maxResPhotoId to S3" }

        reactToMessage(this, "👍")
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isFromTargetChat = env.message.chat.id == botProps.destinationChatId
        val isThisCommand = env.message.text?.lowercase() == "this"
        val hasPhotoReply = env.message.replyToMessage?.photo?.isNotEmpty() == true
        return isFromTargetChat && isThisCommand && hasPhotoReply
    }

    private suspend fun reactToMessage(env: MessageHandlerEnvironment, emoji: String) {
        env.bot
            .setMessageReaction(
                chatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> log.warn { "Failed to react to message: $error" } }
    }
}
