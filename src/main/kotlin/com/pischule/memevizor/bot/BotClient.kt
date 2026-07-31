package com.pischule.memevizor.bot

import com.github.kotlintelegrambot.entities.ParseMode

interface BotClient {
    fun downloadFileBytes(fileId: String): ByteArray?

    fun forwardMessage(chatId: Long, fromChatId: Long, messageId: Long)

    fun setMessageReaction(chatId: Long, messageId: Long, emoji: String)

    fun sendChatAction(chatId: Long)

    fun sendMessage(
        chatId: Long,
        text: String,
        parseMode: ParseMode? = ParseMode.MARKDOWN_V2,
    )
}
