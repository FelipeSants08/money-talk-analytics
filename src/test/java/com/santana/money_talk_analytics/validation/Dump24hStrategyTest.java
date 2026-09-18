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

@DisplayName("Dump24hStrategy - 24h price drop detection")
class Dump24hStrategyTest {

    private Dump24hStrategy strategy;

    @BeforeEach
    void setUp() {
        MarketProperties.Thresholds thresholds = new MarketProperties.Thresholds(
                new BigDecimal("7.0"),
                new BigDecimal("-7.0"),
                new BigDecimal("3.0")
        );
        strategy = new Dump24hStrategy(new MarketProperties(thresholds, 20, "brl"));
    }

    // -------------------------------------------------------------------------
    // Helper: builds a CoinMarketDTO with only the fields relevant to this strategy
    // -------------------------------------------------------------------------
    private CoinMarketDTO coinWith24hChange(BigDecimal change) {
        return new CoinMarketDTO(
                "bitcoin", "btc", "Bitcoin", "https://img.url",
                new BigDecimal("250000"),
                new BigDecimal("1000000000"),
                null,          // 1h – not used here
                change,        // 24h – the field under test
                null           // 7d – not used here
        );
    }

    // -------------------------------------------------------------------------
    // Happy paths – alert SHOULD be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should generate DUMP_24H alert when drop is exactly -7%")
    void shouldGenerateAlertWhenDropIsExactlyThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("-7.0"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.DUMP_24H);
        assertThat(result.get().getTriggeredValue()).isEqualByComparingTo("-7.0");
    }

    @Test
    @DisplayName("should generate DUMP_24H alert when drop is greater than -7% (e.g. -10%)")
    void shouldGenerateAlertWhenDropIsBeyondThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("-10.5"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isPresent();
        assertThat(result.get().getAlertType()).isEqualTo(AlertType.DUMP_24H);
        assertThat(result.get().getCoin()).isEqualTo(coin);
    }

    @Test
    @DisplayName("should populate coin reference correctly inside the alert")
    void shouldPopulateCoinReferenceInAlert() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("-8.0"));

        MarketAlert alert = strategy.validate(coin).orElseThrow();

        assertThat(alert.getCoin().id()).isEqualTo("bitcoin");
        assertThat(alert.getCoin().symbol()).isEqualTo("btc");
        assertThat(alert.getCreatedAt()).isNotNull();
    }

    // -------------------------------------------------------------------------
    // Edge cases – alert SHOULD NOT be generated
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("should NOT generate alert when drop is just above threshold (-6.99%)")
    void shouldNotGenerateAlertWhenDropIsJustAboveThreshold() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("-6.99"));

        Optional<MarketAlert> result = strategy.validate(coin);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should NOT generate alert when price change is positive")
    void shouldNotGenerateAlertForPositiveChange() {
        CoinMarketDTO coin = coinWith24hChange(new BigDecimal("5.0"));

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
