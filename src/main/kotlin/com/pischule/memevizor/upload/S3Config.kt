package com.pischule.memevizor.upload

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.sdk.kotlin.services.s3.S3Client
import aws.smithy.kotlin.runtime.net.url.Url
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(S3Props::class)
@Configuration
class S3Config {

    @Bean
    fun s3Client(s3Props: S3Props) = S3Client {
        endpointUrl = Url.parse(s3Props.endpoint)
        region = s3Props.region
        credentialsProvider = StaticCredentialsProvider {
            accessKeyId = s3Props.accessKeyId
            secretAccessKey = s3Props.secretAccessKey
        }

        // not supported by Yandex Object Storage
        continueHeaderThresholdBytes = null
    }
}
