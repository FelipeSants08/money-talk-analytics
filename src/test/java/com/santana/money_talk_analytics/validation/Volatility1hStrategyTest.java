package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Volatility1hStrategy - 1h price swing detection")
class Volatility1hStrategyTest {

    private Volatility1hStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new Volatility1hStrategy();
    }

    private CoinMarketDTO coinWith1hChange(BigDecimal change) {
        return new CoinMarketDTO(
                "solana", "sol", "Solana", "https://img.url",
                new BigDecimal("900"),
                new BigDecimal("200000000"),
                change,  // 1h – the field under test
                null,
                null
        );
    }

    // -------------------------------------------------------------------------
    // Happy paths – alert SHOULD be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should generate HIGH_VOLATILITY_1H alert when positive swing is exactly 3%")
    void shouldGenerateAlertWhenPositiveSwingIsExactlyThreshold() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("3.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.HIGH_VOLATILITY_1H);
        assertThat(result.get().getTriggeredValue()).isEqualByComparingTo("3.0");
    }

    @Test
    @DisplayName("should generate HIGH_VOLATILITY_1H alert when negative swing is exactly -3% (uses abs)")
    void shouldGenerateAlertWhenNegativeSwingIsExactlyThreshold() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("-3.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.HIGH_VOLATILITY_1H);
        // triggered value must keep the original sign (negative)
        assertThat(result.get().getTriggeredValue()).isEqualByComparingTo("-3.0");
    }

    @Test
    @DisplayName("should generate alert when positive swing exceeds threshold (+5%)")
    void shouldGenerateAlertWhenPositiveSwingExceedsThreshold() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("5.5"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getCoin()).isEqualTo(coin);
    }

    @Test
    @DisplayName("should generate alert when negative swing exceeds threshold (-4%)")
    void shouldGenerateAlertWhenNegativeSwingExceedsThreshold() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("-4.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
    }

    // -------------------------------------------------------------------------
    // Edge cases – alert SHOULD NOT be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should NOT generate alert when swing is just below threshold (2.99%)")
    void shouldNotGenerateAlertWhenSwingIsJustBelowThreshold() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("2.99"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when swing is small and negative (-1%)")
    void shouldNotGenerateAlertForSmallNegativeSwing() {
        CoinMarketDTO coin = coinWith1hChange(new BigDecimal("-1.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when 1h price change is null")
    void shouldNotGenerateAlertWhenChangeIsNull() {
        CoinMarketDTO coin = coinWith1hChange(null);

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when change is zero")
    void shouldNotGenerateAlertWhenChangeIsZero() {
        CoinMarketDTO coin = coinWith1hChange(BigDecimal.ZERO);

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }
}
