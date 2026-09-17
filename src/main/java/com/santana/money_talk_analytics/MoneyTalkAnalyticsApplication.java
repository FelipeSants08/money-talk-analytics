package com.santana.money_talk_analytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class MoneyTalkAnalyticsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneyTalkAnalyticsApplication.class, args);
	}

}
