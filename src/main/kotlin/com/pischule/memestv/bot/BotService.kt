package com.pischule.memestv.bot

import com.github.kotlintelegrambot.Bot
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BotService(private val bot: Bot) {

    @PostConstruct
    fun start() {
        Thread { bot.startPolling() }.start()
        logger.info { "Initialized bot" }
    }

    @PreDestroy
    fun stop() {
        bot.stopPolling()
        logger.info { "Stopped bot" }
    }
}
