package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.config.MarketProperties;
import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pump24hStrategy - 24h price surge detection")
class Pump24hStrategyTest {

    private Pump24hStrategy strategy;

    @BeforeEach
    void setUp() {
        MarketProperties.Thresholds thresholds = new MarketProperties.Thresholds(
                new BigDecimal("7.0"),
                new BigDecimal("-7.0"),
                new BigDecimal("3.0")
        );
        strategy = new Pump24hStrategy(new MarketProperties(thresholds, 20, "brl"));
    }

    private CoinMarketDTO coinWith24hChange(BigDecimal change) {
        return new CoinMarketDTO(
                "ethereum", "eth", "Ethereum", "https://img.url",
                new BigDecimal("15000"),
                new BigDecimal("500000000"),
                null,
                change,
                null
        );
    }

    // -------------------------------------------------------------------------
    // Happy paths – alert SHOULD be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should generate PUMP_24H alert when gain is exactly 7%")
    void shouldGenerateAlertWhenGainIsExactlyThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("7.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.PUMP_24H);
        assertThat(result.get().getTriggeredValue()).isEqualByComparingTo("7.0");
    }

    @Test
    @DisplayName("should generate PUMP_24H alert when gain exceeds threshold (e.g. +15%)")
    void shouldGenerateAlertWhenGainExceedsThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("15.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.PUMP_24H);
        assertThat(result.get().getCoin()).isEqualTo(coin);
    }

    @Test
    @DisplayName("should populate coin reference and createdAt correctly inside the alert")
    void shouldPopulateCoinAndCreatedAtInAlert() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("9.5"));

        MarketAlert alert = strategy.validate(coin).orElseThrow();

        assertThat(alert.getCoin().id()).isEqualTo("ethereum");
        assertThat(alert.getCreatedAt()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Edge cases – alert SHOULD NOT be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should NOT generate alert when gain is just below threshold (6.99%)")
    void shouldNotGenerateAlertWhenGainIsJustBelowThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("6.99"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when price change is negative")
    void shouldNotGenerateAlertForNegativeChange() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("-5.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when price change is zero")
    void shouldNotGenerateAlertWhenChangeIsZero() {
        CoinMarketDTO coin = coinWith24hChange(BigDecimal.ZERO);

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when 24h price change is null")
    void shouldNotGenerateAlertWhenChangeIsNull() {
        CoinMarketDTO coin = coinWith24hChange(null);

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }
}
