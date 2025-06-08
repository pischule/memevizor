package com.pischule.memestv.upload

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@ConditionalOnBean(S3Config::class)
@Service
class S3FileUploaderService(private val s3Client: S3Client, private val s3Props: S3Props) :
    FileUploaderService {
    override suspend fun uploadFile(fileBytes: ByteArray, filename: String, contentType: String) {
        s3Client.putObject(
            PutObjectRequest {
                body = ByteStream.fromBytes(fileBytes)
                bucket = s3Props.bucket
                key = filename
                this.contentType = contentType
            }
        )
        logger.info { "File $filename has been uploaded to S3" }
    }
}
