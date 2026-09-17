package com.santana.money_talk_analytics.validation;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.MarketAlert;

import java.util.Optional;

public interface MarketValidationStrategy {
    Optional<MarketAlert> validate(CoinMarketDTO coin);
}
