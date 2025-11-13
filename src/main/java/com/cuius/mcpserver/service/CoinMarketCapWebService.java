package com.cuius.mcpserver.service;

import com.cuius.mcpserver.dto.CoinMarketCapResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;

@Service
public class CoinMarketCapWebService {
    private static final Logger logger = Logger.getLogger(CoinMarketCapWebService.class.getName());
    private final WebClient webClient;

    @Value("${coinmarketcap.api.key:your-api-key-here}")
    private String apiKey;
    @Value("${coinmarketcap.api.base-url}")
    private String BASE_URL;
    @Value("${coinmarketcap.api.listings}")
    private String LISTINGS_ENDPOINT;

    public CoinMarketCapWebService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
    }

    public CoinMarketCapResponse getCoinMarketCapWebResponse(Integer finalLimit) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(LISTINGS_ENDPOINT)
                        .queryParam("limit", finalLimit)
                        .queryParam("convert", "USD")
                        .build())
                .header("X-CMC_PRO_API_KEY", apiKey)
                .header("Accept", "application/json")
                .retrieve()
                .bodyToMono(CoinMarketCapResponse.class)
                .onErrorResume(e -> {
                    logger.severe("Error fetching cryptocurrency data: " + e.getMessage());
                    return Mono.empty();
                })
                .block();
    }
}
