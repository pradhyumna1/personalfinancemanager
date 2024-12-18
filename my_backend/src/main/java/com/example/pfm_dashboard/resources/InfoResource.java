package com.example.pfm_dashboard.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Arrays;

@RestController
@RequestMapping("/api")
public class InfoResource {

    // Sample product list for demonstration (replace with actual data retrieval)
    private final List<String> plaidProducts = Arrays.asList("auth", "transactions");

    // Define the response structure
    public static class InfoResponse {
        @JsonProperty
        private final List<String> products;
        @JsonProperty
        private final String accessToken;
        @JsonProperty
        private final String itemId;

        public InfoResponse(List<String> products, String accessToken, String itemId) {
            this.products = products;
            this.accessToken = accessToken;
            this.itemId = itemId;
        }
    }

    // Adjust this method to retrieve actual values as needed
    @PostMapping("/info")
    public InfoResponse getInfo() {
        // Sample access token and item ID values for demonstration (replace with actual retrieval logic)
        String accessToken = "sample-access-token";
        String itemId = "sample-item-id";

        return new InfoResponse(plaidProducts, accessToken, itemId);
    }
}
