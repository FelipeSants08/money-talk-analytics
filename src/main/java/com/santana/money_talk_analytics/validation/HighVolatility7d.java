package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.config.MarketProperties;
import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class HighVolatility7d implements MarketValidationStrategy {

    private final MarketProperties marketProperties;

    @Override
    public Optional<MarketAlert> validate(CoinMarketDTO coin) {
        if (coin.priceChangePercentage7d() == null) {
            return Optional.empty();
        }
        if (coin.priceChangePercentage7d().abs().compareTo(marketProperties.thresholds().highVolatility7Days()) >= 0) {
            return Optional.of(new MarketAlert(
                    coin,
                    AlertType.HIGH_VOLATILITY_7D,
                    coin.priceChangePercentage7d(),
                    LocalDateTime.now()
            ));
        }
        return Optional.empty();
    }
}

