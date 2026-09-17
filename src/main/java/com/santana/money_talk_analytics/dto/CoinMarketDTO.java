package com.santana.money_talk_analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CoinMarketDTO(
        String id,
        String symbol,
        String name,
        String image,

        @JsonProperty("current_price")
        BigDecimal currentPrice,

        @JsonProperty("total_volume")
        BigDecimal totalVolume,

        @JsonProperty("price_change_percentage_1h_in_currency")
        BigDecimal priceChangePercentage1h,

        @JsonProperty("price_change_percentage_24h_in_currency")
        BigDecimal priceChangePercentage24h,

        @JsonProperty("price_change_percentage_7d_in_currency")
        BigDecimal priceChangePercentage7d
) {
}
