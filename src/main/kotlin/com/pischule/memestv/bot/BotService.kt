package com.pischule.memestv.bot

import com.github.kotlintelegrambot.Bot
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BotService(private val bot: Bot) {
    private var pollingThread: Thread? = null

    @PostConstruct
    fun start() {
        pollingThread =
            Thread { bot.startPolling() }
                .apply {
                    name = "telegram-bot-polling"
                    start()
                }
        logger.info { "Bot service started" }
    }

    @PreDestroy
    fun stop() {
        bot.stopPolling()
        pollingThread?.join(1000)
        logger.info { "Bot service stopped" }
    }
}
