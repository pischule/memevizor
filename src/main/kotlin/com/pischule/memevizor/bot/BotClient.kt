package com.pischule.memevizor.bot

interface BotClient {
    fun downloadFileBytes(fileId: String): ByteArray?

    fun forwardMessage(chatId: Long, fromChatId: Long, messageId: Long)

    fun setMessageReaction(chatId: Long, messageId: Long, emoji: String)

    fun sendChatAction(chatId: Long)

    fun sendMessage(chatId: Long, text: String)
}
