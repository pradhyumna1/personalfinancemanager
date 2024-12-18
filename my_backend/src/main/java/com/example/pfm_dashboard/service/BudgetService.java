package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.BudgetRepository;
import com.example.pfm_dashboard.repository.CategoryRepository;
import com.example.pfm_dashboard.repository.SimpleTransactionRepository;
import com.example.pfm_dashboard.service.TransactionsService;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionsService transactionsService;
    private final CategoryRepository categoryRepository;
    private final SimpleTransactionRepository transactionRepository;

    @Autowired
    public BudgetService(BudgetRepository budgetRepository, TransactionsService transactionsService, CategoryRepository categoryRepository, SimpleTransactionRepository transactionRepository) {
        this.budgetRepository = budgetRepository;
        this.transactionsService = transactionsService;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
    }

    public void saveAll(List<Budget> budgets) {
        budgetRepository.saveAll(budgets);
    }

    public void saveBudget(Budget budget) {
        budgetRepository.save(budget);
    }

    public Budget createBudget(User user, Double amount, YearMonth month, String categoryName) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }

        Category category = findOrCreateCategory(categoryName, user);

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setAmount(amount);
        budget.setMonth(month);
        budget.setCategory(category);
        return budgetRepository.save(budget);
    }

    public void updateBudgetSpent(Long budgetId, Double spentAmount) {
        if (spentAmount < 0) {
            throw new IllegalArgumentException("Spent amount must be non-negative");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setSpent(budget.getSpent() + spentAmount);
        budgetRepository.save(budget);
    }

    public void updateBudgetAmount(Long budgetId, Double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must be non-negative");
        }

        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budget.setAmount(amount);
        budgetRepository.save(budget);
    }

    public void deleteBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found"));
        budgetRepository.delete(budget);
    }
    
    @Transactional
    public Map<String, BigDecimal> generateSuggestedBudgets(User user, int months) {
        YearMonth endMonth = YearMonth.now().minusMonths(1); // Last month
        YearMonth startMonth = YearMonth.now().minusMonths(months); // 6 months ago

        LocalDate defaultStartDate = startMonth.atDay(1);
        LocalDate endDate = endMonth.atEndOfMonth();

        // Fetch the oldest transaction date for the user
        LocalDate oldestTransactionDate = transactionRepository.findOldestTransactionDateByUserId(user.getId())
                .orElse(LocalDate.now().minusMonths(6)); // Default to 6 months ago if no transactions exist

        System.out.println("Oldest transaction date: " + oldestTransactionDate);
        System.out.println("Default start date: " + defaultStartDate);
        System.out.println("End date: " + endDate);

        // Adjust the start date to be the first day of the month of the oldest transaction,
        // but cap it to a maximum of 6 months ago.
        LocalDate adjustedStartDate = oldestTransactionDate.isBefore(defaultStartDate)
                ? defaultStartDate
                : YearMonth.from(oldestTransactionDate).atDay(1);

        System.out.println("Adjusted start date: " + adjustedStartDate);

        // Calculate the total number of months in the adjusted timeframe
        long numberOfMonths = ChronoUnit.MONTHS.between(YearMonth.from(adjustedStartDate), endMonth) + 1;
        System.out.println("Number of months for budget calculation: " + numberOfMonths);

        // Fetch transactions for the user within the specified time range
        List<SimpleTransaction> transactions = transactionRepository.findByUserIdAndDateBetween(user.getId(), adjustedStartDate, endDate);

        System.out.println("Total transactions fetched: " + transactions.size());

        Map<String, BigDecimal> categoryTotalSpending = new HashMap<>();
        Map<String, Set<YearMonth>> categoryDistinctMonths = new HashMap<>();

        for (SimpleTransaction txn : transactions) {
            String currentCategory = txn.getCategory().getName();
            BigDecimal amount = txn.getAmount();

            // Skip negative transactions
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            // Track spending for the current category
            categoryTotalSpending.merge(currentCategory, amount, BigDecimal::add);

            // Track distinct months for the current category
            YearMonth txnMonth = YearMonth.from(txn.getDate());
            categoryDistinctMonths.computeIfAbsent(currentCategory, k -> new HashSet<>()).add(txnMonth);

            // Include original category spending only if it's different from the current category
            if (txn.getOriginalCategory() != null && !txn.getOriginalCategory().equals(txn.getCategory())) {
                String originalCategory = txn.getOriginalCategory().getName();
                BigDecimal originalAmount = txn.getOriginalAmount();
                categoryTotalSpending.merge(originalCategory, originalAmount, BigDecimal::add);
                categoryDistinctMonths.computeIfAbsent(originalCategory, k -> new HashSet<>()).add(txnMonth);
            }
        }

        System.out.println("Category total spending: " + categoryTotalSpending);
        System.out.println("Category distinct months: " + categoryDistinctMonths);

        // Calculate suggested budgets
        Map<String, BigDecimal> suggestedBudgets = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : categoryTotalSpending.entrySet()) {
            String category = entry.getKey();
            BigDecimal totalSpending = entry.getValue();

            // Use the adjusted number of months as the divisor
            BigDecimal averageSpending = totalSpending.divide(BigDecimal.valueOf(numberOfMonths), 2, RoundingMode.HALF_UP);

            System.out.println("Category: " + category + ", Total Spending: " + totalSpending + ", Average Spending: " + averageSpending);

            suggestedBudgets.put(category, averageSpending);
        }

        return suggestedBudgets;
    }



    public List<Budget> createSuggestedBudgets(User user, Map<String, BigDecimal> suggestedBudgets, YearMonth month) {
        List<Budget> budgets = suggestedBudgets.entrySet().stream().map(entry -> {
            Category category = findOrCreateCategory(entry.getKey(), user);
            Budget budget = new Budget();
            budget.setUser(user);
            budget.setAmount(entry.getValue().doubleValue());
            budget.setMonth(month);
            budget.setCategory(category);
            return budgetRepository.save(budget);
        }).toList();

        return budgets;
    }

    public List<Budget> getBudgetsForUser(User user, YearMonth month) {
        List<Budget> budgets = budgetRepository.findByUserAndMonth(user, month);

        if (budgets == null || budgets.isEmpty()) {
            System.out.println("No budgets found for user: " + user.getUsername() + " for month: " + month);
            return List.of();
        }

        return budgets;
    }

    public Map<String, BigDecimal> calculateActualSpendingForMonth(User user, YearMonth month) {
        Map<String, BigDecimal> spendingByCategory = transactionsService.calculateSpendingByCategory(user.getId(), month);

        List<Category> categories = categoryRepository.findByUser(user);
        for (Category category : categories) {
            spendingByCategory.putIfAbsent(category.getName(), BigDecimal.ZERO);
        }

        return spendingByCategory;
    }

    public Map<YearMonth, List<TransactionDTO>> getTransactionsByCategoryGroupedByMonth(Long userId, String categoryName) {
        List<TransactionDTO> transactions = transactionsService.getTransactionsByCategory(userId, categoryName);

        return transactions.stream()
                .collect(Collectors.groupingBy(transaction -> YearMonth.from(transaction.getDate())));
    }

    private Category findOrCreateCategory(String categoryName, User user) {
        return categoryRepository.findByNameAndUser(categoryName, user)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(categoryName);
                    newCategory.setUser(user);
                    return categoryRepository.save(newCategory);
                });
    }
    @Transactional
    public void deleteAssociatedBudgets(String categoryName) {
        // Step 1: Find the category by name
        Category category = categoryRepository.findByName(categoryName.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

        
        List<Budget> budgetsToDelete = budgetRepository.findByCategory(category);

        System.out.println("yurrr" + budgetsToDelete);

        // Step 3: Delete the budgets
        if (!budgetsToDelete.isEmpty()) {
            budgetRepository.deleteAll(budgetsToDelete);
            System.out.println("Deleted " + budgetsToDelete.size() + " budgets associated with category: " + categoryName);
        } else {
            System.out.println("No budgets found for category: " + categoryName);
        }
    }
    
}
