package com.pischule.memevizor.video

import io.github.oshai.kotlinlogging.KotlinLogging
import java.nio.file.Path
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
        logger.info { "Started transcoding a video" }

        val inputFile = createTempFile()
        val outputFile = createTempFile()

        try {
            inputFile.writeBytes(inputVideo)

            val copyVideo = hasDesiredVideoEncoding(inputFile)
            val copyAudio = hasDesiredAudioEncoding(inputFile)

            val processOutput: String
            val timeTaken = measureTime {
                processOutput =
                    launchCommand(
                        transcodeCommand(
                            inputFile = inputFile,
                            outputFile = outputFile,
                            copyVideo = copyVideo,
                            copyAudio = copyAudio,
                        )
                    )
            }

            logger.atInfo {
                message = "Finished transcoding a video"
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

    private fun launchCommand(command: List<String>): String {
        val process = ProcessBuilder(command).redirectErrorStream(true).start()
        val exitCode = process.waitFor()
        val processOutput = process.inputStream.bufferedReader().use { it.readText() }
        if (exitCode != 0) {
            throw VideoTranscodingException(exitCode, processOutput)
        }
        return processOutput.trim()
    }

    private fun hasDesiredVideoEncoding(inputFile: Path): Boolean {
        val videoCodec = launchCommand(getVideoCodecCommand(inputFile))
        return videoCodec == "av1"
    }

    private fun hasDesiredAudioEncoding(inputFile: Path): Boolean {
        val audioCodec = launchCommand(getAudioCodecCommand(inputFile))
        return audioCodec == "opus" || audioCodec == ""
    }

    private fun getVideoCodecCommand(inputFile: Path) =
        listOf(
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "v:0",
            "-show_entries",
            "stream=codec_name",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            "$inputFile",
        )

    private fun getAudioCodecCommand(inputFile: Path) =
        listOf(
            "ffprobe",
            "-v",
            "error",
            "-select_streams",
            "a:0",
            "-show_entries",
            "stream=codec_name",
            "-of",
            "default=noprint_wrappers=1:nokey=1",
            "$inputFile",
        )

    private fun transcodeCommand(
        inputFile: Path,
        outputFile: Path,
        copyVideo: Boolean,
        copyAudio: Boolean,
    ): List<String> = buildList {
        add("ffmpeg")
        add("-nostdin")
        add("-nostats")
        add("-hide_banner")

        // input
        add("-i")
        add("$inputFile")

        // video
        add("-map")
        add("0:v")
        add("-c:v")
        if (copyVideo) {
            add("copy")
        } else {
            add("libsvtav1")
            add("-preset")
            add("6")
            add("-crf")
            add("35")
            add("-svtav1-params")
            add("film-grain=10")
        }

        // audio
        add("-map")
        add("0:a?")
        add("-c:a")
        if (copyAudio) {
            add("copy")
        } else {
            add("libopus")
            add("-b:a")
            add("96k")
            add("-vbr")
            add("on")
            add("-compression_level")
            add("10")
        }

        // output
        add("-f")
        add("webm")
        add("-y")
        add("$outputFile")
    }
}
