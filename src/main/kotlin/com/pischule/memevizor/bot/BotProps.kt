package com.pischule.memevizor.bot

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("bot") data class BotProps(val token: String, val destinationChatId: Long)
