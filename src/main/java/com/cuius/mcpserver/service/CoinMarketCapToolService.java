package com.cuius.mcpserver.service;

import com.cuius.mcpserver.dto.CoinMarketCapResponse;
import com.cuius.mcpserver.dto.CryptoCurrency;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

@Service
public class CoinMarketCapToolService {
    private static final Logger logger = Logger.getLogger(CoinMarketCapToolService.class.getName());

    private final List<CryptoCurrency> cachedCryptocurrencies;
    private final CoinMarketCapWebService webService;

    @Value("${coinmarketcap.api.listings.default-limit}")
    private Integer LISTINGS_DEFAULT_LIMIT;

    public CoinMarketCapToolService(CoinMarketCapWebService webService) {
        this.cachedCryptocurrencies = new ArrayList<>();
        this.webService = webService;
    }

    @Tool(name = "getLatestCryptoListings", description = "Fetches the latest cryptocurrency listings from CoinMarketCap API and stores them in memory")
    public String getLatestCryptoListings(@ToolParam(required = false, description = "max  number of listings") Integer limit) {
        try {
            logger.info("Fetching latest cryptocurrency listings with limit: " + limit);

            if (limit == null || limit <= 0) {
                limit = LISTINGS_DEFAULT_LIMIT;
            }
            CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(limit);

            if (response != null && response.getData() != null) {
                cachedCryptocurrencies.clear();
                cachedCryptocurrencies.addAll(response.getData());

                logger.info("Successfully fetched and cached " + cachedCryptocurrencies.size() + " cryptocurrencies");

                return "Successfully fetched " + cachedCryptocurrencies.size() + " cryptocurrencies. " +
                       "Total in cache: " + getCachedCount();
            } else {
                return "Failed to fetch cryptocurrency data. Please check your configuration.";
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
    public String getCryptoBySymbol(@ToolParam(required = true, description = "the symbol to use for searching crypto") String symbol) {
        logger.info("Searching for cryptocurrency with symbol: " + symbol);

        if (symbol == null || symbol.trim().isEmpty()) {
            return "Please provide a valid cryptocurrency symbol";
        }

        return cachedCryptocurrencies.stream()
                .filter(crypto -> crypto.getSymbol().equalsIgnoreCase(symbol.trim()))
                .findFirst()
                .map(this::formatCryptoInfo)
                .orElse("Cryptocurrency with symbol '" + symbol + "' not found in cache");
    }

    @Tool(name = "getTopCryptos", description = "Returns the top N cryptocurrencies by market cap rank from the cache")
    public String getTopCryptos(@ToolParam(required = false, description = "number of listings to return")Integer count) {
        if (count == null || count <= 0) {
            count = LISTINGS_DEFAULT_LIMIT;
        }

        logger.info("Getting top " + count + " cryptocurrencies");

        if (cachedCryptocurrencies.isEmpty()) {
            return "No cryptocurrencies in cache. Please fetch data first using getLatestCryptoListings.";
        }

        List<CryptoCurrency> topCryptos = cachedCryptocurrencies.stream()
                .sorted(Comparator.comparingInt(c -> c.getCmcRank() != null ? c.getCmcRank() : Integer.MAX_VALUE))
                .limit(count)
                .toList();

        StringBuilder result = new StringBuilder("Top " + topCryptos.size() + " Cryptocurrencies:\n");
        for (CryptoCurrency crypto : topCryptos) {
            result.append(formatCryptoSummary(crypto)).append("\n");
        }

        return result.toString();
    }

    @Tool(name = "searchCryptoByName", description = "Searches for cryptocurrencies in the cache by name (case-insensitive partial match)")
    public String searchCryptoByName(@ToolParam(description = "name of crypto to search match") String name) {
        logger.info("Searching for cryptocurrencies with name containing: " + name);

        if (name == null || name.trim().isEmpty()) {
            return "Please provide a valid cryptocurrency name to search";
        }

        List<CryptoCurrency> matches = cachedCryptocurrencies.stream()
                .filter(crypto -> crypto.getName().toLowerCase().contains(name.toLowerCase().trim()))
                .limit(LISTINGS_DEFAULT_LIMIT)
                .toList();

        if (matches.isEmpty()) {
            return "No cryptocurrencies found matching '" + name + "'";
        }

        StringBuilder result = new StringBuilder("Found " + matches.size() + " cryptocurrency(ies) matching '" + name + "':\n");
        for (CryptoCurrency crypto : matches) {
            result.append(formatCryptoSummary(crypto)).append("\n");
        }

        return result.toString();
    }

    private int getCachedCount() {
        return cachedCryptocurrencies.size();
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
