package com.example.pfm_dashboard.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.AccountRepository;
import com.example.pfm_dashboard.repository.BudgetRepository;
import com.example.pfm_dashboard.repository.CategoryRepository;
import com.example.pfm_dashboard.repository.ItemRepository;
import com.example.pfm_dashboard.repository.SimpleTransactionRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.Optional;

@Service
public class UpdateTransactionsService {
    private final SimpleTransactionRepository transactionRepository;

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    @Autowired
    public UpdateTransactionsService(
                                ItemRepository itemRepository,
                                SimpleTransactionRepository transactionRepository,
                                UserRepository userRepository,
                                AccountRepository accountRepository,
                                BudgetRepository budgetRepository,
                                CategoryRepository categoryRepository,
                                CategoryService categoryService) {
        this.transactionRepository = transactionRepository;
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Transactional
    public void updateTransactionAmount(List<String> transactionIds, BigDecimal newAmount) {
        if (transactionIds.isEmpty()) {
            throw new IllegalArgumentException("Transaction IDs list is empty.");
        }
    
        // Fetch transactions by IDs
        List<SimpleTransaction> transactions = transactionRepository.findAllById(transactionIds);
    
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found for the provided IDs.");
        }
    
        // Handle single transaction case for optimization
        if (transactions.size() == 1) {
            SimpleTransaction transaction = transactions.get(0);
            System.out.println("Processing a single transaction update for ID: " + transaction.getTransactionId());
    
            BigDecimal difference = newAmount.subtract(transaction.getAmount());
            transaction.setAmount(newAmount);
            transactionRepository.save(transaction);
    
            updateBudgetForTransaction(transaction, difference);
            return; // Exit after single transaction update
        }
    
        // Process multiple transactions
        for (SimpleTransaction transaction : transactions) {
            BigDecimal difference = newAmount.subtract(transaction.getAmount());
            transaction.setAmount(newAmount);
            transactionRepository.save(transaction);
    
            updateBudgetForTransaction(transaction, difference);
        }
    }  

    private void updateBudgetForTransaction(SimpleTransaction transaction, BigDecimal difference) {
        LocalDate transactionDate = transaction.getDate();
        YearMonth yearMonth = YearMonth.from(transactionDate);
    
        // Get the category associated with the transaction
        Category transactionCategory = transaction.getCategory();
    
        // Check if a budget exists for the given category and month
        Optional<Budget> optionalBudget = budgetRepository.findByCategoryAndUserAndMonth(
            transactionCategory, transaction.getUser(), yearMonth
        );
    
        // Update the budget's spent column if the budget exists
        optionalBudget.ifPresentOrElse(
            budget -> {
                budget.setSpent(budget.getSpent() + difference.doubleValue());
                budgetRepository.save(budget); // Persist the updated budget
            },
            () -> System.out.println("No matching budget found for update!")
        );
    }

    @Transactional
    public void updateTransactionMerchant(List<String> transactionIds, String newMerchantName) {
        List<SimpleTransaction> transactions = transactionRepository.findAllById(transactionIds);

        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found for the provided IDs.");
        }

        for (SimpleTransaction transaction : transactions) {
            transaction.setMerchantName(newMerchantName);
            transactionRepository.save(transaction);
        }
    }
    
    @Transactional
    public Category updateTransactionCategory(List<String> transactionIds, String newCategoryName) {
        // Fetch transactions and their user
        List<SimpleTransaction> transactions = transactionRepository.findAllById(transactionIds);
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found.");
        }
        User user = transactions.get(0).getUser();
    
        // Create or find the new category using CategoryService to assign a color
        Category newCategory = categoryRepository.findByNameAndUser(newCategoryName.toLowerCase(), user)
                .orElseGet(() -> categoryService.createCategory(newCategoryName.toLowerCase(), user));
    
        // Fetch the old category from the first transaction (all transactions share the same old category)
        Category oldCategory = transactions.get(0).getCategory();
        if (oldCategory == null) {
            throw new IllegalArgumentException("Old category not found for the transactions.");
        }
    
        // Fetch all months where the user has existing budgets
        Set<YearMonth> monthsWithBudgets = budgetRepository.findDistinctMonthsByUser(user);
    
        // Calculate the total amount of the transactions and the oldest/newest transaction dates
        BigDecimal totalTransactionAmount = transactions.stream()
                .map(SimpleTransaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    
        LocalDate oldestTransactionDate = transactions.stream()
                .map(SimpleTransaction::getDate)
                .min(LocalDate::compareTo)
                .orElse(null);
    
        LocalDate newestTransactionDate = transactions.stream()
                .map(SimpleTransaction::getDate)
                .max(LocalDate::compareTo)
                .orElse(null);
    
        // Determine the adjusted start date
        YearMonth sixMonthsAgo = YearMonth.now().minusMonths(6);
        YearMonth adjustedStartMonth = (oldestTransactionDate != null && YearMonth.from(oldestTransactionDate).isBefore(sixMonthsAgo))
                ? sixMonthsAgo
                : YearMonth.from(oldestTransactionDate);
    
        long numberOfMonths = ChronoUnit.MONTHS.between(adjustedStartMonth, YearMonth.from(newestTransactionDate)) + 1;
    
        BigDecimal averageAmountPerMonth = totalTransactionAmount.divide(BigDecimal.valueOf(numberOfMonths), RoundingMode.HALF_UP);
    
        for (SimpleTransaction txn : transactions) {
            BigDecimal txnAmount = txn.getAmount();
            YearMonth txnMonth = YearMonth.from(txn.getDate());
    
            // Adjust old category's budget for the specific month (if budgets exist for the month)
            if (monthsWithBudgets.contains(txnMonth)) {
                budgetRepository.findByCategoryAndUserAndMonth(oldCategory, user, txnMonth)
                        .ifPresent(budget -> {
                            budget.setSpent(budget.getSpent() - txnAmount.doubleValue());
                            budgetRepository.save(budget);
                        });
            }
    
            // Update the transaction's category and original details
            txn.setOriginalCategory(txn.getCategory());
            txn.setOriginalAmount(txn.getAmount());
            txn.setCategory(newCategory);
            transactionRepository.save(txn);
    
            // Update or create the new category's budget for the specific month
            if (monthsWithBudgets.contains(txnMonth)) {
                budgetRepository.findByCategoryAndUserAndMonth(newCategory, user, txnMonth)
                        .ifPresentOrElse(budget -> {
                            budget.setSpent(budget.getSpent() + txnAmount.doubleValue());
                            budgetRepository.save(budget);
                        }, () -> {
                            // Calculate average for months with no existing budget
                            Budget newBudget = new Budget();
                            newBudget.setCategory(newCategory);
                            newBudget.setUser(user);
                            newBudget.setMonth(txnMonth);
                            newBudget.setSpent(txnAmount.doubleValue());
                            newBudget.setAmount(averageAmountPerMonth.doubleValue());
                            budgetRepository.save(newBudget);
                        });
            }
        }
    
        // Handle months with budgets for the old category but no transactions
        List<Budget> oldCategoryBudgetsWithoutTransactions = budgetRepository.findByCategoryAndUser(oldCategory, user)
                .stream()
                .filter(budget -> transactions.stream()
                        .map(SimpleTransaction::getDate)
                        .noneMatch(date -> YearMonth.from(date).equals(budget.getMonth())))
                .toList();
    
        for (Budget oldBudget : oldCategoryBudgetsWithoutTransactions) {
            YearMonth budgetMonth = oldBudget.getMonth();
            Optional<Budget> optionalBudget = budgetRepository.findByCategoryAndUserAndMonth(newCategory, user, budgetMonth);

            Budget budget = optionalBudget.orElseGet(() -> {
                Budget newBudget = new Budget();
                newBudget.setCategory(newCategory);
                newBudget.setUser(user);
                newBudget.setMonth(budgetMonth);
                newBudget.setSpent(0.0); // No transactions for this budget yet
                return newBudget;
            });
            
            budget.setAmount(optionalBudget.map(Budget::getAmount).orElse(averageAmountPerMonth.doubleValue()));
            budgetRepository.save(budget);
            
        }
    
        // Return the updated or newly created category
        return newCategory;
    }
    @Transactional
    public void updateTransactionDate(List<String> transactionIds, LocalDate newDate) {
        List<SimpleTransaction> transactions = transactionRepository.findAllById(transactionIds);

        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found for the provided IDs.");
        }

        for (SimpleTransaction transaction : transactions) {
            YearMonth oldMonth = YearMonth.from(transaction.getDate());
            transaction.setDate(newDate);
            transactionRepository.save(transaction);

            YearMonth newMonth = YearMonth.from(newDate);
            BigDecimal txnAmount = transaction.getAmount();

            if (!oldMonth.equals(newMonth)) {
                budgetRepository.findByCategoryAndUserAndMonth(transaction.getCategory(), transaction.getUser(), oldMonth)
                        .ifPresent(budget -> {
                            budget.setSpent(budget.getSpent() - txnAmount.doubleValue());
                            budgetRepository.save(budget);
                        });

                budgetRepository.findByCategoryAndUserAndMonth(transaction.getCategory(), transaction.getUser(), newMonth)
                        .ifPresent(budget -> {
                            budget.setSpent(budget.getSpent() + txnAmount.doubleValue());
                            budgetRepository.save(budget);
                        });
                
            }
        }
    }
    
}
