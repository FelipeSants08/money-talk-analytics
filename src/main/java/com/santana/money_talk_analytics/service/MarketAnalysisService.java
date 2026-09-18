package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.validation.MarketValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketAnalysisService {

    private final List<MarketValidationStrategy> validations;
    private final StringRedisTemplate redisTemplate;

    private static final Duration ALERT_COOL_DOWN = Duration.ofHours(4);

    public List<MarketAlert> analyzeMarket(List<CoinMarketDTO> coins){

        return coins.stream()
                .flatMap(coin -> validations.stream()
                        .map(validation -> validation.validate(coin))
                        .filter(Optional::isPresent)
                        .map(Optional::get))
                .filter(this::isNewAlert)
                .toList();
    }

    private boolean isNewAlert(MarketAlert alert){

        String redisKey = String.format("alert:%s:%s", alert.getCoin().id(), alert.getAlertType());

        Boolean isNewKeyCreated = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "notified", ALERT_COOL_DOWN);

        if (Boolean.TRUE.equals(isNewKeyCreated)){
            log.info("Novo alerta detectado e salvo no Redis: {}", redisKey);
            return true;
        }

        log.info("Alerta ignorado por deduplicação (já enviado nas últimas 4h): {}", redisKey);
        return false;
    }
}
