package com.example.pfm_dashboard.resources;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestStatements;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.Products;
import com.plaid.client.request.PlaidApi;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import retrofit2.Response;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class LinkTokenResource {

    private final PlaidApi plaidClient;

    @Autowired
    public LinkTokenResource(PlaidApi plaidClient) {
        this.plaidClient = plaidClient;
    }

    @Value("${plaid.products}")
    private List<String> plaidProducts;

    @Value("${plaid.countryCodes}")
    private List<String> countryCodes;

    @Value("${plaid.redirect_uri:}")
    private String redirectUri;

    @Value("${plaid.android_package_name:}")
    private String androidPackageName;

    @PostMapping("/create_link_token")
    public LinkToken getLinkToken() throws IOException {
        String clientUserId = getCurrentUserId(); // Fetch the current logged-in user ID dynamically

        LinkTokenCreateRequestUser user = new LinkTokenCreateRequestUser()
                .clientUserId(clientUserId);

        List<Products> products = new ArrayList<>();
        for (String product : plaidProducts) {
            try {
                products.add(Products.fromValue(product));
            } catch (IllegalArgumentException e) {
                System.out.println("Unsupported product: " + product);
            }
        }

        List<CountryCode> convertedCountryCodes = countryCodes.stream()
                .map(CountryCode::fromValue)
                .collect(Collectors.toList());

        LinkTokenCreateRequest request = new LinkTokenCreateRequest()
                .user(user)
                .clientName("BudgetSphere")
                .products(products)
                .countryCodes(convertedCountryCodes)
                .language("en");

        if (!redirectUri.isEmpty()) {
            request.setRedirectUri(redirectUri);
        }

        if (!androidPackageName.isEmpty()) {
            request.setAndroidPackageName(androidPackageName);
        }

        if (products.contains(Products.STATEMENTS)) {
            LinkTokenCreateRequestStatements statementsConfig = new LinkTokenCreateRequestStatements()
                    .startDate(LocalDate.now().minusDays(30))
                    .endDate(LocalDate.now());
            request.setStatements(statementsConfig);
        }

        Response<LinkTokenCreateResponse> response = plaidClient.linkTokenCreate(request).execute();

        if (response.isSuccessful() && response.body() != null) {
            return new LinkToken(response.body().getLinkToken());
        } else {
            System.err.println("Error creating link token: " + response.errorBody().string());
            throw new IOException("Failed to create link token: " + (response.errorBody() != null ? response.errorBody().string() : "unknown error"));
        }
    }

    private String getCurrentUserId() {
        // Replace with actual logic to fetch the currently logged-in user's ID
        return "dynamic-user-id";
    }

    public static class LinkToken {
        @JsonProperty
        private String linkToken;

        public LinkToken(String linkToken) {
            this.linkToken = linkToken;
        }

        public String getLinkToken() {
            return linkToken;
        }
    }
}
