package com.cuius.mcpserver;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class WeatherService {

    @Tool(name = "getWeather", description = "gets the current weather details")
    public String getWeather(String city){
        //TODO implementation of calling the model
        return city;
    }
}
