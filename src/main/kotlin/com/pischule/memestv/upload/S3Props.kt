package com.pischule.memestv.upload

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("s3")
data class S3Props(val accessKeyId: String, val secretAccessKey: String, val bucket: String)
