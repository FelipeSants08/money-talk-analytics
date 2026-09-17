package com.santana.money_talk_analytics.client;

import com.santana.money_talk_analytics.dto.TelegramMessageDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "telegramClient", url = "https://api.telegram.org")
public interface TelegramClient {

    @PostMapping("/bot{token}/sendMessage")
    void sendMessage(
            @PathVariable String token,
            @RequestBody TelegramMessageDTO request
    );

}
