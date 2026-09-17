package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.validation.MarketValidationStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketAnalysisService {

    private final List<MarketValidationStrategy> validations;

    public List<MarketAlert> analyzeMarket(List<CoinMarketDTO> coins){

        return coins.stream()
                .flatMap(coin -> validations.stream()
                        .map(validation -> validation.validate(coin))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .toList();
    }
}
