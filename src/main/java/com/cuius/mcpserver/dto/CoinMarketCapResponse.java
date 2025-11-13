package com.cuius.mcpserver.dto;

import java.util.List;

public class CoinMarketCapResponse {
    private Status status;
    private List<CryptoCurrency> data;

    // Getters and Setters
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<CryptoCurrency> getData() {
        return data;
    }

    public void setData(List<CryptoCurrency> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CoinMarketCapResponse{" +
                "status=" + status +
                ", data size=" + (data != null ? data.size() : 0) +
                '}';
    }
}
