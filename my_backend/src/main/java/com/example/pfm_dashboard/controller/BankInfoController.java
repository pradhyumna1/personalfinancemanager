package com.example.pfm_dashboard.controller;

import com.example.pfm_dashboard.dto.AccountDetails;
import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.service.AccountService;
import com.example.pfm_dashboard.service.ItemService;
import com.example.pfm_dashboard.repository.AccountRepository;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.AccountsGetRequest;
import com.plaid.client.model.AccountsGetResponse;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.InstitutionsGetByIdRequest;
import com.plaid.client.model.InstitutionsGetByIdResponse;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BankInfoController {

    private final PlaidApi plaidClient;
    private final ItemService itemService;
    private final AccountRepository accountRepository;
    private final AccountService accountService;

    @Autowired
    public BankInfoController(PlaidApi plaidClient, ItemService itemService, AccountRepository accountRepository, AccountService accountService) {
        this.plaidClient = plaidClient;
        this.itemService = itemService;
        this.accountRepository = accountRepository;
        this.accountService = accountService;

    }

    @GetMapping("/get_bank_info")
    public ResponseEntity<List<AccountDetails>> getBankInfo(@RequestParam String username) {
        try {
            List<String> accessTokens = itemService.getAccessTokensByUsername(username);
            if (accessTokens == null || accessTokens.isEmpty()) {
                System.out.println("No access tokens found for username: " + username);
                return ResponseEntity.ok(Collections.emptyList());
            }
    
            List<AccountDetails> accountDetailsList = new ArrayList<>();
    
            for (String accessToken : accessTokens) {
                // Check if accounts exist in the database for this access token
                List<Account> existingAccounts = accountService.getAccountsByAccessToken(accessToken);
                if (!existingAccounts.isEmpty()) {
                    System.out.println("Using cached accounts for access token: " + accessToken);
    
                    // Map existing accounts to AccountDetails DTOs
                    existingAccounts.forEach(account -> {
                        String institutionName = account.getItem().getBankName(); // Assuming Account -> Item relationship
                        accountDetailsList.add(new AccountDetails(
                            institutionName,
                            account.getName(),
                            account.getType(),
                            account.getSubtype(),
                            account.getAvailableBalance(),
                            account.getCurrentBalance(),
                            account.getAccountId()
                        ));
                    });
                    continue; // Skip calling the Plaid API
                }
    
                // Fetch accounts from Plaid API if not found in the database
                AccountsGetRequest accountsGetRequest = new AccountsGetRequest().accessToken(accessToken);
                Response<AccountsGetResponse> response = plaidClient.accountsGet(accountsGetRequest).execute();
    
                if (!response.isSuccessful() || response.body() == null) {
                    throw new IOException("Failed to fetch account details: " +
                            (response.errorBody() != null ? response.errorBody().string() : "Unknown error"));
                }
    
                String institutionName = itemService.getItemByAccessToken(accessToken).getBankName();
    
                for (AccountBase account : response.body().getAccounts()) {
                    AccountDetails accountDetails = new AccountDetails(
                            institutionName,
                            account.getName(),
                            account.getType() != null ? account.getType().toString() : "Unknown",
                            account.getSubtype() != null ? account.getSubtype().toString() : "Unknown",
                            account.getBalances().getAvailable() != null
                                    ? BigDecimal.valueOf(account.getBalances().getAvailable())
                                    : null,
                            account.getBalances().getCurrent() != null
                                    ? BigDecimal.valueOf(account.getBalances().getCurrent())
                                    : null,
                            account.getAccountId()
                    );
                    accountDetailsList.add(accountDetails);
    
                    saveAccountToDatabase(account, accessToken);
                }
            }
    
            return ResponseEntity.ok(accountDetailsList);
        } catch (Exception e) {
            System.err.println("An error occurred while processing the request: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }     

    private void saveAccountToDatabase(AccountBase account, String accessToken) {
        try {
            // Check if the account already exists in the database
            Account existingAccount = accountRepository.findByAccountId(account.getAccountId()).orElse(null);

            if (existingAccount == null) {
                // Create a new Account entity
                Account newAccount = new Account();
                newAccount.setAccountId(account.getAccountId());
                newAccount.setName(account.getName());
                newAccount.setType(account.getType() != null ? account.getType().toString() : "Unknown");
                newAccount.setSubtype(account.getSubtype() != null ? account.getSubtype().toString() : "Unknown");
                newAccount.setCurrentBalance(account.getBalances().getCurrent() != null
                        ? BigDecimal.valueOf(account.getBalances().getCurrent())
                        : null);
                newAccount.setAvailableBalance(account.getBalances().getAvailable() != null
                        ? BigDecimal.valueOf(account.getBalances().getAvailable())
                        : null);

                // Fetch the associated Item and User
                Item item = itemService.getItemByAccessToken(accessToken);
                if (item == null) throw new IllegalStateException("Item not found for accessToken: " + accessToken);

                User user = item.getUser();
                if (user == null) throw new IllegalStateException("User not found for Item associated with accessToken: " + accessToken);

                newAccount.setItem(item);
                newAccount.setUser(user);

                // Save the new Account to the database
                accountRepository.save(newAccount);
            } else {
                // Update existing Account details
                existingAccount.setName(account.getName());
                existingAccount.setType(account.getType() != null ? account.getType().toString() : "Unknown");
                existingAccount.setSubtype(account.getSubtype() != null ? account.getSubtype().toString() : "Unknown");
                existingAccount.setCurrentBalance(account.getBalances().getCurrent() != null
                        ? BigDecimal.valueOf(account.getBalances().getCurrent())
                        : null);
                existingAccount.setAvailableBalance(account.getBalances().getAvailable() != null
                        ? BigDecimal.valueOf(account.getBalances().getAvailable())
                        : null);

                // Save the updated Account to the database
                accountRepository.save(existingAccount);
            }
        } catch (Exception e) {
            System.err.println("Failed to save account: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String fetchInstitutionName(String institutionId) {
        try {
            InstitutionsGetByIdRequest institutionRequest = new InstitutionsGetByIdRequest()
                    .institutionId(institutionId)
                    .countryCodes(Collections.singletonList(CountryCode.US));
            Response<InstitutionsGetByIdResponse> institutionResponse = plaidClient.institutionsGetById(institutionRequest).execute();

            if (institutionResponse.isSuccessful() && institutionResponse.body() != null) {
                return institutionResponse.body().getInstitution().getName();
            } else {
                throw new IOException("Failed to fetch institution details: " +
                        (institutionResponse.errorBody() != null ? institutionResponse.errorBody().string() : "Unknown error"));
            }
        } catch (IOException e) {
            System.err.println("Error fetching institution name for institutionId: " + institutionId);
            e.printStackTrace();
            return "Unknown Institution";
        }
    }
}
