package com.pischule.memestv.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.runBlocking
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
            val fileBytes = readResourceAsByteArray("static/index.html")
            runBlocking { fileUploaderService.uploadFile(fileBytes, "index.html", "text/html") }
        } catch (e: Error) {
            logger.warn(e) { "Failed to upload " }
        }
    }

    private fun readResourceAsByteArray(resourcePath: String): ByteArray {
        val inputStream =
            ClassLoader.getSystemResourceAsStream(resourcePath) ?: error("$resourcePath not found")
        return inputStream.use { it.readAllBytes() }
    }
}
