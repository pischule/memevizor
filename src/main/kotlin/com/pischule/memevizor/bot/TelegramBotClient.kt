package com.pischule.memevizor.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatAction
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.reaction.ReactionType
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}

@Component
class TelegramBotClient(private val bot: Bot) : BotClient {

    override fun downloadFileBytes(fileId: String): ByteArray? = bot.downloadFileBytes(fileId)

    override fun forwardMessage(chatId: Long, fromChatId: Long, messageId: Long) {
        bot.forwardMessage(ChatId.fromId(chatId), ChatId.fromId(fromChatId), messageId)
            .fold(
                { logger.info { "Forwarded message" } },
                { logger.error { "Failed to forward message: $it" } },
            )
    }

    override fun setMessageReaction(chatId: Long, messageId: Long, emoji: String) {
        bot.setMessageReaction(
                chatId = ChatId.fromId(chatId),
                messageId = messageId,
                reaction = listOf(ReactionType.Emoji(emoji)),
            )
            .onError { error -> logger.warn { "Failed to react to message: $error" } }
    }

    override fun sendChatAction(chatId: Long) {
        bot.sendChatAction(ChatId.fromId(chatId), ChatAction.TYPING)
    }

    override fun sendMessage(chatId: Long, text: String) {
        bot.sendMessage(
                chatId = ChatId.fromId(chatId),
                text = text,
                parseMode = ParseMode.MARKDOWN_V2,
            )
            .onError { logger.warn { "Failed to send message: $it" } }
    }
}
