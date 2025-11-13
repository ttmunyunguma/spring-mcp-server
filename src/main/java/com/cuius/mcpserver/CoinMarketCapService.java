package com.cuius.mcpserver;

import com.cuius.mcpserver.dto.CoinMarketCapResponse;
import com.cuius.mcpserver.dto.CryptoCurrency;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class CoinMarketCapService {
    private static final Logger logger = Logger.getLogger(CoinMarketCapService.class.getName());
    private static final String BASE_URL = "https://pro-api.coinmarketcap.com";
    private static final String LISTINGS_ENDPOINT = "/v1/cryptocurrency/listings/latest";

    private final WebClient webClient;
    private final List<CryptoCurrency> cachedCryptocurrencies;

    @Value("${coinmarketcap.api.key:your-api-key-here}")
    private String apiKey;

    public CoinMarketCapService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl(BASE_URL).build();
        this.cachedCryptocurrencies = new ArrayList<>();
    }

    @Tool(name = "getLatestCryptoListings", description = "Fetches the latest cryptocurrency listings from CoinMarketCap API and stores them in memory")
    public String getLatestCryptoListings(Integer limit) {
        try {
            logger.info("Fetching latest cryptocurrency listings with limit: " + limit);

            if (limit == null || limit <= 0) {
                limit = 10; // Default to 10
            }

            CoinMarketCapResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(LISTINGS_ENDPOINT)
                            .queryParam("limit", limit)
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

            if (response != null && response.getData() != null) {
                // Clear and update cached data
                cachedCryptocurrencies.clear();
                cachedCryptocurrencies.addAll(response.getData());

                logger.info("Successfully fetched and cached " + cachedCryptocurrencies.size() + " cryptocurrencies");

                return "Successfully fetched " + cachedCryptocurrencies.size() + " cryptocurrencies. " +
                       "Total in cache: " + getCachedCount();
            } else {
                return "Failed to fetch cryptocurrency data. Please check your API key and configuration.";
            }
        } catch (Exception e) {
            logger.severe("Exception in getLatestCryptoListings: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    @Tool(name = "getCachedCryptoCount", description = "Returns the number of cryptocurrencies currently cached in memory")
    public String getCachedCryptoCount() {
        int count = cachedCryptocurrencies.size();
        logger.info("Current cached cryptocurrency count: " + count);
        return "Currently caching " + count + " cryptocurrencies";
    }

    @Tool(name = "getCryptoBySymbol", description = "Retrieves a specific cryptocurrency from the cache by its symbol (e.g., BTC, ETH)")
    public String getCryptoBySymbol(String symbol) {
        logger.info("Searching for cryptocurrency with symbol: " + symbol);

        if (symbol == null || symbol.trim().isEmpty()) {
            return "Please provide a valid cryptocurrency symbol";
        }

        return cachedCryptocurrencies.stream()
                .filter(crypto -> crypto.getSymbol().equalsIgnoreCase(symbol.trim()))
                .findFirst()
                .map(crypto -> formatCryptoInfo(crypto))
                .orElse("Cryptocurrency with symbol '" + symbol + "' not found in cache");
    }

    @Tool(name = "getTopCryptos", description = "Returns the top N cryptocurrencies by market cap rank from the cache")
    public String getTopCryptos(Integer count) {
        if (count == null || count <= 0) {
            count = 10;
        }

        logger.info("Getting top " + count + " cryptocurrencies");

        if (cachedCryptocurrencies.isEmpty()) {
            return "No cryptocurrencies in cache. Please fetch data first using getLatestCryptoListings.";
        }

        List<CryptoCurrency> topCryptos = cachedCryptocurrencies.stream()
                .sorted((c1, c2) -> Integer.compare(
                        c1.getCmcRank() != null ? c1.getCmcRank() : Integer.MAX_VALUE,
                        c2.getCmcRank() != null ? c2.getCmcRank() : Integer.MAX_VALUE
                ))
                .limit(count)
                .collect(Collectors.toList());

        StringBuilder result = new StringBuilder("Top " + topCryptos.size() + " Cryptocurrencies:\n");
        for (CryptoCurrency crypto : topCryptos) {
            result.append(formatCryptoSummary(crypto)).append("\n");
        }

        return result.toString();
    }

    @Tool(name = "searchCryptoByName", description = "Searches for cryptocurrencies in the cache by name (case-insensitive partial match)")
    public String searchCryptoByName(String name) {
        logger.info("Searching for cryptocurrencies with name containing: " + name);

        if (name == null || name.trim().isEmpty()) {
            return "Please provide a valid cryptocurrency name to search";
        }

        List<CryptoCurrency> matches = cachedCryptocurrencies.stream()
                .filter(crypto -> crypto.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .limit(10) // Limit to 10 results
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            return "No cryptocurrencies found matching '" + name + "'";
        }

        StringBuilder result = new StringBuilder("Found " + matches.size() + " cryptocurrency(ies) matching '" + name + "':\n");
        for (CryptoCurrency crypto : matches) {
            result.append(formatCryptoSummary(crypto)).append("\n");
        }

        return result.toString();
    }

    // Helper methods
    private int getCachedCount() {
        return cachedCryptocurrencies.size();
    }

    public List<CryptoCurrency> getCachedCryptocurrencies() {
        return new ArrayList<>(cachedCryptocurrencies);
    }

    private String formatCryptoInfo(CryptoCurrency crypto) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ").append(crypto.getName()).append(" (").append(crypto.getSymbol()).append(") ===\n");
        sb.append("Rank: #").append(crypto.getCmcRank()).append("\n");
        sb.append("Circulating Supply: ").append(crypto.getCirculatingSupply()).append("\n");
        sb.append("Total Supply: ").append(crypto.getTotalSupply()).append("\n");
        sb.append("Max Supply: ").append(crypto.getMaxSupply()).append("\n");

        if (crypto.getQuote() != null && crypto.getQuote().containsKey("USD")) {
            var usdQuote = crypto.getQuote().get("USD");
            sb.append("Price (USD): $").append(String.format("%.2f", usdQuote.getPrice())).append("\n");
            sb.append("Market Cap (USD): $").append(String.format("%.2f", usdQuote.getMarketCap())).append("\n");
            sb.append("24h Volume: $").append(String.format("%.2f", usdQuote.getVolume24h())).append("\n");
            sb.append("24h Change: ").append(String.format("%.2f", usdQuote.getPercentChange24h())).append("%\n");
            sb.append("7d Change: ").append(String.format("%.2f", usdQuote.getPercentChange7d())).append("%\n");
        }

        return sb.toString();
    }

    private String formatCryptoSummary(CryptoCurrency crypto) {
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(crypto.getCmcRank()).append(" ");
        sb.append(crypto.getName()).append(" (").append(crypto.getSymbol()).append(")");

        if (crypto.getQuote() != null && crypto.getQuote().containsKey("USD")) {
            var usdQuote = crypto.getQuote().get("USD");
            sb.append(" - $").append(String.format("%.2f", usdQuote.getPrice()));
            sb.append(" (24h: ").append(String.format("%.2f", usdQuote.getPercentChange24h())).append("%)");
        }

        return sb.toString();
    }
}
