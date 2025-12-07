package com.pischule.memevizor.util

import com.github.kotlintelegrambot.entities.Message

fun Message.getMedia(): MessageMedia? {
    photo?.lastOrNull()?.fileId?.let { fileId ->
        return MessageMedia(fileId, MessageMedia.Type.PHOTO)
    }

    video?.let {
        return MessageMedia(it.fileId, MessageMedia.Type.VIDEO)
    }

    videoNote?.let {
        return MessageMedia(it.fileId, MessageMedia.Type.VIDEO)
    }

    document
        ?.takeIf { it.mimeType?.startsWith("video/") == true }
        ?.let {
            return MessageMedia(it.fileId, MessageMedia.Type.VIDEO)
        }

    return null
}

data class MessageMedia(val fileId: String, val type: Type) {
    enum class Type {
        PHOTO,
        VIDEO,
    }
}
