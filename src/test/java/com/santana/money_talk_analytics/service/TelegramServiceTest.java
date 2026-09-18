package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.client.TelegramClient;
import com.santana.money_talk_analytics.dto.TelegramMessageDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TelegramService - message dispatching")
class TelegramServiceTest {

    @Mock
    private TelegramClient client;

    @InjectMocks
    private TelegramService telegramService;

    @BeforeEach
    void setUp() {
        // Inject @Value fields that Spring would normally resolve from application.yaml
        ReflectionTestUtils.setField(telegramService, "chatId", "123456789");
        ReflectionTestUtils.setField(telegramService, "botToken", "bot-token-test");
    }

    // -------------------------------------------------------------------------
    // Happy path
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should call TelegramClient with correct token and message body")
    void shouldCallClientWithCorrectTokenAndBody() {
        String message = "🚀 Bitcoin up 10%!";

        telegramService.sendMessage(message);

        ArgumentCaptor<TelegramMessageDTO> bodyCaptor = ArgumentCaptor.forClass(TelegramMessageDTO.class);
        verify(client, times(1)).sendMessage(eq("bot-token-test"), bodyCaptor.capture());

        TelegramMessageDTO sent = bodyCaptor.getValue();
        assertThat(sent.chatId()).isEqualTo("123456789");
        assertThat(sent.text()).isEqualTo(message);
        assertThat(sent.parseMode()).isEqualTo("HTML");
    }

    @Test
    @DisplayName("should use parse mode HTML when sending any message")
    void shouldAlwaysUseParseModeHtml() {
        telegramService.sendMessage("any message");

        ArgumentCaptor<TelegramMessageDTO> captor = ArgumentCaptor.forClass(TelegramMessageDTO.class);
        verify(client).sendMessage(any(), captor.capture());

        assertThat(captor.getValue().parseMode()).isEqualTo("HTML");
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should NOT propagate exception when TelegramClient throws")
    void shouldSwallowExceptionWhenClientFails() {
        doThrow(new RuntimeException("Telegram API unavailable"))
                .when(client).sendMessage(any(), any());

        // sendMessage must not rethrow — the service catches and logs the error
        assertThatNoException().isThrownBy(() -> telegramService.sendMessage("test message"));
    }

    @Test
    @DisplayName("should still attempt to send even when message is empty")
    void shouldAttemptSendWithEmptyMessage() {
        telegramService.sendMessage("");

        verify(client, times(1)).sendMessage(any(), any());
    }
}
