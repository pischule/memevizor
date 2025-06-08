package com.pischule.memevizor.upload

import org.springframework.stereotype.Service

@Service
interface FileUploaderService {
    suspend fun uploadFile(fileBytes: ByteArray, filename: String, contentType: String)
}
