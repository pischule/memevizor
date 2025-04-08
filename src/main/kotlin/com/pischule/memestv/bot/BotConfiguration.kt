package com.pischule.memestv.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.dispatcher.photos
import com.pischule.memestv.bot.handler.PhotoHandlerService
import com.pischule.memestv.bot.handler.ThisCommandHandlerService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

private val log = KotlinLogging.logger {}

@EnableConfigurationProperties(BotProps::class)
@Configuration
class BotConfiguration(
    private val botProps: BotProps,
    private val thisCommandHandlerService: ThisCommandHandlerService,
    private val photoHandlerService: PhotoHandlerService,
) {

    @Bean
    fun telegramBot(): Bot {
        return bot {
            token = botProps.token
            dispatch {
                message {
                    try {
                        thisCommandHandlerService.create().invoke(this)
                    } catch (e: Error) {
                        log.error(e) { "Error while handling message" }
                    }
                }
                photos {
                    try {
                        photoHandlerService.create().invoke(this)
                    } catch (e: Error) {
                        log.error(e) { "Error while handling photo" }
                    }
                }
            }
        }
    }
}
