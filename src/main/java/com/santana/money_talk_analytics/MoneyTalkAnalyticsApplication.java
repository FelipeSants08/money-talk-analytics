package com.santana.money_talk_analytics;

import com.santana.money_talk_analytics.config.MarketProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
@EnableConfigurationProperties(MarketProperties.class)
public class MoneyTalkAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTalkAnalyticsApplication.class, args);
	}

}
