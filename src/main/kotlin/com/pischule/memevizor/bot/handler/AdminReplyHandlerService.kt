package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.entities.MessageOrigin
import com.pischule.memevizor.bot.BotClient
import com.pischule.memevizor.bot.BotProps
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class AdminReplyHandlerService(
    private val botProps: BotProps,
    @Lazy private val botClient: BotClient,
) {
    private val confirmCommands = setOf("this", "true", "!soxok")

    fun handle(env: MessageHandlerEnvironment) {
        if (!shouldHandleMessage(env)) return

        val text = env.message.text ?: return
        val replyToMessage = env.message.replyToMessage ?: return

        val originalChatId = extractOriginalChatId(replyToMessage.forwardOrigin) ?: return

        botClient.sendMessage(
            chatId = originalChatId,
            text = text,
            parseMode = null,
        )

        logger.info { "Replied to user $originalChatId: $text" }
    }

    private fun shouldHandleMessage(env: MessageHandlerEnvironment): Boolean {
        val isApprover = env.message.from?.id?.let { botProps.approverUserIds.contains(it) } == true
        val isInForwardChat = env.message.chat.id == botProps.forwardChatId
        val isReply = env.message.replyToMessage != null
        val command = env.message.text?.lowercase()
        val isNotConfirmCommand = command !in confirmCommands
        return isApprover && isInForwardChat && isReply && isNotConfirmCommand
    }

    private fun extractOriginalChatId(forwardOrigin: MessageOrigin?): Long? {
        return when (forwardOrigin) {
            is MessageOrigin.User -> forwardOrigin.senderUser.id
            is MessageOrigin.HiddenUser -> {
                logger.warn { "Cannot reply to hidden user: ${forwardOrigin.senderUserName}" }
                null
            }
            is MessageOrigin.Chat -> forwardOrigin.senderChat.id
            is MessageOrigin.Channel -> {
                logger.warn { "Cannot reply to channel forward" }
                null
            }
            null -> {
                logger.warn { "Message is not a forward" }
                null
            }
        }
    }
}
