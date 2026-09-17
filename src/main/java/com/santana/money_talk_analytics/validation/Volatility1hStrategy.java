package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class Volatility1hStrategy implements MarketValidationStrategy{

    private static final BigDecimal THRESHOLD_1H = new BigDecimal("3.0");

    @Override
    public Optional<MarketAlert> validate(CoinMarketDTO coin) {
        if (coin.priceChangePercentage1h() == null){
            return Optional.empty();
        }

        BigDecimal change = coin.priceChangePercentage1h().abs();

        if (change.compareTo(THRESHOLD_1H) >= 0){
            return Optional.of(new MarketAlert(
                    coin,
                    AlertType.HIGH_VOLATILITY_1H,
                    coin.priceChangePercentage1h(),
                    LocalDateTime.now()
                    )
            );
        }

        return Optional.empty();
    }
}
