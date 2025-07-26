package com.pischule.memevizor.video

data class VideoTranscodingException(val ffmpegExitCode: Int, val ffmpegOutput: String) :
    RuntimeException("Video transcoding failed. Code=$ffmpegOutput. Output=$ffmpegOutput")
