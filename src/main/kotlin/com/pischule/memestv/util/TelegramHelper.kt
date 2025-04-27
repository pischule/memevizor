package com.pischule.memestv.util

import com.github.kotlintelegrambot.entities.Message

fun Message.getMaxResPhotoId(): String? = this.replyToMessage?.photo?.lastOrNull()?.fileId
