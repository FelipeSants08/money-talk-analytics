package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.client.TelegramClient;
import com.santana.money_talk_analytics.dto.TelegramMessageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramClient client;

    @Value("${telegram.chat-id:}")
    private String chatId;

    @Value("${telegram.bot-token:}")
    private String botToken;

    public void sendMessage(String message) {
        try {
            TelegramMessageDTO request =
                    new TelegramMessageDTO(chatId, message, "HTML");

            client.sendMessage(botToken, request);
            log.info("Notificação enviada com sucesso para o Telegram!");
        }catch (Exception e){
            log.error("Erro ao enviar mensagem para o Telegram", e);
        }

    }

}
