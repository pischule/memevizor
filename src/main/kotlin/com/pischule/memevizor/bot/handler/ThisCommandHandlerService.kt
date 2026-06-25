package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.pischule.memevizor.bot.BotClient
import com.pischule.memevizor.bot.BotProps
import com.pischule.memevizor.upload.FileStorage
import com.pischule.memevizor.util.*
import com.pischule.memevizor.video.VideoTranscoder
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class ThisCommandHandlerService(
    private val botProps: BotProps,
    private val fileStorage: FileStorage,
    private val videoTranscoder: VideoTranscoder,
    private val botClient: BotClient,
) {
    private val confirmCommands = listOf("this", "true", "!soxok")
    private val mediaFileName = "media"

    fun create(env: MessageHandlerEnvironment) {
        if (!shouldHandleMessage(env)) return

        val replyToMessage = env.message.replyToMessage ?: return
        val media = replyToMessage.getMedia() ?: return

        botClient.sendChatAction(env.message.chat.id)
        withLoggingContext("file_id" to media.fileId) {
            val fileBytes = botClient.downloadFileBytes(media.fileId) ?: return
            logger.info { "Downloaded a file from Telegram, size=${fileBytes.size}" }

            val (convertedBytes, contentType) =
                when (media.type) {
                    MessageMedia.Type.PHOTO -> Pair(fileBytes, "image/jpeg")
                    MessageMedia.Type.VIDEO ->
                        Pair(videoTranscoder.transcode(fileBytes), "video/webm")
                }

            fileStorage.upload(convertedBytes, mediaFileName, contentType)

            botClient.setMessageReaction(
                chatId = env.message.chat.id,
                messageId = env.message.messageId,
                emoji = "👍",
            )
        }
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isApprover = env.message.from?.id?.let { botProps.approverUserIds.contains(it) } == true
        val command = env.message.text?.lowercase()
        val isConfirmCommand = command in confirmCommands
        return isApprover && isConfirmCommand
    }
}
