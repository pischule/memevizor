package com.pischule.memevizor.upload

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("s3")
data class S3Props(
    val endpoint: String,
    val region: String,
    val accessKeyId: String,
    val secretAccessKey: String,
    val bucket: String,
)
