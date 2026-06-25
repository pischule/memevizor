package com.pischule.memevizor

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.handlers.MessageHandlerEnvironment
import com.github.kotlintelegrambot.dispatcher.handlers.media.MediaHandlerEnvironment
import com.github.kotlintelegrambot.entities.Chat
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.files.PhotoSize
import com.github.kotlintelegrambot.entities.files.Video
import com.pischule.memevizor.bot.BotClient
import com.pischule.memevizor.bot.handler.MediaHandlerService
import com.pischule.memevizor.bot.handler.ThisCommandHandlerService
import com.pischule.memevizor.upload.FileStorage
import com.pischule.memevizor.video.VideoTranscoder
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

@ActiveProfiles("test")
@SpringBootTest
class HappyPathIntegrationTest {

    @Autowired private lateinit var thisCommandHandler: ThisCommandHandlerService

    @Autowired private lateinit var mediaHandler: MediaHandlerService

    @MockitoBean private lateinit var botClient: BotClient

    @MockitoBean private lateinit var fileStorage: FileStorage

    @MockitoBean private lateinit var videoTranscoder: VideoTranscoder

    private val approverChatId = 2000000000L
    private val approverUserId = 1000000000L
    private val userChatId = 777L

    private val approverChat = Chat(id = approverChatId, type = "private")
    private val userChat = Chat(id = userChatId, type = "private")
    private val approverUser = User(id = approverUserId, isBot = false, firstName = "Approver")
    private val bot = mock<Bot>()
    private val testImageBytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())

    // --------------- Happy paths ---------------

    @Test
    fun `happy path - user sends photo, bot forwards, approver confirms, photo is uploaded`() {
        // == Step 1: user sends a photo ==

        // Given: a user sends a photo to the bot chat
        val mediaMessage =
            Message(
                messageId = 10L,
                chat = userChat,
                date = 200,
                photo = listOf(PhotoSize("p1", "p1u", 100, 100)),
            )

        // When: the media handler processes the photo
        val mediaEnv =
            MediaHandlerEnvironment(
                bot,
                Update(updateId = 1, message = mediaMessage),
                mediaMessage,
                "dummy",
            )
        mediaHandler.handle(mediaEnv)

        // Then: the photo is forwarded to the approver chat
        verify(botClient).forwardMessage(eq(approverChatId), eq(userChatId), eq(10L))
        // Then: a 👀 reaction is added to the user's message
        verify(botClient).setMessageReaction(eq(userChatId), eq(10L), eq("👀"))

        // == Step 2: approver confirms the forwarded photo ==

        // Given: the forwarded photo exists in the approver chat
        val forwardedMessage =
            Message(
                messageId = 20L,
                chat = approverChat,
                date = 210,
                photo = listOf(PhotoSize("p1", "p1u", 100, 100)),
            )
        // Given: the approver replies with "this" on the forwarded message
        val approveMessage =
            Message(
                messageId = 21L,
                chat = approverChat,
                date = 220,
                from = approverUser,
                text = "this",
                replyToMessage = forwardedMessage,
            )
        whenever(botClient.downloadFileBytes("p1")).thenReturn(testImageBytes)

        // When: the this-command handler processes the approver's reply
        val approveEnv =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 2, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(approveEnv)

        // Then: the photo is downloaded
        // Then: the photo is uploaded to storage
        // Then: a 👍 reaction is added to the approver's message
        // Then: the video transcoder is never called
        val inOrder = inOrder(botClient, fileStorage, videoTranscoder)
        inOrder.verify(botClient).downloadFileBytes("p1")
        inOrder.verify(fileStorage).upload(testImageBytes, "media", "image/jpeg")
        inOrder.verify(botClient).setMessageReaction(eq(approverChatId), eq(21L), eq("👍"))
        inOrder.verify(videoTranscoder, never()).transcode(any())
    }

    @Test
    fun `happy path - user sends video, bot forwards, approver confirms, video is transcoded and uploaded`() {
        val videoFileId = "v999"

        // == Step 1: user sends a video ==

        // Given: a user sends a video to the bot chat
        val mediaMessage =
            Message(
                messageId = 30L,
                chat = userChat,
                date = 300,
                video = Video(videoFileId, "vu", 640, 480, 30),
            )

        // When: the media handler processes the video
        val mediaEnv =
            MediaHandlerEnvironment(
                bot,
                Update(updateId = 3, message = mediaMessage),
                mediaMessage,
                "dummy",
            )
        mediaHandler.handle(mediaEnv)

        // Then: the video is forwarded to the approver chat
        verify(botClient).forwardMessage(eq(approverChatId), eq(userChatId), eq(30L))
        // Then: a 👀 reaction is added to the user's message
        verify(botClient).setMessageReaction(eq(userChatId), eq(30L), eq("👀"))

        // == Step 2: approver confirms the forwarded video ==

        // Given: the forwarded video exists in the approver chat
        val forwardedMessage =
            Message(
                messageId = 40L,
                chat = approverChat,
                date = 310,
                video = Video(videoFileId, "vu", 640, 480, 30),
            )
        // Given: the approver replies with "this" on the forwarded message
        val approveMessage =
            Message(
                messageId = 41L,
                chat = approverChat,
                date = 320,
                from = approverUser,
                text = "this",
                replyToMessage = forwardedMessage,
            )
        val rawVideoBytes = byteArrayOf(0x00, 0x01, 0x02)
        val transcodedBytes = byteArrayOf(0x1A.toByte(), 0x45, 0xDF.toByte())
        whenever(botClient.downloadFileBytes(videoFileId)).thenReturn(rawVideoBytes)
        whenever(videoTranscoder.transcode(rawVideoBytes)).thenReturn(transcodedBytes)

        // When: the this-command handler processes the approver's reply
        val approveEnv =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 4, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(approveEnv)

        // Then: the video is downloaded, transcoded, uploaded, and reacted — in order
        val inOrder = inOrder(botClient, fileStorage, videoTranscoder)
        inOrder.verify(botClient).downloadFileBytes(videoFileId)
        inOrder.verify(videoTranscoder).transcode(rawVideoBytes)
        inOrder.verify(fileStorage).upload(transcodedBytes, "media", "video/webm")
        inOrder.verify(botClient).setMessageReaction(eq(approverChatId), eq(41L), eq("👍"))
    }

    // --------------- Edge cases: ThisCommandHandlerService ---------------

    @Test
    fun `should ignore message from non-approver`() {
        // Given: a non-approver user replies "this" on a forwarded media message
        val nonApproverUser = User(id = 999L, isBot = false, firstName = "Stranger")
        val mediaMessage =
            Message(
                messageId = 1L,
                chat = approverChat,
                date = 123,
                photo = listOf(PhotoSize("p1", "p1u", 100, 100)),
            )
        val approveMessage =
            Message(
                messageId = 2L,
                chat = approverChat,
                date = 124,
                from = nonApproverUser,
                text = "this",
                replyToMessage = mediaMessage,
            )

        // When: the this-command handler processes the message
        val env =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 3, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(env)

        // Then: no interactions with any dependency occur
        verifyNoInteractions(botClient, fileStorage, videoTranscoder)
    }

    @Test
    fun `should ignore non-confirm command text`() {
        // Given: the approver replies with a non-confirm text on a forwarded media message
        val mediaMessage =
            Message(
                messageId = 1L,
                chat = approverChat,
                date = 123,
                photo = listOf(PhotoSize("p1", "p1u", 100, 100)),
            )
        val approveMessage =
            Message(
                messageId = 2L,
                chat = approverChat,
                date = 124,
                from = approverUser,
                text = "nope",
                replyToMessage = mediaMessage,
            )

        // When: the this-command handler processes the message
        val env =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 4, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(env)

        // Then: no interactions with any dependency occur
        verifyNoInteractions(botClient, fileStorage, videoTranscoder)
    }

    @Test
    fun `should ignore message that is not a reply`() {
        // Given: the approver sends a "this" message that is not a reply
        val approveMessage =
            Message(
                messageId = 2L,
                chat = approverChat,
                date = 124,
                from = approverUser,
                text = "this",
            )

        // When: the this-command handler processes the message
        val env =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 5, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(env)

        // Then: no interactions with any dependency occur
        verifyNoInteractions(botClient, fileStorage, videoTranscoder)
    }

    @ParameterizedTest
    @ValueSource(strings = ["this", "true", "!soxok", "THIS", "tRuE"])
    fun `should accept alternative confirm commands`(command: String) {
        // Given: the approver replies with a valid confirm command variant on a forwarded photo
        val mediaMessage =
            Message(
                messageId = 1L,
                chat = approverChat,
                date = 123,
                photo = listOf(PhotoSize("px", "pxu", 100, 100)),
            )
        val approveMessage =
            Message(
                messageId = 2L,
                chat = approverChat,
                date = 124,
                from = approverUser,
                text = command,
                replyToMessage = mediaMessage,
            )
        whenever(botClient.downloadFileBytes("px")).thenReturn(testImageBytes)

        // When: the this-command handler processes the message
        val env =
            MessageHandlerEnvironment(
                bot,
                Update(updateId = 6, message = approveMessage),
                approveMessage,
            )
        thisCommandHandler.handle(env)

        // Then: the photo file is downloaded
        verify(botClient).downloadFileBytes("px")
    }

    // --------------- Edge cases: MediaHandlerService ---------------

    @Test
    fun `media handler should forward message from user chat`() {
        // Given: a user sends a photo to the bot chat (not the forward chat)
        val mediaMessage =
            Message(
                messageId = 10L,
                chat = userChat,
                date = 200,
                photo = listOf(PhotoSize("pf", "pfu", 200, 200)),
            )

        // When: the media handler processes the photo
        val env =
            MediaHandlerEnvironment(
                bot,
                Update(updateId = 10, message = mediaMessage),
                mediaMessage,
                "dummy",
            )
        mediaHandler.handle(env)

        // Then: the photo is forwarded to the approver chat
        verify(botClient).forwardMessage(eq(approverChatId), eq(userChatId), eq(10L))
        // Then: a 👀 reaction is added to the user's message
        verify(botClient).setMessageReaction(eq(userChatId), eq(10L), eq("👀"))
    }

    @Test
    fun `media handler should NOT forward message from the forward chat itself`() {
        // Given: a message is sent directly in the forward (approver) chat
        val mediaMessage =
            Message(
                messageId = 11L,
                chat = approverChat,
                date = 201,
                photo = listOf(PhotoSize("pf2", "pfu2", 200, 200)),
            )

        // When: the media handler processes the photo
        val env =
            MediaHandlerEnvironment(
                bot,
                Update(updateId = 11, message = mediaMessage),
                mediaMessage,
                "dummy",
            )
        mediaHandler.handle(env)

        // Then: the message is NOT forwarded
        verify(botClient, never()).forwardMessage(any(), any(), any())
        // Then: a 👀 reaction is still added
        verify(botClient).setMessageReaction(eq(approverChatId), eq(11L), eq("👀"))
    }
}
