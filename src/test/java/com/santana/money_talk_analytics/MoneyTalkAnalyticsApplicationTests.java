package com.santana.money_talk_analytics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Smoke test: verifies that the Spring application context loads without errors.
 *
 * External services (Redis, CoinGecko, Telegram, Gemini) are replaced with
 * dummy values so this test runs without any real infrastructure.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.ai.google.genai.api-key=test-key",
        "spring.data.redis.host=localhost",
        "coingecko.api.key=test-key",
        "telegram.bot-token=test-token",
        "telegram.chat-id=test-chat-id",
        "spring.data.redis.port=6379"
})
class MoneyTalkAnalyticsApplicationTests {

    @Test
    void contextLoads() {
        // Verifies that all beans are wired correctly and the context starts up.
        // No assertions needed — a failure here means a misconfigured bean or
        // missing required property.
    }

}
