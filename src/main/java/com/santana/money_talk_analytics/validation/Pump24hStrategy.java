package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.AlertType;
import com.santana.money_talk_analytics.model.MarketAlert;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class Pump24hStrategy implements MarketValidationStrategy{

    private static final BigDecimal THRESHOLD_PUMP = new BigDecimal("7.0");

    @Override
    public Optional<MarketAlert> validate(CoinMarketDTO coin) {
        if (coin.priceChangePercentage24h() == null){
            return Optional.empty();
        }

        if (coin.priceChangePercentage24h().compareTo(THRESHOLD_PUMP) >= 0){
            return Optional.of(new MarketAlert(
                    coin,
                    AlertType.PUMP_24H,
                    coin.priceChangePercentage24h(),
                    LocalDateTime.now()
            ));
        }
        return Optional.empty();
    }
}
