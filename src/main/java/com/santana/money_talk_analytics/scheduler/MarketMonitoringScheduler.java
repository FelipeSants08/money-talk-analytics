package com.santana.money_talk_analytics.scheduler;

import com.santana.money_talk_analytics.client.CoinGeckoClient;
import com.santana.money_talk_analytics.config.MarketProperties;
import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.service.AiInsightService;
import com.santana.money_talk_analytics.service.MarketAnalysisService;
import com.santana.money_talk_analytics.service.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketMonitoringScheduler {

    private final CoinGeckoClient client;
    private final MarketAnalysisService service;
    private final AiInsightService aiInsight;
    private final TelegramService telegramService;
    private final MarketProperties marketProperties;

    @Value("${coingecko.api.key:}")
    private String apiKey;

    @Scheduled(fixedRate = 20, timeUnit = TimeUnit.MINUTES)
    public void monitorMarket() {
        log.info("Iniciando monitoramento do mercado de criptomoedas...");

        try {
            List<CoinMarketDTO> topCoins = client.getTopCoins(
                    apiKey,
                    marketProperties.currency(),
                    "market_cap_desc",
                    marketProperties.topCoins(),
                    1,
                    false,
                    "1h,24h,7d");

            List<MarketAlert> alerts = service.analyzeMarket(topCoins);
            log.info("Número de alertas ativos: " + alerts.size());

            if (alerts.isEmpty()) {
                log.info("Mercado estável. Nenhum alerta relevante gerado nesta checagem.");
                return;
            }

            log.info("Atenção! Foram identificados {} alertas de volatilidade no mercado:", alerts.size());
            alerts.forEach(alert -> log.info("Alerta: {} - Moeda: {} - Variação: {}%",
                    alert.getAlertType(),
                    alert.getCoin().name(),
                    alert.getTriggeredValue()));

            String insightText = aiInsight.generateMarketSummary(alerts);
            log.info("Mensagem do Agente recebida!");

            telegramService.sendMessage(insightText);

        } catch (Exception e) {
            log.error("Erro ao executar o monitoramento de mercado", e);
        }
    }
}
