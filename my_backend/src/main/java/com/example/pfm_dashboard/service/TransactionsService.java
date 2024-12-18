package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import com.example.pfm_dashboard.repository.AccountRepository;
import com.example.pfm_dashboard.repository.BudgetRepository;
import com.example.pfm_dashboard.repository.CategoryRepository;
import com.example.pfm_dashboard.repository.SimpleTransactionRepository;
import com.plaid.client.model.Transaction;
import com.plaid.client.model.TransactionsSyncRequest;
import com.plaid.client.model.TransactionsSyncResponse;
import com.plaid.client.request.PlaidApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Response;
import org.springframework.transaction.annotation.Transactional;
import java.math.RoundingMode;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionsService {


    private final PlaidApi plaidClient;
    private final ItemRepository itemRepository;
    private final SimpleTransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Autowired
    public TransactionsService(PlaidApi plaidClient,
                                ItemRepository itemRepository,
                                SimpleTransactionRepository transactionRepository,
                                UserRepository userRepository,
                                AccountRepository accountRepository,
                                BudgetRepository budgetRepository,
                                CategoryRepository categoryRepository,
                                CategoryService categoryService) {
        this.plaidClient = plaidClient;
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    public List<TransactionDTO> getRecentTransactionsByItemId(String itemId) throws IOException, InterruptedException {
        Optional<Item> optionalItem = itemRepository.findByItemId(itemId);
        if (optionalItem.isEmpty()) {
            throw new IllegalArgumentException("Item not found with itemId: " + itemId);
        }
    
        Item item = optionalItem.get();
        String accessToken = item.getAccessToken();
        String cursor = item.getTransactionCursor();
    
        List<SimpleTransaction> existingTransactions = transactionRepository.findByItem(item);
    
        List<SimpleTransaction> addedTransactions = new ArrayList<>();
        boolean hasMore = true;
        while (hasMore) {
            TransactionsSyncRequest request = new TransactionsSyncRequest().accessToken(accessToken).cursor(cursor);
            Response<TransactionsSyncResponse> response = plaidClient.transactionsSync(request).execute();
    
            if (!response.isSuccessful()) {
                throw new IOException("Failed to fetch transactions: " + response.errorBody().string());
            }
    
            TransactionsSyncResponse responseBody = response.body();
            if (responseBody == null) {
                throw new IOException("Response body is null");
            }
    
            List<Transaction> added = responseBody.getAdded();
            if (!added.isEmpty()) {
                cursor = responseBody.getNextCursor();
                added.forEach(plaidTxn -> {
                    Account account = accountRepository.findByAccountId(plaidTxn.getAccountId())
                            .orElseThrow(() -> new IllegalStateException("Account not found for accountId: " + plaidTxn.getAccountId()));
    
                    String categoryName = plaidTxn.getPersonalFinanceCategory() != null
                            ? plaidTxn.getPersonalFinanceCategory().getPrimary().toLowerCase().trim()
                            : "Uncategorized";
    
                    // Use the CategoryService to find or create a category with a color
                    Category category = categoryRepository.findByNameAndUserId(categoryName, item.getUser().getId())
                            .orElseGet(() -> categoryService.createCategory(categoryName, item.getUser()));
    
                    SimpleTransaction txn = new SimpleTransaction(
                            plaidTxn.getTransactionId(),
                            BigDecimal.valueOf(plaidTxn.getAmount()),
                            BigDecimal.valueOf(plaidTxn.getAmount()),
                            category,
                            category,
                            plaidTxn.getDate(),
                            plaidTxn.getMerchantName() != null ? plaidTxn.getMerchantName() : plaidTxn.getName(),
                            item,
                            item.getUser(),
                            account
                    );
    
                    transactionRepository.deleteByTransactionId(txn.getTransactionId());
                    addedTransactions.add(txn);
                    transactionRepository.save(txn);
                });
    
                hasMore = responseBody.getHasMore();
                item.setTransactionCursor(cursor);
                itemRepository.save(item);
            } else {
                hasMore = false;
            }
        }
    
        existingTransactions.addAll(addedTransactions);
    
        Map<String, SimpleTransaction> uniqueTransactions = existingTransactions.stream()
                .collect(Collectors.toMap(SimpleTransaction::getTransactionId, txn -> txn, (txn1, txn2) -> txn2));
    
        return uniqueTransactions.values().stream()
                .sorted(Comparator.comparing(SimpleTransaction::getDate).reversed())
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }
    

    public List<TransactionDTO> getAllTransactionsForUser(String username) {
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username))
            .getId();
    
        // Fetch transactions sorted by date in descending order
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdOrderByDateDesc(userId);
    
        // Convert transactions to DTOs
        return transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }
    


    public Map<String, BigDecimal> calculateSpendingByCategory(Long userId, LocalDate startDate, LocalDate endDate) {
        // Fetch transactions for the given user and date range
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    
        Map<String, BigDecimal> spending = new HashMap<>();
    
        for (SimpleTransaction txn : transactions) {
            if (txn == null || txn.getAmount() == null) {
                System.out.println("Skipping transaction with missing data: " + txn);
                continue;
            }
    
            if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0) { // Check for expenses (positive amounts)
                Category category = txn.getCategory(); // Fetch the Category entity
                String categoryName = category != null ? category.getName() : "Uncategorized";
    
                // Add the amount to the corresponding category's total
                spending.put(categoryName, spending.getOrDefault(categoryName, BigDecimal.ZERO).add(txn.getAmount().abs()));
            } else {
                System.out.println("Skipping invalid transaction: " + txn);
            }
        }
    
        // Optionally, sort the spending by amount in descending order
        return spending.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, // Merge function for duplicate keys (not expected here)
                        LinkedHashMap::new // Maintain order
                ));
    }
    

    public Map<String, BigDecimal> calculateSpendingByCategory(Long userId, YearMonth month) {
        // Convert YearMonth into LocalDate start and end dates
        LocalDate startDate = month.atDay(1); // First day of the month
        LocalDate endDate = month.atEndOfMonth(); // Last day of the month
    
        // Fetch transactions for the user and specific date range
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
    
        // Calculate total spending by category
        Map<String, BigDecimal> spendingByCategory = new HashMap<>();
        for (SimpleTransaction txn : transactions) {
            if (txn == null || txn.getAmount() == null) {
                System.out.println("Skipping transaction with missing data: " + txn);
                continue;
            }
    
            if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0) { // Check for positive amounts (expenses)
                Category category = txn.getCategory(); // Get the associated Category entity
                String categoryName = category != null ? category.getName() : "uncategorized"; // Default to "uncategorized"
    
                // Add the amount to the category's total
                spendingByCategory.put(
                    categoryName,
                    spendingByCategory.getOrDefault(categoryName, BigDecimal.ZERO).add(txn.getAmount().abs())
                );
            } else {
                System.out.println("Skipping invalid transaction: " + txn);
            }
        }
    
        // Return spending by category
        return spendingByCategory;
    }
       

    public Map<String, BigDecimal> calculateIncomeVsExpenses(Long userId, LocalDate startDate, LocalDate endDate) {
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (SimpleTransaction txn : transactions) {
            if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                totalExpenses = totalExpenses.add(txn.getAmount());
            } else {
                totalIncome = totalIncome.add(txn.getAmount().abs());
            }
        }

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("Income", totalIncome);
        summary.put("Expenses", totalExpenses);
        return summary;
    }

    public List<TransactionDTO> getTransactionsByMerchant(Long userId, String merchantName) {
        List<SimpleTransaction> transactions = transactionRepository.findByUserId(userId).stream()
                .filter(txn -> merchantName.equalsIgnoreCase(txn.getMerchantName()))
                .collect(Collectors.toList());

        return transactions.stream()
                .map(TransactionDTO::new)
                .collect(Collectors.toList());
    }

    public List<String> getTransactionIdsByCategory(Long userId, String categoryName) {
        // Fetch transactions by user ID and filter by category name
        List<SimpleTransaction> transactions = transactionRepository.findByUserId(userId).stream()
                .filter(txn -> categoryName.equalsIgnoreCase(txn.getCategory().getName()))
                .collect(Collectors.toList());
    
        // Return only the transaction IDs
        return transactions.stream()
                .map(SimpleTransaction::getTransactionId)
                .collect(Collectors.toList());
    }    

    public Map<String, BigDecimal> calculateSpendingByMerchants(Long userId, String yearMonth) {
        LocalDate startDate = LocalDate.parse(yearMonth + "-01");
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
    
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        List<String> recurringKeywords = List.of("PAYMENT", "AUTOMATIC", "CREDIT CARD", "RECURRING", "SUBSCRIPTION", "AUTO PAY", "MONTHLY");
    
        Map<String, BigDecimal> spendingByMerchant = new HashMap<>();
        for (SimpleTransaction txn : transactions) {
            if (txn.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                String merchant = txn.getMerchantName() != null
                    ? txn.getMerchantName().replaceAll("\\s+", " ").trim()
                    : "Unknown";
                boolean isRecurring = recurringKeywords.stream()
                    .anyMatch(keyword -> merchant.toLowerCase().contains(keyword.toLowerCase()));
                if (isRecurring) {
                    System.out.println("Filtered out recurring transaction: " + merchant);
                    continue;
                }
                spendingByMerchant.put(
                    merchant,
                    spendingByMerchant.getOrDefault(merchant, BigDecimal.ZERO).add(txn.getAmount().abs())
                );
            }
        }
        return spendingByMerchant;
    }
    
    
    
    public Map<String, Long> getMostFrequentMerchants(Long userId, int topN) {
        List<SimpleTransaction> transactions = transactionRepository.findByUserId(userId);

        Map<String, Long> merchantFrequency = transactions.stream()
                .filter(txn -> txn.getMerchantName() != null)
                .collect(Collectors.groupingBy(SimpleTransaction::getMerchantName, Collectors.counting()));

        return merchantFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(topN)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }



    public List<TransactionDTO> filterTransactionsByCategoryAndDate(
        String username, 
        String categoryName, 
        LocalDate startDate, 
        LocalDate endDate
    ) {
        // Retrieve the userId from the username
        Long userId = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("User not found for username: " + username))
            .getId();
    
        // Fetch the transactions filtered by user, category, and date range
        List<SimpleTransaction> transactions;
    
        if (categoryName == null || categoryName.isBlank()) {
            // If no category is specified, fetch transactions only filtered by user and date range
            transactions = transactionRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else {
            // Fetch the category entity
            Category category = categoryRepository.findByName(categoryName.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
    
            // Fetch transactions filtered by category
            transactions = transactionRepository.findByUserIdAndCategoryAndDateBetween(userId, category, startDate, endDate);
        }
    
        // Map transactions to DTOs
        return transactions.stream()
            .map(TransactionDTO::new)
            .collect(Collectors.toList());
    }
    

    public LocalDate getOldestTransactionDateForUser(Long userId) {
        return transactionRepository.findOldestTransactionDateByUserId(userId)
                .orElse(null); // Return null if no transactions exist
    }
    
    public List<TransactionDTO> getTransactionsByCategory(Long userId, String categoryName) {
        System.out.println("Fetching transactions for user: " + userId + " and category: " + categoryName);
        Category category = categoryRepository.findByNameAndUserId(categoryName.toLowerCase().trim(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));
        System.out.println("Category found: " + category.getName());
    
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndCategory(userId, category);
        System.out.println("Transactions found: " + transactions.size());
        
        return transactions.stream().map(TransactionDTO::new).collect(Collectors.toList());
    }
      
}