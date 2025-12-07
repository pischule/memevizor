package com.pischule.memevizor.upload

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.oshai.kotlinlogging.withLoggingContext
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@ConditionalOnBean(S3Config::class)
@Service
class FileUploaderService(private val s3Client: S3Client, private val s3Props: S3Props) {
    fun uploadFile(fileBytes: ByteArray, filename: String, contentType: String) {
        withLoggingContext("filename" to filename, "bucket" to s3Props.bucket) {
            val request = PutObjectRequest {
                bucket = s3Props.bucket
                key = filename
                body = ByteStream.fromBytes(fileBytes)
                this.contentType = contentType
            }

            logger.info { "Started uploading a file" }
            runBlocking { s3Client.putObject(request) }
            logger.info { "Uploaded a file to S3" }
        }
    }
}
