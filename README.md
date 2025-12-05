# CoinMarketCap MCP Server

A Spring Boot-based Model Context Protocol (MCP) server that provides cryptocurrency data tools powered by the CoinMarketCap API. This server exposes five powerful tools for fetching, caching, and querying cryptocurrency information.

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.3-blue.svg)](https://spring.io/projects/spring-ai)

## 📋 Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Available Tools](#available-tools)
- [Usage Examples](#usage-examples)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [API Integration](#api-integration)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

## ✨ Features

- **5 MCP Tools** for cryptocurrency data operations
- **In-memory caching** for fast data access
- **Spring AI integration** for seamless MCP protocol support
- **Reactive WebClient** for efficient API calls
- **Comprehensive test coverage** with unit and integration tests
- **Type-safe DTOs** with Jackson serialization
- **Configurable API settings** via application properties

## 🏗️ Architecture

The project follows a clean architecture pattern with the following layers:

```
┌─────────────────────────────────────────┐
│   MCP Protocol (Spring AI)              │
├─────────────────────────────────────────┤
│   Tool Service Layer                    │
│   - CoinMarketCapToolService (@Tool)    │
├─────────────────────────────────────────┤
│   Web Service Layer                     │
│   - CoinMarketCapWebService (WebClient) │
├─────────────────────────────────────────┤
│   DTOs                                  │
│   - CoinMarketCapResponse               │
│   - CryptoCurrency, Quote, Status       │
├─────────────────────────────────────────┤
│   External API                          │
│   - CoinMarketCap Pro API               │
└─────────────────────────────────────────┘
```

### Key Components

- **SpringMcpServerApplication**: Main application class with tool registration
- **CoinMarketCapToolService**: Exposes 5 MCP tools with in-memory caching
- **CoinMarketCapWebService**: Handles HTTP communication with CoinMarketCap API
- **DTOs**: Type-safe data transfer objects for API responses

## 📦 Prerequisites

- **Java 21** or higher
- **Gradle 8.x** (wrapper included)
- **CoinMarketCap API Key** ([Get one here](https://pro.coinmarketcap.com/signup))
- **Internet connection** for API calls

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/ttmunyunguma/spring-mcp-server.git
cd spring-mcp-server
```

### 2. Configure API Key

Edit `src/main/resources/application.properties`:

```properties
coinmarketcap.api.key=YOUR_API_KEY_HERE
```

### 3. Build the Project

```bash
./gradlew build
```

## ⚙️ Configuration

### Application Properties

Located in `src/main/resources/application.properties`:

```properties
# MCP Server Configuration
spring.ai.mcp.server.name=coinmarketcap-mcp-server
spring.ai.mcp.server.type=sync
spring.ai.mcp.server.version=1.0.0

# Spring Boot Settings
spring.main.web-application-type=none
spring.main.banner-mode=off

# CoinMarketCap API Configuration
coinmarketcap.api.key=your-api-key-here
coinmarketcap.api.base-url=https://pro-api.coinmarketcap.com
coinmarketcap.api.listings=/v1/cryptocurrency/listings/latest
coinmarketcap.api.listings.default-limit=10
```

### Environment Variables

Alternatively, configure via environment variables:

```bash
export COINMARKETCAP_API_KEY=your-api-key-here
export COINMARKETCAP_API_BASE_URL=https://pro-api.coinmarketcap.com
export COINMARKETCAP_API_LISTINGS_DEFAULT_LIMIT=10
```

## 🛠️ Available Tools

The server exposes 5 tools via the MCP protocol:

### 1. `getLatestCryptoListings`

Fetches the latest cryptocurrency listings from CoinMarketCap and stores them in memory.

**Parameters:**
- `limit` (optional): Maximum number of listings to fetch (default: 10)

**Returns:** Success message with count of fetched cryptocurrencies

**Example:**
```
getLatestCryptoListings(limit: 50)
→ "Successfully fetched 50 cryptocurrencies. Total in cache: 50"
```

### 2. `getCachedCryptoCount`

Returns the number of cryptocurrencies currently cached in memory.

**Parameters:** None

**Returns:** Count of cached cryptocurrencies

**Example:**
```
getCachedCryptoCount()
→ "Currently caching 50 cryptocurrencies"
```

### 3. `getCryptoBySymbol`

Retrieves detailed information about a specific cryptocurrency by its symbol.

**Parameters:**
- `symbol` (required): The cryptocurrency symbol (e.g., "BTC", "ETH")

**Returns:** Detailed cryptocurrency information including price, market cap, supply, and change percentages

**Example:**
```
getCryptoBySymbol(symbol: "BTC")
→ 
=== Bitcoin (BTC) ===
Rank: #1
Circulating Supply: 19000000.0
Total Supply: 19000000.0
Max Supply: 21000000.0
Price (USD): $50000.00
Market Cap (USD): $950000000000.00
24h Volume: $30000000000.00
24h Change: 2.50%
7d Change: 5.00%
```

### 4. `getTopCryptos`

Returns the top N cryptocurrencies by market cap rank from the cache.

**Parameters:**
- `count` (optional): Number of cryptocurrencies to return (default: 10)

**Returns:** List of top cryptocurrencies with summary information

**Example:**
```
getTopCryptos(count: 3)
→ 
Top 3 Cryptocurrencies:
#1 Bitcoin (BTC) - $50000.00 (24h: 2.50%)
#2 Ethereum (ETH) - $3000.00 (24h: 1.50%)
#3 Tether (USDT) - $1.00 (24h: 0.01%)
```

### 5. `searchCryptoByName`

Searches for cryptocurrencies by name (case-insensitive partial match).

**Parameters:**
- `name` (required): The name or partial name to search for

**Returns:** List of matching cryptocurrencies

**Example:**
```
searchCryptoByName(name: "bit")
→ 
Found 1 cryptocurrency(ies) matching 'bit':
#1 Bitcoin (BTC) - $50000.00 (24h: 2.50%)
```

## 💡 Usage Examples

### Basic Workflow

1. **Fetch latest data:**
   ```
   getLatestCryptoListings(limit: 100)
   ```

2. **Check cache status:**
   ```
   getCachedCryptoCount()
   ```

3. **Query specific cryptocurrency:**
   ```
   getCryptoBySymbol(symbol: "ETH")
   ```

4. **Get top performers:**
   ```
   getTopCryptos(count: 10)
   ```

5. **Search by name:**
   ```
   searchCryptoByName(name: "cardano")
   ```

### Integration with MCP Clients

This server can be integrated with any MCP-compatible client. Here's how to configure it in Claude Desktop:

```json
{
  "mcpServers": {
    "coinmarketcap": {
      "command": "java",
      "args": [
        "-jar",
        "/path/to/spring-mcp-server-0.0.1.jar"
      ],
      "env": {
        "COINMARKETCAP_API_KEY": "your-api-key-here"
      }
    }
  }
}
```

## 🧪 Testing

The project includes comprehensive test coverage with both unit and integration tests.

### Run All Tests

```bash
./gradlew allTests
```

### Run Unit Tests

```bash
./gradlew test
```

### Run Integration Tests

```bash
./gradlew integrationTest 
```


### View Test Reports

After running tests, open the HTML report:

```bash
open build/reports/tests/test/index.html
```

### Test Coverage

- **CoinMarketCapToolServiceTest**: 19 unit tests covering all tool methods
- **CoinMarketCapWebServiceTest**: 9 integration tests for HTTP communication
- **Total Coverage**: 28 tests with comprehensive edge case handling

For detailed testing documentation, see [TESTING.md](TESTING.md).

## 📁 Project Structure

```
spring-mcp-server/
├── src/
│   ├── main/
│   │   ├── java/com/cuius/mcpserver/
│   │   │   ├── SpringMcpServerApplication.java    # Main application
│   │   │   ├── dto/
│   │   │   │   ├── CoinMarketCapResponse.java     # API response wrapper
│   │   │   │   ├── CryptoCurrency.java            # Cryptocurrency entity
│   │   │   │   ├── Quote.java                     # Price/market data
│   │   │   │   └── Status.java                    # API status info
│   │   │   └── service/
│   │   │       ├── CoinMarketCapToolService.java  # MCP tools
│   │   │       └── CoinMarketCapWebService.java   # HTTP client
│   │   └── resources/
│   │       └── application.properties              # Configuration
│   └── test/
│       ├── java/com/cuius/mcpserver/
│       │   ├── service/
│       │   │   ├── CoinMarketCapToolServiceTest.java   # Unit tests
│       │   │   └── CoinMarketCapWebServiceTest.java    # Integration tests
│       │   └── SpringMcpServerApplicationTests.java    # App tests
│       └── resources/
│           └── application.properties                   # Test config
├── build.gradle                                     # Build configuration
├── settings.gradle                                  # Project settings
├── TESTING.md                                       # Testing guide
└── README.md                                        # This file
```

## 🔌 API Integration

### CoinMarketCap API

This server integrates with the [CoinMarketCap Professional API](https://pro.coinmarketcap.com/):

- **Endpoint**: `/v1/cryptocurrency/listings/latest`
- **Authentication**: API key via `X-CMC_PRO_API_KEY` header
- **Rate Limits**: Depends on your subscription tier
- **Response Format**: JSON

### API Response Example

```json
{
  "status": {
    "timestamp": "2024-01-15T10:30:00.000Z",
    "error_code": 0,
    "elapsed": 10,
    "credit_count": 1
  },
  "data": [
    {
      "id": 1,
      "name": "Bitcoin",
      "symbol": "BTC",
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
    }
  ]
}
```

## 🔧 Troubleshooting

#### API Key Issues

**Problem**: 401 Unauthorized error

**Solution**:
1. Verify your API key is correct in `application.properties`
2. Check your CoinMarketCap account status
3. Ensure you haven't exceeded rate limits

#### Empty Cache

**Problem**: Tools return "No cryptocurrencies in cache"

**Solution**: Call `getLatestCryptoListings` first to populate the cache:
```
getLatestCryptoListings(limit: 100)
```

### Debug Mode

Enable debug logging in `application.properties`:

```properties
logging.level.com.cuius.mcpserver=DEBUG
logging.level.org.springframework.web.reactive=DEBUG
```

## 🙏 Acknowledgments

- [Spring Boot](https://spring.io/projects/spring-boot) - Application framework
- [Spring AI](https://spring.io/projects/spring-ai) - MCP protocol support
- [CoinMarketCap](https://coinmarketcap.com/) - Cryptocurrency data provider
- [Project Reactor](https://projectreactor.io/) - Reactive programming support

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/ttmunyunguma/spring-mcp-server/issues)
- **Documentation**: [Project Wiki](https://github.com/ttmunyunguma/spring-mcp-server/wiki)
- **CoinMarketCap API**: [API Documentation](https://coinmarketcap.com/api/documentation/v1/)

---

**Built using Spring Boot and Spring AI**
