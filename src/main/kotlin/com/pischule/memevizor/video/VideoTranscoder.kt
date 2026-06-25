package com.pischule.memevizor.video

fun interface VideoTranscoder {
    fun transcode(inputVideo: ByteArray): ByteArray
}
