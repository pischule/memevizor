package com.pischule.memestv.s3

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("s3")
data class S3Props(val accessKeyId: String, val secretAccessKey: String, val bucket: String)
