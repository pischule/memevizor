package com.pischule.memestv.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Profile("local")
@Service
class DummyFileUploadService() : FileUploaderService {

    override suspend fun uploadFile(fileBytes: ByteArray, filename: String) {
        logger.info { "File $filename has been successfully uploaded to nowhere" }
    }
}
