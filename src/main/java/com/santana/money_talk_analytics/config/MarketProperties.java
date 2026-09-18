package com.santana.money_talk_analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "market")
public record MarketProperties(
        Thresholds thresholds,
        int topCoins,
        String currency
) {
    public record Thresholds(
            BigDecimal pump24h,
            BigDecimal dump24h,
            BigDecimal volatility1h
    ) {}
}
