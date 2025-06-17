package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memevizor.bot.BotProps
import com.pischule.memevizor.upload.FileUploaderService
import com.pischule.memevizor.util.getMaxResPhotoId
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ThisCommandHandlerService(
    private val botProps: BotProps,
    private val fileUploaderService: FileUploaderService,
) {
    private val confirmCommands = listOf("this", "!soxok")

    suspend fun create(env: MessageHandlerEnvironment) {
        if (!shouldHandleMessage(env)) return

        val maxResPhotoId = env.message.replyToMessage?.getMaxResPhotoId() ?: return

        withLoggingContext("file_id" to maxResPhotoId) {
            val fileBytes = env.bot.downloadFileBytes(maxResPhotoId) ?: return
            logger.info { "Downloaded a file from Telegram" }

            fileUploaderService.uploadFile(fileBytes, "_.jpeg", "image/jpeg")

            reactToMessage(env, "👍")
        }
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isApprover = env.message.from?.id?.let { botProps.approverUserIds.contains(it) } == true
        val command = env.message.text?.lowercase()
        val isConfirmCommand = command in confirmCommands
        val hasPhotoReply = env.message.replyToMessage?.photo?.isNotEmpty() == true
        return isApprover && isConfirmCommand && hasPhotoReply
    }

    private suspend fun reactToMessage(env: MessageHandlerEnvironment, emoji: String) {
        env.bot
            .setMessageReaction(
                chatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> logger.warn { "Failed to react to message: $error" } }
    }
}
