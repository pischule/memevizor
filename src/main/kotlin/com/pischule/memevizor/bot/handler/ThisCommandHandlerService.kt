package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memevizor.bot.BotProps
import com.pischule.memevizor.upload.FileUploaderService
import com.pischule.memevizor.util.getMaxResPhotoId
import com.pischule.memevizor.util.getVideoFileId
import com.pischule.memevizor.video.VideoEncoderService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ThisCommandHandlerService(
    private val botProps: BotProps,
    private val fileUploaderService: FileUploaderService,
    private val videoEncoderService: VideoEncoderService,
) {
    private val confirmCommands = listOf("this", "!soxok")

    suspend fun create(env: MessageHandlerEnvironment) {
        if (!shouldHandleMessage(env)) return

        val replyToMessage = env.message.replyToMessage ?: return

        val imageFileId = replyToMessage.getMaxResPhotoId()
        val videoFileId = replyToMessage.getVideoFileId()
        val fileId = imageFileId ?: videoFileId ?: return

        env.bot.sendChatAction(ChatId.fromId(env.message.chat.id), ChatAction.TYPING)
        withLoggingContext("file_id" to fileId) {
            val fileBytes = env.bot.downloadFileBytes(fileId) ?: return
            logger.info { "Downloaded a file from Telegram, size=${fileBytes.size}" }

            val convertedBytes =
                if (videoFileId != null) {
                    videoEncoderService.encodeToWebm(fileBytes)
                } else {
                    fileBytes
                }

            val contentType =
                if (videoFileId != null) {
                    "video/webm"
                } else {
                    "image/jpeg"
                }

            fileUploaderService.uploadFile(convertedBytes, "_", contentType)

            reactToMessage(env, "👍")
        }
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isApprover = env.message.from?.id?.let { botProps.approverUserIds.contains(it) } == true
        val command = env.message.text?.lowercase()
        val isConfirmCommand = command in confirmCommands
        val hasMediaReply =
            env.message.replyToMessage?.let {
                it.photo?.isNotEmpty() == true || it.video != null
            } == true
        return isApprover && isConfirmCommand && hasMediaReply
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
