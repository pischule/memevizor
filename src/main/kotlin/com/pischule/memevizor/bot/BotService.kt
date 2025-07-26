package com.pischule.memevizor.bot

import com.github.kotlintelegrambot.Bot
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import java.util.concurrent.ExecutorService
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class BotService(private val bot: Bot, private val botPollingExecutor: ExecutorService) {

    @PostConstruct
    fun start() {
        botPollingExecutor.submit(bot::startPolling)
        logger.info { "Bot service started" }
    }

    @PreDestroy
    fun stop() {
        bot.stopPolling()
        logger.info { "Bot service stopped" }
    }
}
