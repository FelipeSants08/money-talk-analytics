package com.santana.money_talk_analytics.model;

import lombok.Getter;

@Getter
public enum AlertType {
    HIGH_VOLATILITY_1H("Oscilação brusca na última 1 hora"),
    PUMP_24H("Forte alta nas últimas 24 horas"),
    DUMP_24H("Forte queda nas últimas 24 horas"),
    HIGH_VOLATILITY_7D("Tendência expressiva nos últimos 7 dias");

    private final String description;

    AlertType(String description) {
        this.description = description;
    }

}
