package com.pischule.memevizor.util

import com.github.kotlintelegrambot.entities.Message

fun Message.getMaxResPhotoId(): String? = this.photo?.lastOrNull()?.fileId

fun Message.getVideoFileId(): String? = this.video?.fileId
