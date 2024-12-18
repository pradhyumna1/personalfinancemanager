package com.example.pfm_dashboard.controller;

import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.service.BudgetService;
import com.example.pfm_dashboard.service.CategoryService;
import com.example.pfm_dashboard.service.TransactionsService;
import com.example.pfm_dashboard.service.UserService;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionsService transactionsService;

    // Helper method to fetch user by username
    private User getUserByUsername(String username) {
        return userService.findUserByUsername(username);
    }

    // Create Budget
    @PostMapping("/create")
    public ResponseEntity<Budget> createBudget(@RequestBody Map<String, Object> requestBody, @RequestParam String username) {
        try {
            User user = getUserByUsername(username);

            // Parse request body
            String categoryName = (String) requestBody.get("category");
            Double amount = Double.valueOf(requestBody.get("amount").toString());
            YearMonth month = YearMonth.parse((String) requestBody.get("month"));

            // Fetch or create the category
            Category category = categoryService.findOrCreateCategory(categoryName, user);

            // Create budget
            Budget createdBudget = budgetService.createBudget(user, amount, month, category.getName());
            return ResponseEntity.status(201).body(createdBudget);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    // Update Budget Spent
    @PatchMapping("/update/{budgetId}")
    public ResponseEntity<Void> updateBudgetSpent(@PathVariable Long budgetId, @RequestParam Double spentAmount) {
        try {
            budgetService.updateBudgetSpent(budgetId, spentAmount);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    @PatchMapping("/updateAmount/{budgetId}")
    public ResponseEntity<String> updateBudgetAmount(
            @PathVariable Long budgetId,
            @RequestBody Map<String, Double> requestBody) {
        if (!requestBody.containsKey("amount")) {
            return ResponseEntity.badRequest().body("Missing required parameter: amount");
        }

        Double amount = requestBody.get("amount");
        budgetService.updateBudgetAmount(budgetId, amount);
        return ResponseEntity.ok("Budget amount updated successfully");
    }

    // Delete Budget
    @DeleteMapping("/delete/{budgetId}")
    public ResponseEntity<Void> deleteBudget(@PathVariable Long budgetId) {
        try {
            budgetService.deleteBudget(budgetId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }

    // Generate Suggested Budgets
    @GetMapping("/suggested")
    public ResponseEntity<Map<String, Map<String, BigDecimal>>> generateSuggestedBudgets(
            @RequestParam String username,
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(required = false) String month) {
        try {
            System.out.println("Generating suggested budgets for user: " + username);
    
            User user = getUserByUsername(username);
    
            LocalDate oldestTransactionDate = transactionsService.getOldestTransactionDateForUser(user.getId());
            System.out.println("Oldest transaction date: " + oldestTransactionDate);
    
            if (oldestTransactionDate == null) {
                return ResponseEntity.badRequest().body(null);
            }
    
            YearMonth earliestMonth = YearMonth.from(oldestTransactionDate);
            YearMonth currentMonth = YearMonth.now();
            int availableMonths = (int) ChronoUnit.MONTHS.between(earliestMonth, currentMonth);
    
            System.out.println("Available months for budgets: " + availableMonths);
    
            int adjustedMonths = Math.min(months, availableMonths);
    
            System.out.println("Adjusted months for budget generation: " + adjustedMonths);
    
            YearMonth specificMonth = month != null ? YearMonth.parse(month) : null;
            System.out.println("Specific month (if provided): " + specificMonth);
    
            Map<String, BigDecimal> suggestedBudgets = budgetService.generateSuggestedBudgets(user, adjustedMonths);
            System.out.println("Suggested budgets: " + suggestedBudgets);
    
            Map<String, BigDecimal> actualSpending = new HashMap<>();
            if (specificMonth != null) {
                actualSpending = budgetService.calculateActualSpendingForMonth(user, specificMonth);
                System.out.println("Actual spending for " + specificMonth + ": " + actualSpending);
            }
    
            Map<String, Map<String, BigDecimal>> response = new HashMap<>();
            for (String category : suggestedBudgets.keySet()) {
                Map<String, BigDecimal> details = new HashMap<>();
                details.put("suggested", suggestedBudgets.get(category));
                details.put("actual", actualSpending.getOrDefault(category, BigDecimal.ZERO));
                response.put(category, details);
            }
    
            System.out.println("Final response: " + response);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }    

    @GetMapping("/user/{username}")
    public ResponseEntity<List<Budget>> getUserBudgets(
            @PathVariable String username,
            @RequestParam String month) {
        try {
            User user = getUserByUsername(username);

            YearMonth budgetMonth = YearMonth.parse(month);

            List<Budget> budgets = budgetService.getBudgetsForUser(user, budgetMonth);

            if (budgets == null || budgets.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/create-suggested")
    public ResponseEntity<Void> createSuggestedBudgets(
            @RequestBody Map<String, Object> requestPayload) {
        try {
            String username = (String) requestPayload.get("username");
            List<Map<String, Object>> budgetsRequest = (List<Map<String, Object>>) requestPayload.get("budgets");
            String monthString = (String) requestPayload.get("month");
    
            YearMonth budgetMonth = monthString != null && !monthString.isEmpty()
                    ? YearMonth.parse(monthString)
                    : YearMonth.now();
    
            User user = getUserByUsername(username);
    
            List<Budget> suggestedBudgets = budgetsRequest.stream()
                    .map(b -> {
                        // Extract the 'name' field from the 'category' object
                        Map<String, String> categoryMap = (Map<String, String>) b.get("category");
                        String categoryName = categoryMap.get("name"); // Get the category name
                        
                        Category category = categoryService.findOrCreateCategory(categoryName, user);
                        
                        Budget budget = new Budget();
                        budget.setUser(user);
                        budget.setCategory(category);
                        budget.setAmount(Double.valueOf(b.get("amount").toString()));
                        budget.setSpent(Double.valueOf(b.getOrDefault("spent", 0).toString()));
                        budget.setMonth(budgetMonth);
                        return budget;
                    }).collect(Collectors.toList());
    
            budgetService.saveAll(suggestedBudgets);
    
            return ResponseEntity.status(201).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    

    @GetMapping("/transactions-by-category")
    public Map<YearMonth, List<TransactionDTO>> getTransactionsByCategoryGroupedByMonth(
            @RequestParam String username,
            @RequestParam String category) {
    
        System.out.println("Fetching transactions for username: " + username + " and category: " + category);
    
        User user = getUserByUsername(username);
        System.out.println("User found: " + user.getUsername());
    
        Map<YearMonth, List<TransactionDTO>> transactions = budgetService.getTransactionsByCategoryGroupedByMonth(user.getId(), category);
        System.out.println("Transactions grouped by month: " + transactions);
    
        return transactions;
    }

    
    
}
