package com.santana.money_talk_analytics.scheduler;

import com.santana.money_talk_analytics.client.CoinGeckoClient;
import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import com.santana.money_talk_analytics.model.MarketAlert;
import com.santana.money_talk_analytics.service.AiInsightService;
import com.santana.money_talk_analytics.service.MarketAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarketMonitoringScheduler {

    private final CoinGeckoClient client;
    private final MarketAnalysisService service;
    private final AiInsightService aiInsight;

    @Value("${coingecko.api.key:}")
    private String apiKey;

    @Scheduled(fixedRate = 100000)
    public void monitorMarket(){
        log.info("Iniciando monitoramento do mercado de criptomoedas...");

        try {
            log.info(apiKey);
            List<CoinMarketDTO> topCoins = client.getTopCoins(
                    apiKey, "brl", "market_cap_desc", 20, 1, false, "1h,24h,7d");

            List<MarketAlert> alerts = service.analyzeMarket(topCoins);

            if (alerts.isEmpty()){
                log.info("Mercado estável. Nenhum alerta relevante gerado nesta checagem.");
                return;
            }

            log.info("Atenção! Foram identificados {} alertas de volatilidade no mercado:", alerts.size());
            alerts.forEach(alert -> log.info("Alerta: {} - Moeda: {} - Variação: {}%",
                    alert.getAlertType(),
                    alert.getCoin().name(),
                    alert.getTriggeredValue()));

            String insightText = aiInsight.generateMarketSummary(alerts);
            log.info("\n=== ANÁLISE GERADA PELA IA ===\n{}\n==============================", insightText);

        } catch (Exception e){
            log.error("Erro ao executar o monitoramento de mercado", e);
        }
    }

}
