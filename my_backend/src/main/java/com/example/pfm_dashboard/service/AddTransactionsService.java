package com.example.pfm_dashboard.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.task.TaskExecutionProperties.Simple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import com.example.pfm_dashboard.repository.AccountRepository;
import com.example.pfm_dashboard.repository.BudgetRepository;
import com.example.pfm_dashboard.repository.CategoryRepository;
import com.example.pfm_dashboard.repository.SimpleTransactionRepository;
import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.Account;
import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.model.SimpleTransaction;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class AddTransactionsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SimpleTransactionRepository transactionRepository;

    @Transactional
    public TransactionDTO addTransaction(String username, String merchantName, BigDecimal amount, String accountName, String categoryName, LocalDate date) {
        try {
            System.out.println("Starting to process addTransaction...");
            
            // Step 1: Retrieve user by username
            System.out.println("Fetching user by username: " + username);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Long userId = user.getId();
            System.out.println("User retrieved: " + user);
    
            // Step 2: Retrieve account by account name and user
            System.out.println("Fetching account by account name: " + accountName + " and userId: " + userId);
            Account account = accountRepository.findByNameAndUserId(accountName, userId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found"));
            System.out.println("Account retrieved: " + account);
    
            // Step 3: Retrieve or create category
            System.out.println("Fetching or creating category: " + categoryName);
            Category category = categoryRepository.findByNameAndUser(categoryName.toLowerCase(), user)
                    .orElseGet(() -> {
                        System.out.println("Category not found, creating a new one: " + categoryName);
                        return categoryService.createCategory(categoryName.toLowerCase(), user);
                    });
            System.out.println("Category retrieved/created: " + category);
    
            // Step 4: Create the transaction
            System.out.println("Creating transaction object...");
            SimpleTransaction transaction = new SimpleTransaction();
            transaction.setTransactionId(UUID.randomUUID().toString());
            transaction.setAmount(amount);
            transaction.setOriginalAmount(amount);
            transaction.setAccount(account);
            transaction.setMerchantName(merchantName);
            transaction.setCategory(category);
            transaction.setOriginalCategory(category);
            transaction.setDate(date);
            transaction.setUser(user);
            System.out.println("Transaction object created: " + transaction);
    
            // Save the transaction
            transaction = transactionRepository.save(transaction);
            System.out.println("Transaction saved to database: " + transaction);
    
            // Step 5: Convert the saved transaction to DTO
            TransactionDTO transactionDTO = convertToDTO(transaction);
            System.out.println("Transaction converted to DTO: " + transactionDTO);
    
            return transactionDTO;
        } catch (Exception e) {
            System.out.println("Exception occurred while adding transaction: " + e.getMessage());
            e.printStackTrace(); // Log the stack trace for debugging
            throw e;
        }
    }
    

    private TransactionDTO convertToDTO(SimpleTransaction transaction) {
        return new TransactionDTO(transaction);
    }
    
}
