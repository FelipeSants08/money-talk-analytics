package com.santana.money_talk_analytics.client;

import com.santana.money_talk_analytics.dto.CoinMarketDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "coinGeckoClient", url = "https://api.coingecko.com/api/v3")
public interface CoinGeckoClient {

    @GetMapping("/coins/markets")
    List<CoinMarketDTO> getTopCoins(
            @RequestHeader(value = "x-cg-demo-api-key", required = false) String apiKey,
            @RequestParam(value = "vs_currency", defaultValue = "brl") String vsCurrency,
            @RequestParam(value = "order", defaultValue = "market_cap_desc") String order,
            @RequestParam(value = "per_page", defaultValue = "20") int perPage,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sparkline", defaultValue = "false") boolean sparkline,
            @RequestParam(value = "price_change_percentage", defaultValue = "1h,24h,7d") String priceChangePercentage
    );

}
