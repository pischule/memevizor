package com.pischule.memevizor.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async

private val logger = KotlinLogging.logger {}

@Configuration
class IndexInitializer(val fileUploaderService: FileUploaderService) {

    @Async
    @EventListener
    fun applicationStartedHandler(event: ApplicationStartedEvent) {
        try {
            val fileBytes = readResourceAsByteArray("/static/index.html")
            fileUploaderService.uploadFile(fileBytes, "index.html", "text/html")
        } catch (e: Exception) {
            logger.warn(e) { "Failed to upload " }
        }
    }

    private fun readResourceAsByteArray(resourcePath: String): ByteArray {
        val inputStream =
            ClassLoader.getSystemResourceAsStream(resourcePath) ?: error("$resourcePath not found")
        return inputStream.use { it.readAllBytes() }
    }
}
