package com.pischule.memevizor

import com.pischule.memevizor.bot.BotClient
import com.pischule.memevizor.upload.FileStorage
import com.pischule.memevizor.video.VideoTranscoder
import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestConfig {

    @Primary @Bean fun botClient(): BotClient = mock()

    @Primary @Bean fun fileStorage(): FileStorage = mock()

    @Primary @Bean fun videoTranscoder(): VideoTranscoder = mock()
}
