package com.example.pfm_dashboard.config;

import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;

@Configuration
public class PlaidConfig {

    // Hardcoded for testing. Ideally, replace these with environment variables
    private final String clientId = "67310306fda72b001a3538ba";
    private final String secret = "5da0805774a54d79061e4f8365dfeb";
    public String plaidEnv = ApiClient.Sandbox;


    @Bean
    public PlaidApi plaidClient() {
        // Manually set API keys
        HashMap<String, String> apiKeys = new HashMap<>();
        apiKeys.put("clientId", clientId);
        apiKeys.put("secret", secret);
        apiKeys.put("plaidVersion", "2020-09-14"); // Required API version

        // Initialize ApiClient and set environment
        ApiClient apiClient = new ApiClient(apiKeys);
        apiClient.setPlaidAdapter(plaidEnv);

        return apiClient.createService(PlaidApi.class);
    }

}
