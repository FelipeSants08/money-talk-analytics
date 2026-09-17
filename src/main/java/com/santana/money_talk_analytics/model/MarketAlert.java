package com.santana.money_talk_analytics.model;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
public class MarketAlert {
    private CoinMarketDTO coin;
    private AlertType alertType;
    private BigDecimal triggeredValue;
    private LocalDateTime createdAt;

    public MarketAlert(CoinMarketDTO coin, AlertType alertType, BigDecimal triggeredValue, LocalDateTime createdAt) {
        this.coin = coin;
        this.alertType = alertType;
        this.triggeredValue = triggeredValue;
        this.createdAt = createdAt;
    }
}
