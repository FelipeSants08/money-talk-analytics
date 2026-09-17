package com.santana.money_talk_analytics.service;

import com.santana.money_talk_analytics.model.MarketAlert;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightService {

    private final ChatModel chatModel;

    public String generateMarketSummary(List<MarketAlert> alerts){

        String alertsData = alerts.stream()
                .map(alert -> String.format("- %s (%s): Preço R$ %s | Tipo: %s | Variação: %s%%",
                        alert.getCoin().name(),
                        alert.getCoin().symbol().toUpperCase(),
                        alert.getCoin().currentPrice(),
                        alert.getAlertType(),
                        alert.getTriggeredValue()))
                .collect(Collectors.joining("\n"));

        String promptText = """
                Você é um analista especialista no mercado de criptomoedas e atua como bot de notícias de um canal do Telegram.
                
                Com base nos seguintes alertas de volatilidade detectados pelo sistema em tempo real:
                
                %s
                
                Escreva uma mensagem engajadora e objetiva em Português do Brasil para o canal do Telegram.
                Instruções de formatação:
                - Use emojis para destacar as altas (🚀/🟢) e quedas (⚠️/🔴).
                - Seja direto ao ponto (máximo de 3 parágrafos curtos ou tópicos).
                - Forneça uma breve leitura técnica do porquê esse movimento importa.
                - Não adicione introduções genéricas como "Aqui está a sua mensagem:". Vá direto ao texto final.
                """.formatted(alertsData);

        Prompt prompt = new Prompt(
                promptText,
                GoogleGenAiChatOptions.builder()
                        .model("gemini-3.6-flash")
                        .build()
        );

        log.info("Enviando dados para processamento da IA Gemini...");

        return chatModel.call(prompt).getResult().getOutput().getText();
    }
}
