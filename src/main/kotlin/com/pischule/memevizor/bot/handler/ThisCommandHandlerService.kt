package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import com.pischule.memevizor.bot.BotProps
import com.pischule.memevizor.upload.FileUploaderService
import com.pischule.memevizor.util.*
import com.pischule.memevizor.video.VideoTranscoderService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ThisCommandHandlerService(
    private val botProps: BotProps,
    private val fileUploaderService: FileUploaderService,
    private val videoTranscoderService: VideoTranscoderService,
) {
    private val confirmCommands = listOf("this", "!soxok")
    private val mediaFileName = "media"

    fun create(env: MessageHandlerEnvironment) {
        if (!shouldHandleMessage(env)) return

        val replyToMessage = env.message.replyToMessage ?: return
        val media = replyToMessage.getMedia() ?: return

        env.bot.sendChatAction(ChatId.fromId(env.message.chat.id), ChatAction.TYPING)
        withLoggingContext("file_id" to media.fileId) {
            val fileBytes = env.bot.downloadFileBytes(media.fileId) ?: return
            logger.info { "Downloaded a file from Telegram, size=${fileBytes.size}" }

            val (convertedBytes, contentType) =
                when (media.type) {
                    MessageMedia.Type.PHOTO -> Pair(fileBytes, "image/jpeg")
                    MessageMedia.Type.VIDEO ->
                        Pair(videoTranscoderService.transcode(fileBytes), "video/webm")
                }

            fileUploaderService.uploadFile(convertedBytes, mediaFileName, contentType)

            reactToMessage(env, "👍")
        }
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isApprover = env.message.from?.id?.let { botProps.approverUserIds.contains(it) } == true
        val command = env.message.text?.lowercase()
        val isConfirmCommand = command in confirmCommands
        return isApprover && isConfirmCommand
    }

    private fun reactToMessage(env: MessageHandlerEnvironment, emoji: String) {
        env.bot
            .setMessageReaction(
                chatId = ChatId.fromId(env.message.chat.id),
                messageId = env.message.messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> logger.warn { "Failed to react to message: $error" } }
    }
}
