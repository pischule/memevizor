package com.pischule.memevizor.upload

import io.github.oshai.kotlinlogging.KotlinLogging
import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@ConditionalOnBean(S3Config::class)
@Service
class S3FileUploaderService(private val s3Client: MinioClient, private val s3Props: S3Props) :
    FileUploaderService {
    override fun uploadFile(fileBytes: ByteArray, filename: String, contentType: String) {
        logger.info { "Before upload" }
        s3Client.putObject(
            PutObjectArgs.builder()
                .bucket(s3Props.bucket)
                .`object`(filename)
                .stream(fileBytes.inputStream(), fileBytes.size.toLong(), -1)
                .contentType(contentType)
                .build()
        )
        logger.info { "File $filename has been uploaded to S3" }
    }
}
