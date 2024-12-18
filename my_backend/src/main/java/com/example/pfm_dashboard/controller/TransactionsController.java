package com.example.pfm_dashboard.controller;

import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.Request.AddTransactionRequest;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.service.ItemService;
import com.example.pfm_dashboard.service.TransactionsService;
import com.example.pfm_dashboard.service.AddTransactionsService;
import com.example.pfm_dashboard.service.UpdateTransactionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TransactionsController {

    private final TransactionsService transactionsService;
    private final ItemService itemService;
    private final AddTransactionsService addTransactionsService;
    private final UpdateTransactionsService updateTransactionsService;

    @Autowired
    public TransactionsController(TransactionsService transactionsService, ItemService itemService,AddTransactionsService addTransactionsService, UpdateTransactionsService updateTransactionsService) {
        this.transactionsService = transactionsService;
        this.itemService = itemService;
        this.addTransactionsService = addTransactionsService;
        this.updateTransactionsService = updateTransactionsService;
    }

    // Endpoint to fetch and save recent transactions
    @GetMapping("/get_transactions")
    public ResponseEntity<List<TransactionDTO>> getRecentTransactions(@RequestParam String username) {
        try {
            List<String> itemIds = itemService.getItemIdsByUsername(username);
            if (itemIds == null || itemIds.isEmpty()) {
                // Return empty list if no item IDs are found
                return ResponseEntity.ok(List.of());
            }

            List<TransactionDTO> allTransactions = itemIds.stream()
                    .flatMap(itemId -> {
                        try {
                            return transactionsService.getRecentTransactionsByItemId(itemId).stream();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .toList();

            return ResponseEntity.ok(allTransactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    @GetMapping("/get-saved-transactions")
    public ResponseEntity<List<TransactionDTO>> getSavedTransactions(@RequestParam String username) {
        try {
            // Fetch all transactions for the user using the service layer
            List<TransactionDTO> transactions = transactionsService.getAllTransactionsForUser(username);

            // Return the transactions in the response
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to get total income and expenses
    @GetMapping("/total-expenses-income")
    public ResponseEntity<Map<String, BigDecimal>> getTotalExpensesAndIncome(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        try {
            Map<String, BigDecimal> result = transactionsService.calculateIncomeVsExpenses(
                    userId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to get spending by category
    @GetMapping("/transactions-by-category")
    public ResponseEntity<Map<String, BigDecimal>> getTransactionsByCategory(
            @RequestParam Long userId,
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        try {
            Map<String, BigDecimal> spendingByCategory = transactionsService.calculateSpendingByCategory(
                    userId,
                    LocalDate.parse(startDate),
                    LocalDate.parse(endDate)
            );
            return ResponseEntity.ok(spendingByCategory);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to get spending by merchants
    @GetMapping("/spending-by-merchants")
    public ResponseEntity<Map<String, BigDecimal>> getSpendingByMerchants(
            @RequestParam Long userId,
            @RequestParam(required = false) String yearMonth
    ) {
        try {
            if (yearMonth == null || yearMonth.isEmpty()) {
                yearMonth = LocalDate.now().toString().substring(0, 7); // Default to current month
            }
            Map<String, BigDecimal> spendingByMerchants = transactionsService.calculateSpendingByMerchants(userId, yearMonth);
            return ResponseEntity.ok(spendingByMerchants);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    

    // Endpoint to get most frequent merchants
    @GetMapping("/most-frequent-merchants")
    public ResponseEntity<Map<String, Long>> getMostFrequentMerchants(
            @RequestParam Long userId,
            @RequestParam int topN
    ) {
        try {
            Map<String, Long> mostFrequentMerchants = transactionsService.getMostFrequentMerchants(userId, topN);
            return ResponseEntity.ok(mostFrequentMerchants);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    // Endpoint to filter transactions by category and date range
    @GetMapping("/filter_transactions")
    public ResponseEntity<List<TransactionDTO>> filterTransactions(
            @RequestParam String username,
            @RequestParam(required = false) String category,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate

    ) {
        try {
            System.out.println("filtering");
            List<TransactionDTO> filteredTransactions = transactionsService.filterTransactionsByCategoryAndDate(
                    username,
                    category,
                    startDate,
                    endDate
            );
            return ResponseEntity.ok(filteredTransactions);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PatchMapping("/update-txn-amount")
    public ResponseEntity<Map<String, String>> updateTransactionAmount(
            @RequestBody Map<String, Object> requestBody) {
        try {
            List<String> transactionIds = (List<String>) requestBody.get("transactionIds");
            BigDecimal newAmount = new BigDecimal(requestBody.get("newAmount").toString());
    
            if (transactionIds == null || transactionIds.isEmpty() || newAmount == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request payload."));
            }
    
            // Call the service to update the transaction amount
            updateTransactionsService.updateTransactionAmount(transactionIds, newAmount);
    
            return ResponseEntity.ok(Map.of("message", "Transaction amounts updated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }
    @PatchMapping("/update-txn-merchant")
    public ResponseEntity<Map<String, String>> updateTransactionMerchant(
            @RequestBody Map<String, Object> requestBody) {
        try {
            List<String> transactionIds = (List<String>) requestBody.get("transactionIds");
            String newMerchantName = (String) requestBody.get("newMerchantName");

            if (transactionIds == null || transactionIds.isEmpty() || newMerchantName == null || newMerchantName.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request payload."));
            }

            // Call the service to update the transaction merchant name
            updateTransactionsService.updateTransactionMerchant(transactionIds, newMerchantName);

            return ResponseEntity.ok(Map.of("message", "Transaction merchants updated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PatchMapping("/update-txn-date")
    public ResponseEntity<Map<String, String>> updateTransactionDate(
            @RequestBody Map<String, Object> requestBody) {
        try {
            List<String> transactionIds = (List<String>) requestBody.get("transactionIds");
            LocalDate newDate = LocalDate.parse((String) requestBody.get("newDate"));

            if (transactionIds == null || transactionIds.isEmpty() || newDate == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request payload."));
            }

            // Call the service to update the transaction date
            updateTransactionsService.updateTransactionDate(transactionIds, newDate);

            return ResponseEntity.ok(Map.of("message", "Transaction dates updated successfully."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }


    

    @PatchMapping("/update-txn-category")
    public ResponseEntity<Map<String, String>> updateTransactionCategory(
            @RequestBody Map<String, Object> requestBody) {
        try {
            List<String> transactionIds = (List<String>) requestBody.get("transactionIds");
            String newCategory = (String) requestBody.get("newCategory");
    
            if (transactionIds == null || transactionIds.isEmpty() || newCategory == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid request payload."));
            }
    
            // Call service to update the transaction category
            Category updatedCategory = updateTransactionsService.updateTransactionCategory(transactionIds, newCategory);
    
            // Return the updated category details (name and color)
            Map<String, String> response = Map.of(
                "name", updatedCategory.getName(),
                "color", updatedCategory.getColor()
            );
            return ResponseEntity.ok(response);
    
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }    

    @PostMapping("/add-transaction")
    public ResponseEntity<TransactionDTO> addTransaction(@RequestBody AddTransactionRequest request) {
        System.out.println("Received Add Transaction Request: " + request);
        try {
            System.out.println("Received Add Transaction Request: " + request);
            
            TransactionDTO transactionDTO = addTransactionsService.addTransaction(
                    request.getUsername(),
                    request.getMerchantName(),
                    request.getAmount(),
                    request.getAccountName(),
                    request.getCategoryName(),
                    request.getDate()
            );
            System.out.println("Transaction successfully added: " + transactionDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(transactionDTO);
        } catch (IllegalArgumentException e) {
            System.out.println("Bad request: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            System.out.println("Internal server error: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
    

}
