package com.pischule.memestv

import aws.sdk.kotlin.services.s3.S3Client
import aws.sdk.kotlin.services.s3.model.PutObjectRequest
import aws.smithy.kotlin.runtime.content.ByteStream
import org.springframework.stereotype.Service

@Service
class FileUploaderService(private val s3Client: S3Client, private val s3Props: S3Props) {

    suspend fun uploadFile(fileBytes: ByteArray) {
        s3Client.putObject(
            PutObjectRequest {
                body = ByteStream.fromBytes(fileBytes)
                bucket = s3Props.bucket
                key = "_.jpeg"
            }
        )
    }
}
