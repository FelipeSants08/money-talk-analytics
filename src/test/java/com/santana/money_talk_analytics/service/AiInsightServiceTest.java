package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.Prompt;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AiInsightService - AI prompt generation and response handling")
class AiInsightServiceTest {

    @Mock
    private ChatModel chatModel;

    @InjectMocks
    private AiInsightService aiInsightService;

    // -------------------------------------------------------------------------
    // Helper builders
    // -------------------------------------------------------------------------

    private CoinMarketDTO btcCoin() {
        return new CoinMarketDTO(
                "bitcoin", "btc", "Bitcoin", "https://img.url",
                new BigDecimal("300000"),
                new BigDecimal("1000000000"),
                new BigDecimal("4.0"),
                new BigDecimal("8.5"),
                null
        );
    }

    private MarketAlert pumpAlert(CoinMarketDTO coin) {
        return new MarketAlert(coin, AlertType.PUMP_24H, new BigDecimal("8.5"), LocalDateTime.now());
    }

    /**
     * Builds a full mock chain: ChatModel -> ChatResponse -> Generation -> AssistantMessage
     * This reflects exactly how Spring AI structures the response object.
     */
    private void mockChatModelResponse(String responseText) {
        AssistantMessage message = mock(AssistantMessage.class);
        when(message.getText()).thenReturn(responseText);

        Generation generation = mock(Generation.class);
        when(generation.getOutput()).thenReturn(message);

        ChatResponse chatResponse = mock(ChatResponse.class);
        when(chatResponse.getResult()).thenReturn(generation);

        when(chatModel.call(any(Prompt.class))).thenReturn(chatResponse);
    }

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should return the text produced by the AI model")
    void shouldReturnAiGeneratedText() {
        String expectedText = "🚀 <b>Bitcoin</b> disparou!";
        mockChatModelResponse(expectedText);

        String result = aiInsightService.generateMarketSummary(List.of(pumpAlert(btcCoin())));

        assertThat(result).isEqualTo(expectedText);
    }

    @Test
    @DisplayName("should call ChatModel exactly once per invocation")
    void shouldCallChatModelExactlyOnce() {
        mockChatModelResponse("any response");

        aiInsightService.generateMarketSummary(List.of(pumpAlert(btcCoin())));

        verify(chatModel, times(1)).call(any(Prompt.class));
    }

    @Test
    @DisplayName("should include coin name in the prompt sent to the AI")
    void shouldIncludeCoinNameInPrompt() {
        mockChatModelResponse("response");
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        aiInsightService.generateMarketSummary(List.of(pumpAlert(btcCoin())));

        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getContents();
        assertThat(promptText).contains("Bitcoin");
    }

    @Test
    @DisplayName("should include all alerts data in the prompt when multiple alerts are provided")
    void shouldIncludeAllAlertsInPrompt() {
        mockChatModelResponse("multi-alert response");
        ArgumentCaptor<Prompt> promptCaptor = ArgumentCaptor.forClass(Prompt.class);

        CoinMarketDTO ethCoin = new CoinMarketDTO(
                "ethereum", "eth", "Ethereum", "https://img.url",
                new BigDecimal("15000"), new BigDecimal("500000000"),
                null, new BigDecimal("-8.0"), null
        );
        MarketAlert dumpAlert = new MarketAlert(ethCoin, AlertType.DUMP_24H, new BigDecimal("-8.0"), LocalDateTime.now());

        aiInsightService.generateMarketSummary(List.of(pumpAlert(btcCoin()), dumpAlert));

        verify(chatModel).call(promptCaptor.capture());
        String promptText = promptCaptor.getValue().getContents();
        assertThat(promptText).contains("Bitcoin").contains("Ethereum");
    }
}
