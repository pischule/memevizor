package com.pischule.memevizor.util

import com.github.kotlintelegrambot.entities.Message

fun Message.getMedia(): MessageMedia? {
    photo?.lastOrNull()?.fileId?.let { fileId ->
        return MessageMedia(fileId, MessageMedia.Type.PHOTO)
    }

    (video?.fileId ?: videoNote?.fileId)?.let { fileId ->
        return MessageMedia(fileId, MessageMedia.Type.VIDEO)
    }

    return null
}

data class MessageMedia(val fileId: String, val type: Type) {
    enum class Type {
        PHOTO,
        VIDEO,
    }
}
