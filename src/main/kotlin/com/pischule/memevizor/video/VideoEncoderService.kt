package com.pischule.memevizor.video

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class VideoEncoderService {

    fun encodeToWebm(inputVideo: ByteArray): ByteArray {
        logger.info { "Started encoding a file" }

        val inputFile = createTempFile()
        val outputFile = createTempFile()
        val command: List<String> =
            listOf(
                "ffmpeg",
                "-i",
                "$inputFile",
                // video
                "-map",
                "0:v",
                "-c:v",
                "libvpx-vp9",
                "-cpu-used",
                "6",
                "-crf",
                "40",
                "-b:v",
                "0k",
                // file
                "-f",
                "webm",
                "-y",
                "$outputFile",
            )

        try {
            inputFile.writeBytes(inputVideo)

            val process = ProcessBuilder(command).redirectErrorStream(true).start()

            val output = process.inputStream.bufferedReader().use { it.readText() }
            val exitCode = process.waitFor()

            if (exitCode != 0) {
                error("Video conversion failed: exit code $exitCode, $output")
            }

            logger.info { "Converted" }

            return outputFile.readBytes()
        } finally {
            inputFile.deleteIfExists()
            outputFile.deleteIfExists()
        }
    }
}
