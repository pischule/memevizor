package com.pischule.memevizor

import aws.sdk.kotlin.services.s3.S3Client
import com.pischule.memevizor.bot.BotService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

/**
 * Verifies that the production bean graph loads without circular dependency errors.
 *
 * Unlike the main test suite (which uses the "test" profile with lazy initialization
 * and @MockitoBean for BotClient), this test uses the default profile with eager initialization.
 * Only side-effect-causing beans are mocked.
 */
@SpringBootTest(
    properties =
        [
            "bot.token=verify-token",
            "bot.forward-chat-id=1",
            "bot.approver-user-ids=2,3",
            "s3.endpoint=https://localhost",
            "s3.region=us-east-1",
            "s3.access-key-id=test-key",
            "s3.secret-access-key=test-secret",
            "s3.bucket=test-bucket",
        ]
)
class ProductionContextLoadTest {

    @MockitoBean private lateinit var botService: BotService

    @MockitoBean private lateinit var s3Client: S3Client

    @Test
    fun `context loads without circular dependency`() {
        // If we reach this point, the ApplicationContext was created
        // successfully — no circular dependency or other wiring errors.
    }
}
