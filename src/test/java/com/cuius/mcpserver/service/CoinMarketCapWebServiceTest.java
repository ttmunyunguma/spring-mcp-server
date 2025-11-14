package com.cuius.mcpserver.service;

import com.cuius.mcpserver.dto.CoinMarketCapResponse;
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CoinMarketCapWebService Integration Tests")
class CoinMarketCapWebServiceTest {

    private MockWebServer mockWebServer;
    private CoinMarketCapWebService webService;
    private static final String TEST_API_KEY = "my-test-api-key";
    private static final String TEST_BASE_URL = "http://localhost:9187";
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(9187);

        String baseUrl = mockWebServer.url("/").toString();
        WebClient.Builder webClientBuilder = WebClient.builder();
        webService = new CoinMarketCapWebService(webClientBuilder, TEST_BASE_URL);

        // Set private fields using reflection
        ReflectionTestUtils.setField(webService, "apiKey", TEST_API_KEY);
        ReflectionTestUtils.setField(webService, "LISTINGS_ENDPOINT", "/v1/cryptocurrency/listings/latest");
    }

    @AfterEach
    void tearDown() {
        mockWebServer.close();
    }

    @Test
    @DisplayName("Should successfully fetch cryptocurrency data with correct headers")
    void testGetCoinMarketCapWebResponse_Success() throws InterruptedException {
        // Arrange
        String jsonResponse = """
                {
                    "status": {
                        "timestamp": "2024-01-15T10:30:00.000Z",
                        "error_code": 0,
                        "error_message": null,
                        "elapsed": 10,
                        "credit_count": 1
                    },
                    "data": [
                        {
                            "id": 1,
                            "name": "Bitcoin",
                            "symbol": "BTC",
                            "slug": "bitcoin",
                            "num_market_pairs": 500,
                            "date_added": "2013-04-28T00:00:00.000Z",
                            "tags": ["mineable"],
                            "max_supply": 21000000,
                            "circulating_supply": 19000000,
                            "total_supply": 19000000,
                            "cmc_rank": 1,
                            "last_updated": "2024-01-15T10:29:00.000Z",
                            "quote": {
                                "USD": {
                                    "price": 50000,
                                    "volume_24h": 30000000000,
                                    "volume_change_24h": 5.5,
                                    "percent_change_1h": 0.5,
                                    "percent_change_24h": 2.5,
                                    "percent_change_7d": 5.0,
                                    "percent_change_30d": 10.0,
                                    "market_cap": 950000000000,
                                    "market_cap_dominance": 45.5,
                                    "fully_diluted_market_cap": 1050000000000,
                                    "last_updated": "2024-01-15T10:29:00.000Z"
                                }
                            }
                        }
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body(jsonResponse)
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isNotNull();
        assertThat(response.getStatus().getErrorCode()).isEqualTo(0);
        assertThat(response.getData()).isNotNull();
        assertThat(response.getData()).hasSize(1);
        assertThat(response.getData().getFirst().getName()).isEqualTo("Bitcoin");
        assertThat(response.getData().getFirst().getSymbol()).isEqualTo("BTC");
        assertThat(response.getData().getFirst().getCmcRank()).isEqualTo(1);

        // Verify request
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getRequestLine()).contains("/v1/cryptocurrency/listings/latest");
        assertThat(recordedRequest.getRequestLine()).contains("limit=10");
        assertThat(recordedRequest.getRequestLine()).contains("convert=USD");
        assertThat(recordedRequest.getHeaders().get("X-CMC_PRO_API_KEY")).isEqualTo(TEST_API_KEY);
        assertThat(recordedRequest.getHeaders().get("Accept")).isEqualTo("application/json");
    }

    @Test
    @DisplayName("Should handle API error response gracefully")
    void testGetCoinMarketCapWebResponse_ApiError() {
        // Arrange
        String errorResponse = """
                {
                    "status": {
                        "timestamp": "2024-01-15T10:30:00.000Z",
                        "error_code": 401,
                        "error_message": "Invalid API Key",
                        "elapsed": 1,
                        "credit_count": 0
                    }
                }
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(401)
                .body(errorResponse)
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert - Should return null on error
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle network timeout gracefully")
    void testGetCoinMarketCapWebResponse_Timeout() {
        // Arrange
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body("{}")
                .headersDelay(5, TimeUnit.SECONDS)
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert - Should return null on timeout
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle malformed JSON response")
    void testGetCoinMarketCapWebResponse_MalformedJson() {
        // Arrange
        String malformedJson = "{ this is not valid json }";

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body(malformedJson)
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should handle empty response body")
    void testGetCoinMarketCapWebResponse_EmptyBody() {
        // Arrange
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body("")
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert
        assertThat(response).isNull();
    }

    @Test
    @DisplayName("Should correctly pass different limit parameters")
    void testGetCoinMarketCapWebResponse_DifferentLimits() throws InterruptedException {
        // Arrange
        String jsonResponse = """
                {
                    "status": {
                        "timestamp": "2024-01-15T10:30:00.000Z",
                        "error_code": 0
                    },
                    "data": []
                }
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body(jsonResponse)
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        webService.getCoinMarketCapWebResponse(50);

        // Assert
        RecordedRequest recordedRequest = mockWebServer.takeRequest();
        assertThat(recordedRequest.getRequestLine()).contains("limit=50");
    }

    @Test
    @DisplayName("Should handle response with multiple cryptocurrencies")
    void testGetCoinMarketCapWebResponse_MultipleCryptos() {
        // Arrange
        String jsonResponse = """
                {
                    "status": {
                        "timestamp": "2024-01-15T10:30:00.000Z",
                        "error_code": 0
                    },
                    "data": [
                        {
                            "id": 1,
                            "name": "Bitcoin",
                            "symbol": "BTC",
                            "slug": "bitcoin",
                            "cmc_rank": 1,
                            "circulating_supply": 19000000,
                            "total_supply": 19000000,
                            "max_supply": 21000000,
                            "quote": {
                                "USD": {
                                    "price": 50000,
                                    "market_cap": 950000000000,
                                    "volume_24h": 30000000000,
                                    "percent_change_24h": 2.5,
                                    "percent_change_7d": 5.0
                                }
                            }
                        },
                        {
                            "id": 2,
                            "name": "Ethereum",
                            "symbol": "ETH",
                            "slug": "ethereum",
                            "cmc_rank": 2,
                            "circulating_supply": 120000000,
                            "total_supply": 120000000,
                            "max_supply": null,
                            "quote": {
                                "USD": {
                                    "price": 3000,
                                    "market_cap": 360000000000,
                                    "volume_24h": 15000000000,
                                    "percent_change_24h": 1.5,
                                    "percent_change_7d": 3.0
                                }
                            }
                        }
                    ]
                }
                """;

        mockWebServer.enqueue(new MockResponse.Builder()
                .code(200)
                .body(jsonResponse)
                .addHeader("Content-Type", "application/json")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getData()).hasSize(2);
        assertThat(response.getData().get(0).getName()).isEqualTo("Bitcoin");
        assertThat(response.getData().get(1).getName()).isEqualTo("Ethereum");
        assertThat(response.getData().get(0).getQuote().get("USD").getPrice()).isEqualTo(50000.0);
        assertThat(response.getData().get(1).getQuote().get("USD").getPrice()).isEqualTo(3000.0);
    }

    @Test
    @DisplayName("Should handle server error (500)")
    void testGetCoinMarketCapWebResponse_ServerError() {
        // Arrange
        mockWebServer.enqueue(new MockResponse.Builder()
                .code(500)
                .body("Internal Server Error")
                .build());

        // Act
        CoinMarketCapResponse response = webService.getCoinMarketCapWebResponse(10);

        // Assert
        assertThat(response).isNull();
    }
}
