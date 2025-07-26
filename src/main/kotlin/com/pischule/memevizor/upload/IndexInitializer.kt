package com.pischule.memevizor.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

private val logger = KotlinLogging.logger {}

@Configuration
class IndexInitializer(val fileUploaderService: FileUploaderService, val ctx: ApplicationContext) {

    @Async
    @EventListener(ApplicationStartedEvent::class)
    fun applicationStartedHandler() {
        try {
            val fileBytes = getIndexHtmlBytes()
            fileUploaderService.uploadFile(fileBytes, "index.html", "text/html")
        } catch (e: Exception) {
            logger.warn(e) { "Failed to upload " }
        }
    }

    private fun getIndexHtmlBytes(): ByteArray =
        ctx.getResource("classpath:static/index.html").inputStream.use { it.readAllBytes() }
}
