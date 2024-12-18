package com.example.pfm_dashboard.resources;

import com.example.pfm_dashboard.dto.AccountDetails;
import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.service.UserService;
import com.example.pfm_dashboard.repository.AccountRepository;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.InstitutionsGetByIdRequest;
import com.plaid.client.model.InstitutionsGetByIdResponse;
import com.plaid.client.model.ItemGetRequest;
import com.plaid.client.model.ItemGetResponse;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.Response;
import java.util.stream.Collectors;
import com.example.pfm_dashboard.service.ItemService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class AccessTokenResource {

    private final PlaidApi plaidClient;
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final AccountRepository accountRepository;

    @Autowired
    public AccessTokenResource(PlaidApi plaidClient, UserService userService, ItemRepository itemRepository, AccountRepository accountRepository) {
        this.plaidClient = plaidClient;
        this.userService = userService;
        this.itemRepository = itemRepository;
        this.accountRepository = accountRepository;
    }

    public static class PublicTokenRequest {
        private String publicToken;
        private String username;

        public String getPublicToken() { return publicToken; }
        public void setPublicToken(String publicToken) { this.publicToken = publicToken; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @PostMapping("/set_access_token")
    public ResponseEntity<String> setAccessToken(@RequestBody PublicTokenRequest request) throws IOException {
        ItemPublicTokenExchangeRequest exchangeRequest = new ItemPublicTokenExchangeRequest()
                .publicToken(request.getPublicToken());
        Response<ItemPublicTokenExchangeResponse> response = plaidClient.itemPublicTokenExchange(exchangeRequest).execute();

        if (response.isSuccessful()) {
            String accessToken = response.body().getAccessToken();
            String itemId = response.body().getItemId();

            User user = userService.findUserByUsername(request.getUsername());
            if (user == null) {
                return ResponseEntity.status(404).body("User not found.");
            }

            // Fetch bank (institution) details using the access token
            String bankName = fetchBankName(accessToken);

            // Create a new Item entry for the accessToken
            Item item = new Item();
            item.setItemId(itemId);
            item.setUser(user);
            item.setAccessToken(accessToken);
            item.setBankName(bankName);
            itemRepository.save(item);

            // Check if a "Cash" account already exists for the user
            if (!accountRepository.existsByNameAndUserId("Cash", user.getId())) {
                // Use a DTO to represent the "Cash" account
                AccountDetails cashAccountDetails = new AccountDetails(
                    "Manual Account", // Institution Name
                    "Cash", // Account Name
                    "manual", // Account Type
                    "cash", // Account Subtype
                    BigDecimal.ZERO, // Available Balance
                    BigDecimal.ZERO, // Current Balance
                    UUID.randomUUID().toString() // Account ID
                );

                // Map DTO to Account entity
                Account cashAccount = new Account();
                cashAccount.setUser(user);
                cashAccount.setName(cashAccountDetails.getAccountName());
                cashAccount.setType(cashAccountDetails.getAccountType());
                cashAccount.setSubtype(cashAccountDetails.getAccountSubtype());
                cashAccount.setAvailableBalance(cashAccountDetails.getAvailableBalance());
                cashAccount.setCurrentBalance(cashAccountDetails.getCurrentBalance());
                cashAccount.setAccountId(cashAccountDetails.getAccountId());
                cashAccount.setItem(null); // Not associated with any Item

                // Save the Cash account
                accountRepository.save(cashAccount);
            }

            return ResponseEntity.ok("Access token, bank info, and cash account saved successfully for user: " + user.getEmail());
        } else {
            return ResponseEntity.status(500).body("Failed to exchange public token.");
        }
    }



    public String fetchBankName(String accessToken) throws IOException {
        // Fetch the institution ID associated with the access token
        ItemGetRequest itemGetRequest = new ItemGetRequest().accessToken(accessToken);
        Response<ItemGetResponse> itemResponse = plaidClient.itemGet(itemGetRequest).execute();

        if (!itemResponse.isSuccessful() || itemResponse.body() == null) {
            throw new IOException("Failed to fetch item details: " + (itemResponse.errorBody() != null ? itemResponse.errorBody().string() : "Unknown error"));
        }

        String institutionId = itemResponse.body().getItem().getInstitutionId();

        // Fetch the institution details by ID
        InstitutionsGetByIdRequest institutionRequest = new InstitutionsGetByIdRequest()
                .institutionId(institutionId)
                .countryCodes(Collections.singletonList(CountryCode.US));
        Response<InstitutionsGetByIdResponse> institutionResponse = plaidClient.institutionsGetById(institutionRequest).execute();

        if (!institutionResponse.isSuccessful() || institutionResponse.body() == null) {
            throw new IOException("Failed to fetch institution details: " + (institutionResponse.errorBody() != null ? institutionResponse.errorBody().string() : "Unknown error"));
        }

        return institutionResponse.body().getInstitution().getName();
    }

    // Method to retrieve all bank names for a user
    @GetMapping("/get_all_bank_names")
    public ResponseEntity<List<String>> getAllBankNames(@RequestParam String username) {
        User user = userService.findUserByUsername(username);
        if (user == null) {
            return ResponseEntity.status(404).body(Collections.singletonList("User not found."));
        }

        // Retrieve all items associated with the user and collect bank names
        List<String> bankNames = itemRepository.findByUser(user).stream()
                .map(Item::getBankName)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bankNames);
    }
}
