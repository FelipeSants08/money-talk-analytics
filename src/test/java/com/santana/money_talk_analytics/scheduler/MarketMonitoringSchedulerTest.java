package com.santana.money_talk_analytics.scheduler;

import com.santana.money_talk_analytics.client.CoinGeckoClient;
import com.santana.money_talk_analytics.config.MarketProperties;
import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.service.AiInsightService;
import com.santana.money_talk_analytics.service.MarketAnalysisService;
import com.santana.money_talk_analytics.service.TelegramService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketMonitoringScheduler - orchestration logic")
class MarketMonitoringSchedulerTest {

    @Mock
    private CoinGeckoClient coinGeckoClient;

    @Mock
    private MarketAnalysisService marketAnalysisService;

    @Mock
    private AiInsightService aiInsightService;

    @Mock
    private TelegramService telegramService;

    @Mock
    private MarketProperties marketProperties;

    @InjectMocks
    private MarketMonitoringScheduler scheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(scheduler, "apiKey", "test-api-key");

        MarketProperties.Thresholds thresholds = new MarketProperties.Thresholds(
                new BigDecimal("7.0"), new BigDecimal("-7.0"), new BigDecimal("3.0"));
        lenient().when(marketProperties.currency()).thenReturn("brl");
        lenient().when(marketProperties.topCoins()).thenReturn(20);
        lenient().when(marketProperties.thresholds()).thenReturn(thresholds);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CoinMarketDTO fakeCoin() {
        return new CoinMarketDTO(
                "bitcoin", "btc", "Bitcoin", "https://img.url",
                new BigDecimal("300000"), new BigDecimal("1000000000"),
                new BigDecimal("4.0"), new BigDecimal("8.5"), null
        );
    }

    private MarketAlert fakeAlert(CoinMarketDTO coin) {
        return new MarketAlert(coin, AlertType.PUMP_24H, new BigDecimal("8.5"), LocalDateTime.now());
    }

    // -------------------------------------------------------------------------
    // Happy paths
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should fetch coins, analyse market, generate AI insight and send Telegram message")
    void shouldExecuteFullPipelineWhenAlertsExist() {
        CoinMarketDTO coin = fakeCoin();
        MarketAlert alert = fakeAlert(coin);
        String aiMessage = "🚀 Bitcoin disparou!";

        when(coinGeckoClient.getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenReturn(List.of(coin));
        when(marketAnalysisService.analyzeMarket(List.of(coin)))
                .thenReturn(List.of(alert));
        when(aiInsightService.generateMarketSummary(List.of(alert)))
                .thenReturn(aiMessage);

        scheduler.monitorMarket();

        verify(coinGeckoClient, times(1)).getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any());
        verify(marketAnalysisService, times(1)).analyzeMarket(List.of(coin));
        verify(aiInsightService, times(1)).generateMarketSummary(List.of(alert));
        verify(telegramService, times(1)).sendMessage(aiMessage);
    }

    @Test
    @DisplayName("should NOT call AI or Telegram when no alerts are generated")
    void shouldSkipAiAndTelegramWhenMarketIsStable() {
        when(coinGeckoClient.getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenReturn(List.of(fakeCoin()));
        when(marketAnalysisService.analyzeMarket(any()))
                .thenReturn(List.of()); // empty = market stable

        scheduler.monitorMarket();

        verifyNoInteractions(aiInsightService);
        verifyNoInteractions(telegramService);
    }

    @Test
    @DisplayName("should pass the configured API key to CoinGecko client")
    void shouldPassApiKeyToCoinGeckoClient() {
        when(coinGeckoClient.getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenReturn(List.of());
        when(marketAnalysisService.analyzeMarket(any())).thenReturn(List.of());

        scheduler.monitorMarket();

        verify(coinGeckoClient).getTopCoins(
                eq("test-api-key"), any(), any(), anyInt(), anyInt(), anyBoolean(), any());
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should NOT propagate exception when CoinGecko client throws")
    void shouldSwallowExceptionWhenCoinGeckoFails() {
        when(coinGeckoClient.getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenThrow(new RuntimeException("CoinGecko API down"));

        // monitorMarket must not rethrow — it catches and logs the exception
        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> scheduler.monitorMarket());
    }

    @Test
    @DisplayName("should NOT call Telegram when AI service throws")
    void shouldNotCallTelegramWhenAiServiceFails() {
        CoinMarketDTO coin = fakeCoin();
        MarketAlert alert = fakeAlert(coin);

        when(coinGeckoClient.getTopCoins(any(), any(), any(), anyInt(), anyInt(), anyBoolean(), any()))
                .thenReturn(List.of(coin));
        when(marketAnalysisService.analyzeMarket(any())).thenReturn(List.of(alert));
        when(aiInsightService.generateMarketSummary(any()))
                .thenThrow(new RuntimeException("Gemini API timeout"));

        // Exception must be swallowed and Telegram must never be called
        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> scheduler.monitorMarket());

        verifyNoInteractions(telegramService);
    }
}
