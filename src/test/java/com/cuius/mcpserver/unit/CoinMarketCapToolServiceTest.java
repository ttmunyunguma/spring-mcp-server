package com.cuius.mcpserver.unit;

import com.cuius.mcpserver.dto.CoinMarketCapResponse;
import com.cuius.mcpserver.dto.CryptoCurrency;
import com.cuius.mcpserver.dto.Quote;
import com.cuius.mcpserver.dto.Status;
import com.cuius.mcpserver.service.CoinMarketCapToolService;
import com.cuius.mcpserver.service.CoinMarketCapWebService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
@DisplayName("CoinMarketCapToolService Tests")
class CoinMarketCapToolServiceTest {

    @Mock
    private CoinMarketCapWebService webService;

    @InjectMocks
    private CoinMarketCapToolService toolService;

    private CoinMarketCapResponse mockResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(toolService, "LISTINGS_DEFAULT_LIMIT", 10);

        List<CryptoCurrency> mockCryptoList = createMockCryptocurrencies();
        mockResponse = createMockResponse(mockCryptoList);
    }

    @Test
    @DisplayName("Should fetch and cache cryptocurrency listings successfully")
    void testGetLatestCryptoListings_Success() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);

        // Act
        String result = toolService.getLatestCryptoListings(5);

        // Assert
        assertThat(result).contains("Successfully fetched 3 cryptocurrencies");
        assertThat(result).contains("Total in cache: 3");
        verify(webService, times(1)).getCoinMarketCapWebResponse(5);
    }

    @Test
    @DisplayName("Should use default limit when limit is null")
    void testGetLatestCryptoListings_NullLimit() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(10)).thenReturn(mockResponse);

        // Act
        String result = toolService.getLatestCryptoListings(null);

        // Assert
        assertThat(result).contains("Successfully fetched");
        verify(webService, times(1)).getCoinMarketCapWebResponse(10);
    }

    @Test
    @DisplayName("Should use default limit when limit is zero or negative")
    void testGetLatestCryptoListings_InvalidLimit() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(10)).thenReturn(mockResponse);

        // Act
        String result = toolService.getLatestCryptoListings(0);

        // Assert
        assertThat(result).contains("Successfully fetched");
        verify(webService, times(1)).getCoinMarketCapWebResponse(10);
    }

    @Test
    @DisplayName("Should return error message when web service returns null")
    void testGetLatestCryptoListings_NullResponse() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(null);

        // Act
        String result = toolService.getLatestCryptoListings(5);

        // Assert
        assertThat(result).contains("Failed to fetch cryptocurrency data");
    }

    @Test
    @DisplayName("Should handle exception from web service")
    void testGetLatestCryptoListings_ExceptionHandling() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt()))
                .thenThrow(new RuntimeException("API Connection Failed"));

        // Act
        String result = toolService.getLatestCryptoListings(5);

        // Assert
        assertThat(result).contains("Error: API Connection Failed");
    }

    @Test
    @DisplayName("Should return cached cryptocurrency count")
    void testGetCachedCryptoCount() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getCachedCryptoCount();

        // Assert
        assertThat(result).isEqualTo("Currently caching 3 cryptocurrencies");
    }

    @Test
    @DisplayName("Should return zero count when cache is empty")
    void testGetCachedCryptoCount_EmptyCache() {
        // Act
        String result = toolService.getCachedCryptoCount();

        // Assert
        assertThat(result).isEqualTo("Currently caching 0 cryptocurrencies");
    }

    @Test
    @DisplayName("Should retrieve cryptocurrency by symbol successfully")
    void testGetCryptoBySymbol_Success() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getCryptoBySymbol("BTC");

        // Assert
        assertThat(result).contains("Bitcoin (BTC)");
        assertThat(result).contains("Rank: #1");
        assertThat(result).contains("Price (USD): $50000.00");
    }

    @Test
    @DisplayName("Should handle case-insensitive symbol search")
    void testGetCryptoBySymbol_CaseInsensitive() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getCryptoBySymbol("btc");

        // Assert
        assertThat(result).contains("Bitcoin (BTC)");
    }

    @Test
    @DisplayName("Should return not found message for non-existent symbol")
    void testGetCryptoBySymbol_NotFound() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getCryptoBySymbol("XYZ");

        // Assert
        assertThat(result).contains("Cryptocurrency with symbol 'XYZ' not found in cache");
    }

    @Test
    @DisplayName("Should validate symbol parameter")
    void testGetCryptoBySymbol_NullOrEmpty() {
        // Act & Assert - null symbol
        String resultNull = toolService.getCryptoBySymbol(null);
        assertThat(resultNull).contains("Please provide a valid cryptocurrency symbol");

        // Act & Assert - empty symbol
        String resultEmpty = toolService.getCryptoBySymbol("   ");
        assertThat(resultEmpty).contains("Please provide a valid cryptocurrency symbol");
    }

    @Test
    @DisplayName("Should return top cryptocurrencies sorted by rank")
    void testGetTopCryptos_Success() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getTopCryptos(2);

        // Assert
        assertThat(result).contains("Top 2 Cryptocurrencies:");
        assertThat(result).contains("#1 Bitcoin (BTC)");
        assertThat(result).contains("#2 Ethereum (ETH)");
        assertThat(result).doesNotContain("#3 Cardano (ADA)");
    }

    @Test
    @DisplayName("Should use default count when count is null or invalid")
    void testGetTopCryptos_DefaultCount() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.getTopCryptos(null);

        // Assert
        assertThat(result).contains("Top 3 Cryptocurrencies:");
    }

    @Test
    @DisplayName("Should handle empty cache when getting top cryptos")
    void testGetTopCryptos_EmptyCache() {
        // Act
        String result = toolService.getTopCryptos(5);

        // Assert
        assertThat(result).contains("No cryptocurrencies in cache");
        assertThat(result).contains("Please fetch data first using getLatestCryptoListings");
    }

    @Test
    @DisplayName("Should search cryptocurrencies by name")
    void testSearchCryptoByName_Success() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.searchCryptoByName("Bit");

        // Assert
        assertThat(result).contains("Found 1 cryptocurrency(ies) matching 'Bit'");
        assertThat(result).contains("Bitcoin (BTC)");
    }

    @Test
    @DisplayName("Should handle case-insensitive name search")
    void testSearchCryptoByName_CaseInsensitive() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.searchCryptoByName("ethereum");

        // Assert
        assertThat(result).contains("Ethereum (ETH)");
    }

    @Test
    @DisplayName("Should return not found message when no matches")
    void testSearchCryptoByName_NoMatches() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);
        toolService.getLatestCryptoListings(5);

        // Act
        String result = toolService.searchCryptoByName("NonExistent");

        // Assert
        assertThat(result).contains("No cryptocurrencies found matching 'NonExistent'");
    }

    @Test
    @DisplayName("Should validate name parameter")
    void testSearchCryptoByName_NullOrEmpty() {
        // Act & Assert - null name
        String resultNull = toolService.searchCryptoByName(null);
        assertThat(resultNull).contains("Please provide a valid cryptocurrency name to search");

        // Act & Assert - empty name
        String resultEmpty = toolService.searchCryptoByName("   ");
        assertThat(resultEmpty).contains("Please provide a valid cryptocurrency name to search");
    }

    @Test
    @DisplayName("Should clear cache when fetching new listings")
    void testGetLatestCryptoListings_ClearsPreviousCache() {
        // Arrange
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(mockResponse);

        // Act - First fetch
        toolService.getLatestCryptoListings(5);
        String firstCount = toolService.getCachedCryptoCount();

        // Create new mock data with different size
        List<CryptoCurrency> newMockList = Arrays.asList(
                createCryptoCurrency(1L, "Bitcoin", "BTC", 1, 50000.0),
                createCryptoCurrency(2L, "Ethereum", "ETH", 2, 3000.0)
        );
        CoinMarketCapResponse newResponse = createMockResponse(newMockList);
        when(webService.getCoinMarketCapWebResponse(anyInt())).thenReturn(newResponse);

        // Act - Second fetch
        toolService.getLatestCryptoListings(5);
        String secondCount = toolService.getCachedCryptoCount();

        // Assert
        assertThat(firstCount).isEqualTo("Currently caching 3 cryptocurrencies");
        assertThat(secondCount).isEqualTo("Currently caching 2 cryptocurrencies");
    }

    private List<CryptoCurrency> createMockCryptocurrencies() {
        return Arrays.asList(
                createCryptoCurrency(1L, "Bitcoin", "BTC", 1, 50000.0),
                createCryptoCurrency(2L, "Ethereum", "ETH", 2, 3000.0),
                createCryptoCurrency(3L, "Cardano", "ADA", 3, 1.5)
        );
    }

    private CryptoCurrency createCryptoCurrency(Long id, String name, String symbol,
                                                Integer rank, Double price) {
        CryptoCurrency crypto = new CryptoCurrency();
        crypto.setId(id);
        crypto.setName(name);
        crypto.setSymbol(symbol);
        crypto.setCmcRank(rank);
        crypto.setCirculatingSupply(19000000.0);
        crypto.setTotalSupply(21000000.0);
        crypto.setMaxSupply(21000000.0);

        Quote usdQuote = new Quote();
        usdQuote.setPrice(price);
        usdQuote.setMarketCap(price * 19000000.0);
        usdQuote.setVolume24h(1000000000.0);
        usdQuote.setPercentChange24h(2.5);
        usdQuote.setPercentChange7d(5.0);
        usdQuote.setLastUpdated(LocalDateTime.now());

        Map<String, Quote> quoteMap = new HashMap<>();
        quoteMap.put("USD", usdQuote);
        crypto.setQuote(quoteMap);

        return crypto;
    }

    private CoinMarketCapResponse createMockResponse(List<CryptoCurrency> cryptoList) {
        CoinMarketCapResponse response = new CoinMarketCapResponse();

        Status status = new Status();
        status.setTimestamp(LocalDateTime.now());
        status.setErrorCode(0);
        status.setErrorMessage(null);

        response.setStatus(status);
        response.setData(cryptoList);

        return response;
    }
}
