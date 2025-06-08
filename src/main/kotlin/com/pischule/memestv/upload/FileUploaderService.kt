package com.pischule.memestv.upload

import org.springframework.stereotype.Service

@Service
interface FileUploaderService {
    suspend fun uploadFile(fileBytes: ByteArray, filename: String)
}
