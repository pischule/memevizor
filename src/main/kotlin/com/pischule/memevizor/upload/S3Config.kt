package com.pischule.memevizor.upload

import io.minio.MinioClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("!local")
@EnableConfigurationProperties(S3Props::class)
@Configuration
class S3Config {
    @Bean
    fun s3Client(s3Props: S3Props): MinioClient =
        MinioClient.builder()
            .endpoint("https://storage.yandexcloud.net")
            .region("ru-central1")
            .credentials(s3Props.accessKeyId, s3Props.secretAccessKey)
            .build()
}
