package com.pischule.memevizor.util

import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.files.Document
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.files.Video
import com.github.kotlintelegrambot.entities.files.VideoNote
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TelegramHelperTest {
    private val mockChat = Chat(id = 1L, type = "private")

    @Test
    fun `getMedia should return photo from PhotoSize`() {
        val message =
            Message(
                messageId = 1L,
                chat = mockChat,
                date = 123,
                photo = listOf(PhotoSize("p1", "p1u", 100, 100), PhotoSize("p2", "p2u", 200, 200)),
            )

        val result = message.getMedia()

        result shouldBe MessageMedia(fileId = "p2", type = MessageMedia.Type.PHOTO)
    }

    @Test
    fun `getMedia should return video from Video`() {
        val message =
            Message(
                messageId = 1L,
                chat = mockChat,
                date = 123,
                video = Video("v1", "v1u", 100, 100, 10),
            )

        val result = message.getMedia()

        result shouldBe MessageMedia(fileId = "v1", type = MessageMedia.Type.VIDEO)
    }

    @Test
    fun `getMedia should return video from VideoNote`() {
        val message =
            Message(
                messageId = 1L,
                chat = mockChat,
                date = 123,
                videoNote = VideoNote("vn1", "vn1u", 100, 10),
            )

        val result = message.getMedia()

        result shouldBe MessageMedia(fileId = "vn1", type = MessageMedia.Type.VIDEO)
    }

    @Test
    fun `getMedia should return video from Document with video mimeType`() {
        val message =
            Message(
                messageId = 1L,
                chat = mockChat,
                date = 123,
                document = Document("d1", "d1u", mimeType = "video/mp4"),
            )

        val result = message.getMedia()

        result shouldBe MessageMedia(fileId = "d1", type = MessageMedia.Type.VIDEO)
    }

    @Test
    fun `getMedia should return null for Document with non-video mimeType`() {
        val message =
            Message(
                messageId = 1L,
                chat = mockChat,
                date = 123,
                document = Document("d1", "d1u", mimeType = "image/jpeg"),
            )

        val result = message.getMedia()

        result.shouldBeNull()
    }

    @Test
    fun `getMedia should return null for message with no media`() {
        val message = Message(messageId = 1L, chat = mockChat, date = 123, text = "hello")

        val result = message.getMedia()

        result.shouldBeNull()
    }
}
