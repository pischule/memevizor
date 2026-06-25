package com.pischule.memevizor.bot.handler

import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.pischule.memevizor.bot.BotClient
import com.pischule.memevizor.bot.BotProps
import org.springframework.stereotype.Service

@Service
class MediaHandlerService(private val botProps: BotProps, private val botClient: BotClient) {

    fun create(env: MediaHandlerEnvironment<*>) {
        if (shouldForwardMessage(env)) {
            botClient.forwardMessage(
                chatId = botProps.forwardChatId,
                fromChatId = env.message.chat.id,
                messageId = env.message.messageId,
            )
        }

        botClient.setMessageReaction(
            chatId = env.message.chat.id,
            messageId = env.message.messageId,
            emoji = "👀",
        )
    }

    private fun shouldForwardMessage(env: MediaHandlerEnvironment<*>): Boolean {
        return env.message.chat.id != botProps.forwardChatId
    }
}
