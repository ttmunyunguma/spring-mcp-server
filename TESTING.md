# CoinMarketCap MCP Server - Testing Guide

This guide explains how to set up and run tests for your Spring MCP Server project.

## Test Files Created

Two comprehensive test files have been created:

1. **CoinMarketCapToolServiceTest.java** - Unit tests for the tool service with mocking
2. **CoinMarketCapWebServiceTest.java** - Integration tests for the web service using MockWebServer

## Required Dependencies

Update your `build.gradle` file to include these test dependencies:

```gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-mcp-server'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
    
    // Test dependencies
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'com.squareup.okhttp3:mockwebserver3:5.3.0'  // For API mocking
    testImplementation 'org.assertj:assertj-core:3.25.1'            // For fluent assertions
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

## Test Coverage

### CoinMarketCapToolServiceTest

Unit tests covering all tool methods:

- **getLatestCryptoListings**
    - ✓ Successful fetch and cache
    - ✓ Null/invalid limit handling
    - ✓ Null response handling
    - ✓ Exception handling
    - ✓ Cache clearing on new fetch

- **getCachedCryptoCount**
    - ✓ Returns correct count
    - ✓ Handles empty cache

- **getCryptoBySymbol**
    - ✓ Successful retrieval
    - ✓ Case-insensitive search
    - ✓ Not found scenarios
    - ✓ Null/empty parameter validation

- **getTopCryptos**
    - ✓ Returns sorted cryptocurrencies
    - ✓ Respects limit parameter
    - ✓ Handles empty cache
    - ✓ Default count behavior

- **searchCryptoByName**
    - ✓ Partial name matching
    - ✓ Case-insensitive search
    - ✓ No matches scenario
    - ✓ Parameter validation

### CoinMarketCapWebServiceTest

Integration tests for web service:

- ✓ Successful API call with correct headers
- ✓ API error response handling
- ✓ Network timeout handling
- ✓ Malformed JSON handling
- ✓ Empty response handling
- ✓ Multiple cryptocurrencies parsing
- ✓ Server error handling
- ✓ Query parameter verification

## Running the Tests

### Run all tests
```bash
./gradlew test
```

### Run specific test class
```bash
./gradlew test --tests CoinMarketCapToolServiceTest
./gradlew test --tests CoinMarketCapWebServiceTest
```

### Run specific test method
```bash
./gradlew test --tests CoinMarketCapToolServiceTest.testGetLatestCryptoListings_Success
```

### Run tests with detailed output
```bash
./gradlew test --info
```

### Generate test report
```bash
./gradlew test
# Open build/reports/tests/test/index.html in your browser
```

## Test Structure

### Unit Tests (CoinMarketCapToolServiceTest)

Uses Mockito to mock the `CoinMarketCapWebService` dependency:

```java
@ExtendWith(MockitoExtension.class)
class CoinMarketCapToolServiceTest {
    @Mock
    private CoinMarketCapWebService webService;
    
    @InjectMocks
    private CoinMarketCapToolService toolService;
    
    @Test
    void testGetLatestCryptoListings_Success() {
        when(webService.getCoinMarketCapWebResponse(anyInt()))
            .thenReturn(mockResponse);
        // ... test logic
    }
}
```

### Integration Tests (CoinMarketCapWebServiceTest)

Uses MockWebServer to simulate real HTTP calls:

```java
import mockwebserver3.MockResponse;
import mockwebserver3.MockWebServer;
import mockwebserver3.RecordedRequest;

class CoinMarketCapWebServiceTest {
    private MockWebServer mockWebServer;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        // ... setup
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.close(); // Note: close() not shutdown() in v3
    }
    
    @Test
    void testGetCoinMarketCapWebResponse_Success() throws InterruptedException {
        // MockResponse uses Builder pattern in v3
        mockWebServer.enqueue(new MockResponse.Builder()
            .code(200)
            .body(jsonResponse)
            .addHeader("Content-Type", "application/json")
            .build());
        
        // RecordedRequest uses getRequestUrl() instead of getPath()
        RecordedRequest request = mockWebServer.takeRequest();
        assertThat(request.getRequestUrl().toString()).contains("/api/endpoint");
        assertThat(request.getHeader("Authorization")).isEqualTo("Bearer token");
    }
}
```

## Key Testing Patterns Used

### 1. Mockito for Unit Testing
- `@Mock` - Creates mock objects
- `@InjectMocks` - Injects mocks into the service under test
- `when().thenReturn()` - Defines mock behavior
- `verify()` - Verifies interactions

### 2. AssertJ for Fluent Assertions
```java
assertThat(result)
    .contains("Successfully fetched")
    .doesNotContain("Error");
```

### 3. JUnit 5 Features
- `@DisplayName` - Descriptive test names
- `@BeforeEach` - Setup before each test
- `@Test` - Marks test methods
- `@ExtendWith` - Extends with Mockito

### 4. ReflectionTestUtils for Private Fields
```java
ReflectionTestUtils.setField(toolService, "LISTINGS_DEFAULT_LIMIT", 10);
```

### 5. MockWebServer for HTTP Testing
- Simulates real HTTP responses
- Verifies request headers and parameters
- Tests error scenarios

## Test Data

Both test classes include helper methods to create mock data:

```java
private CryptoCurrency createCryptoCurrency(Long id, String name, 
                                           String symbol, Integer rank, 
                                           Double price) {
    // Creates fully populated cryptocurrency objects for testing
}
```

## Continuous Integration

These tests are ready for CI/CD pipelines:

### GitHub Actions Example
```yaml
- name: Run tests
  run: ./gradlew test
  
- name: Publish test results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: build/reports/tests/test/
```

### Jenkins Example
```groovy
stage('Test') {
    steps {
        sh './gradlew test'
    }
    post {
        always {
            junit 'build/test-results/test/*.xml'
        }
    }
}
```

## Best Practices Demonstrated

1. **Test Isolation** - Each test is independent
2. **Descriptive Names** - Tests clearly describe what they verify
3. **Arrange-Act-Assert** - Clear test structure
4. **Edge Cases** - Tests cover null, empty, and error scenarios
5. **Mock External Dependencies** - No real API calls in unit tests
6. **Integration Testing** - Verify HTTP communication separately

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Guide](https://assertj.github.io/doc/)
- [MockWebServer 3 Guide](https://github.com/square/okhttp/tree/master/mockwebserver)
- [Spring Boot Testing](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
