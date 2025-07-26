package com.pischule.memevizor.video

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes
import kotlin.time.measureTime
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class VideoTranscoderService {

    fun transcode(inputVideo: ByteArray): ByteArray {
        logger.info { "Started transcoding a file" }

        val inputFile = createTempFile()
        val outputFile = createTempFile()

        val command: List<String> =
            listOf(
                "ffmpeg",
                "-nostdin",
                "-nostats",
                "-hide_banner",
                // input
                "-i",
                "$inputFile",
                // video
                "-map",
                "0:v",
                "-c:v",
                "libsvtav1",
                "-preset",
                "6",
                "-crf",
                "35",
                "-svtav1-params",
                "film-grain=10",
                // audio
                "-map",
                "0:a?",
                "-c:a",
                "libopus",
                "-b:a",
                "96k",
                "-vbr",
                "on",
                "-compression_level",
                "10",
                // output
                "-f",
                "webm",
                "-y",
                "$outputFile",
            )

        try {
            inputFile.writeBytes(inputVideo)

            val process = ProcessBuilder(command).redirectErrorStream(true).start()
            val exitCode: Int
            val timeTaken = measureTime { exitCode = process.waitFor() }
            val processOutput = process.inputStream.bufferedReader().use { it.readText() }
            if (exitCode != 0) {
                throw VideoTranscodingException(exitCode, processOutput)
            }

            logger.atInfo {
                message = "Finished transcoding a file"
                payload =
                    mapOf(
                        "processOutput" to processOutput,
                        "timeTakenMs" to timeTaken.inWholeMilliseconds,
                    )
            }
            return outputFile.readBytes()
        } finally {
            inputFile.deleteIfExists()
            outputFile.deleteIfExists()
        }
    }
}
