package com.cuius.mcpserver;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.logging.Logger;

@SpringBootApplication
public class SpringMcpServerApplication {
    public static final Logger logger = Logger.getLogger(SpringMcpServerApplication.class.getName());

    public static void main(String[] args) {
        SpringApplication.run(SpringMcpServerApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider weatherTools(WeatherService weatherService){
        return MethodToolCallbackProvider.builder().toolObjects(weatherService).build();
    }

    @Bean
    public ToolCallbackProvider coinMarketCapTools(CoinMarketCapService coinMarketCapService){
        return MethodToolCallbackProvider.builder().toolObjects(coinMarketCapService).build();
    }
}
