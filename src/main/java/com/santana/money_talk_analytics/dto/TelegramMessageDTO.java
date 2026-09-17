package com.santana.money_talk_analytics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessageDTO(
        @JsonProperty("chat_id")
        String chatId, String text,
        @JsonProperty("parse_mode")
        String parseMode) {
}
