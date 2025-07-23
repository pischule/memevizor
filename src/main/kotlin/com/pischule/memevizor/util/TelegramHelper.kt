package com.pischule.memevizor.util

import com.github.kotlintelegrambot.entities.Message

fun Message.getMaxResPhotoId(): String? = photo?.lastOrNull()?.fileId

fun Message.getVideoFileId(): String? = video?.fileId ?: videoNote?.fileId
