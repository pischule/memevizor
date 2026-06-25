package com.pischule.memevizor.upload

interface FileStorage {
    fun upload(fileBytes: ByteArray, filename: String, contentType: String)
}
