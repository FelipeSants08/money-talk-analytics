# 📊 Money Talk Analytics

Bot de monitoramento de criptomoedas que detecta movimentos relevantes de mercado e envia alertas inteligentes para um canal do Telegram, com análise gerada por IA.

## Como funciona

A cada **20 minutos**, o sistema busca os dados das top 20 criptomoedas por capitalização de mercado via [CoinGecko](https://www.coingecko.com/). Para cada moeda, aplica um conjunto de estratégias de validação. Quando algum alerta é disparado, o [Google Gemini](https://ai.google.dev/) gera uma mensagem analítica e engajadora que é enviada diretamente para o Telegram.

Para evitar spam, alertas repetidos para a mesma moeda ficam em cooldown de **4 horas** via Redis.

```
CoinGecko API → Análise de Mercado → Redis (deduplicação) → Gemini AI → Telegram
```

## Alertas suportados

| Tipo | Descrição | Threshold padrão |
|------|-----------|-----------------|
| `PUMP_24H` | Alta expressiva nas últimas 24h | ≥ +7% |
| `DUMP_24H` | Queda expressiva nas últimas 24h | ≤ -7% |
| `VOLATILITY_1H` | Alta volatilidade na última hora | ≥ 3% |

Os thresholds são configuráveis via `application.yaml`.

## Tecnologias

- **Java 25** + **Spring Boot 4.1.1**
- **Spring AI** — integração com Google Gemini (gemini-3.6-flash)
- **Spring Cloud OpenFeign** — cliente HTTP para a CoinGecko API
- **Redis** — deduplicação de alertas com TTL
- **Lombok**

## Configuração

Defina as seguintes variáveis de ambiente antes de subir a aplicação:

| Variável | Descrição |
|----------|-----------|
| `GEMINI_API_KEY` | Chave da API do Google Gemini |
| `API_KEY_COINGECKO` | Chave da API do CoinGecko |
| `TELEGRAM_BOT_TOKEN` | Token do bot do Telegram |
| `TELEGRAM_CHAT_ID` | ID do chat/canal que vai receber os alertas |
| `REDIS_HOST` | Host do Redis (padrão: `localhost`) |

### Parâmetros de mercado (`application.yaml`)

```yaml
market:
  top-coins: 20        # quantas moedas monitorar
  currency: brl        # moeda de referência
  thresholds:
    pump-24h: 7.0      # % de alta para disparar alerta
    dump-24h: -7.0     # % de queda para disparar alerta
    volatility-1h: 3.0 # % de variação em 1h para disparar alerta
```

## Como executar

Você precisará de uma instância do Redis rodando. Com Docker:

```bash
docker run -d -p 6379:6379 redis
```

Depois, suba a aplicação:

```bash
./gradlew bootRun
```
