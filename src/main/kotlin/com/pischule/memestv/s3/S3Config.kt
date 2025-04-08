package com.pischule.memestv.s3

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
    fun s3Client(s3Props: S3Props): S3Client {
        return S3Client {
            endpointUrl = Url.parse("https://storage.yandexcloud.net")
            region = "ru-central1"
            credentialsProvider = StaticCredentialsProvider {
                accessKeyId = s3Props.accessKeyId
                secretAccessKey = s3Props.secretAccessKey
            }
        }
    }
}
