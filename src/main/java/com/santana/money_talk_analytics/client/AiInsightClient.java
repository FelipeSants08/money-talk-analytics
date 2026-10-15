package com.santana.money_talk_analytics.client;

import com.santana.money_talk_analytics.dto.RequestAiInsight;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notification-service", url = "http://localhost:8081") // Replace with the actual URL of the AI Insight service
public interface AiInsightClient {

    @PostMapping("/ai-insight")
    void getAiInsight(@RequestBody RequestAiInsight request);
}
