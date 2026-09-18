package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.validation.MarketValidationStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MarketAnalysisService - strategy pipeline and Redis deduplication")
class MarketAnalysisServiceTest {

    @Mock
    private MarketValidationStrategy strategyA;

    @Mock
    private MarketValidationStrategy strategyB;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    // Spring injects List<MarketValidationStrategy> — we need to construct the
    // service manually so we can pass both mocked strategies in the list.
    private MarketAnalysisService service;

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private CoinMarketDTO coin(String id) {
        return new CoinMarketDTO(
                id, id, id.toUpperCase(), "https://img.url",
                new BigDecimal("1000"), new BigDecimal("100000"),
                null, null, null
        );
    }

    private MarketAlert alertFor(CoinMarketDTO coin, AlertType type) {
        return new MarketAlert(coin, type, new BigDecimal("10.0"), LocalDateTime.now());
    }

    /**
     * Prepares the Redis mock so that setIfAbsent returns the given value.
     * true  -> key did not exist -> new alert (should pass through)
     * false -> key already existed -> duplicate (should be filtered out)
     */
    private void givenRedisSetIfAbsentReturns(Boolean value) {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenReturn(value);
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should return alerts that pass validation and are new in Redis")
    void shouldReturnNewAlerts() {
        service = new MarketAnalysisService(List.of(strategyA), redisTemplate);
        CoinMarketDTO coin = coin("bitcoin");
        MarketAlert alert = alertFor(coin, AlertType.PUMP_24H);

        when(strategyA.validate(coin)).thenReturn(Optional.of(alert));
        givenRedisSetIfAbsentReturns(true); // Redis considers it a new key

        List<MarketAlert> result = service.analyzeMarket(List.of(coin));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAlertType()).isEqualTo(AlertType.PUMP_24H);
    }

    @Test
    @DisplayName("should deduplicate alerts already present in Redis")
    void shouldFilterOutDuplicateAlertsFromRedis() {
        service = new MarketAnalysisService(List.of(strategyA), redisTemplate);
        CoinMarketDTO coin = coin("bitcoin");
        MarketAlert alert = alertFor(coin, AlertType.PUMP_24H);

        when(strategyA.validate(coin)).thenReturn(Optional.of(alert));
        givenRedisSetIfAbsentReturns(false); // Redis says key already exists

        List<MarketAlert> result = service.analyzeMarket(List.of(coin));

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should return empty list when no strategy triggers an alert")
    void shouldReturnEmptyWhenNoStrategyTriggered() {
        service = new MarketAnalysisService(List.of(strategyA, strategyB), redisTemplate);
        CoinMarketDTO coin = coin("bitcoin");

        when(strategyA.validate(coin)).thenReturn(Optional.empty());
        when(strategyB.validate(coin)).thenReturn(Optional.empty());

        List<MarketAlert> result = service.analyzeMarket(List.of(coin));

        assertThat(result).isEmpty();
        verifyNoInteractions(redisTemplate); // Redis must NOT be touched if no alert was triggered
    }

    @Test
    @DisplayName("should apply all strategies to each coin independently")
    void shouldApplyAllStrategiesPerCoin() {
        service = new MarketAnalysisService(List.of(strategyA, strategyB), redisTemplate);
        CoinMarketDTO coin = coin("ethereum");
        MarketAlert alertFromA = alertFor(coin, AlertType.PUMP_24H);
        MarketAlert alertFromB = alertFor(coin, AlertType.HIGH_VOLATILITY_1H);

        when(strategyA.validate(coin)).thenReturn(Optional.of(alertFromA));
        when(strategyB.validate(coin)).thenReturn(Optional.of(alertFromB));
        givenRedisSetIfAbsentReturns(true);

        List<MarketAlert> result = service.analyzeMarket(List.of(coin));

        assertThat(result).hasSize(2);
        verify(strategyA, times(1)).validate(coin);
        verify(strategyB, times(1)).validate(coin);
    }

    @Test
    @DisplayName("should process multiple coins and aggregate all new alerts")
    void shouldProcessMultipleCoins() {
        service = new MarketAnalysisService(List.of(strategyA), redisTemplate);
        CoinMarketDTO btc = coin("bitcoin");
        CoinMarketDTO eth = coin("ethereum");
        MarketAlert btcAlert = alertFor(btc, AlertType.DUMP_24H);
        MarketAlert ethAlert = alertFor(eth, AlertType.DUMP_24H);

        when(strategyA.validate(btc)).thenReturn(Optional.of(btcAlert));
        when(strategyA.validate(eth)).thenReturn(Optional.of(ethAlert));
        givenRedisSetIfAbsentReturns(true);

        List<MarketAlert> result = service.analyzeMarket(List.of(btc, eth));

        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("should return empty list when coin list is empty")
    void shouldReturnEmptyListWhenNoCoinIsProvided() {
        service = new MarketAnalysisService(List.of(strategyA), redisTemplate);

        List<MarketAlert> result = service.analyzeMarket(List.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(strategyA, redisTemplate);
    }

    @Test
    @DisplayName("should build correct Redis key using coin id and alert type")
    void shouldBuildCorrectRedisKey() {
        service = new MarketAnalysisService(List.of(strategyA), redisTemplate);
        CoinMarketDTO coin = coin("bitcoin");
        MarketAlert alert = alertFor(coin, AlertType.PUMP_24H);

        when(strategyA.validate(coin)).thenReturn(Optional.of(alert));
        givenRedisSetIfAbsentReturns(true);

        service.analyzeMarket(List.of(coin));

        // Expected key format: "alert:<coinId>:<alertType>"
        verify(valueOperations).setIfAbsent(eq("alert:bitcoin:PUMP_24H"), anyString(), any(Duration.class));
    }
}
