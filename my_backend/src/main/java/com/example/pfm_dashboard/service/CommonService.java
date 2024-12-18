package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.dto.TransactionDTO;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.CategoryRepository;
import com.example.pfm_dashboard.repository.SimpleTransactionRepository;
import com.example.pfm_dashboard.repository.UserRepository;
import com.example.pfm_dashboard.repository.BudgetRepository;
import com.example.pfm_dashboard.model.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CommonService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final SimpleTransactionRepository transactionRepository;
    private final TransactionsService transactionsService;
    private final BudgetService budgetService;
    private final CategoryService categoryService;
    private final BudgetRepository budgetRepository;

    @Autowired
    public CommonService(UserRepository userRepository, CategoryRepository categoryRepository,
                         SimpleTransactionRepository transactionRepository, TransactionsService transactionsService,
                         BudgetService budgetService, CategoryService categoryService,
                         BudgetRepository budgetRepository) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.transactionRepository = transactionRepository;
        this.transactionsService = transactionsService;
        this.budgetService = budgetService;
        this.categoryService = categoryService;
        this.budgetRepository = budgetRepository;
    }

    /**
     * Step 1: Reassign transactions from the category being deleted to the "Other" category
     * and update budgets accordingly.
     */
   @Transactional
    public void reassignTransactions(String username, String categoryName) {
        System.out.println("Reassigning transactions and budgets for user: " + username + " and category: " + categoryName);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long userId = user.getId();
        Category categoryToDelete = categoryRepository.findByNameAndUserId(categoryName.toLowerCase().trim(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

        System.out.println("Category to delete found: " + categoryToDelete.getName());

        // Check if "Other" category exists; if not, create it
        Category otherCategory = categoryRepository.findByNameAndUser("other", user)
                .orElseGet(() -> categoryService.createCategory("other", user));
        System.out.println("'Other' category resolved: " + otherCategory.getName());

        // Step 1: Handle budgets for months with transactions
        List<SimpleTransaction> transactionsToReassign = transactionRepository.findByUserIdAndCategory(userId, categoryToDelete);
        Set<YearMonth> transactionMonths = transactionsToReassign.stream()
                .map(txn -> YearMonth.from(txn.getDate()))
                .collect(Collectors.toSet());

        transactionMonths.forEach(month -> {
            // Ensure there are existing budgets for the month
            if (budgetRepository.existsByUserAndMonth(user, month)) {
                // Get the budget for the category-to-delete for this month
                budgetRepository.findByCategoryAndUserAndMonth(categoryToDelete, user, month).ifPresent(budgetToDelete -> {
                    // Check if a budget for "Other" already exists
                    budgetRepository.findByCategoryAndUserAndMonth(otherCategory, user, month)
                            .ifPresentOrElse(otherBudget -> {
                                // Add the amount to the existing "Other" budget
                                otherBudget.setAmount(otherBudget.getAmount() + budgetToDelete.getAmount());
                                otherBudget.setSpent(otherBudget.getSpent() + budgetToDelete.getSpent());
                                budgetRepository.save(otherBudget);
                            }, () -> {
                                // Create a new budget for "Other" with the same amount as the category-to-delete budget
                                Budget newBudget = new Budget();
                                newBudget.setCategory(otherCategory);
                                newBudget.setUser(user);
                                newBudget.setMonth(month);
                                newBudget.setAmount(budgetToDelete.getAmount());
                                newBudget.setSpent(budgetToDelete.getSpent());
                                budgetRepository.save(newBudget);
                            });
                    System.out.println("Handled budget for month: " + month + " and category: " + categoryToDelete.getName());
                });
            }
        });

        // Step 2: Handle remaining budgets for months without transactions
        List<Budget> remainingBudgets = budgetRepository.findByCategoryAndUser(categoryToDelete, user);
        remainingBudgets.forEach(budgetToDelete -> {
            YearMonth budgetMonth = budgetToDelete.getMonth();

            // Ensure we are not double-counting months already processed
            if (!transactionMonths.contains(budgetMonth)) {
                budgetRepository.findByCategoryAndUserAndMonth(otherCategory, user, budgetMonth)
                        .ifPresentOrElse(otherBudget -> {
                            // Add the amount to the existing "Other" budget
                            otherBudget.setAmount(otherBudget.getAmount() + budgetToDelete.getAmount());
                            otherBudget.setSpent(otherBudget.getSpent() + budgetToDelete.getSpent());
                            budgetRepository.save(otherBudget);
                        }, () -> {
                            // Create a new budget for "Other"
                            Budget newBudget = new Budget();
                            newBudget.setCategory(otherCategory);
                            newBudget.setUser(user);
                            newBudget.setMonth(budgetMonth);
                            newBudget.setAmount(budgetToDelete.getAmount());
                            newBudget.setSpent(budgetToDelete.getSpent());
                            budgetRepository.save(newBudget);
                        });
                System.out.println("Handled remaining budget for month: " + budgetMonth + " and category: " + categoryToDelete.getName());
            }
        });

        // Step 3: Reassign transactions
        transactionsToReassign.forEach(txn -> {
            txn.setOriginalCategory(otherCategory);
            txn.setCategory(otherCategory);
            transactionRepository.save(txn);
            System.out.println("Reassigned transaction ID: " + txn.getTransactionId());
        });

        System.out.println("Reassigned all transactions and budgets for category: " + categoryName);
    }


    /**
     * Step 2: Delete budgets and the category itself.
     */
    @Transactional
    public void deleteCategoryAndBudgets(String username, String categoryName) {
        System.out.println("Deleting category and associated budgets for user: " + username + " and category: " + categoryName);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Long userId = user.getId();
        Category categoryToDelete = categoryRepository.findByNameAndUserId(categoryName.toLowerCase().trim(), userId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryName));

        budgetService.deleteAssociatedBudgets(categoryName);
        categoryRepository.delete(categoryToDelete);

        System.out.println("Category '" + categoryName + "' deleted successfully.");
    }

    /**
     * General method to update transaction categories and propagate changes to budgets.
     */
    @Transactional
    public Category updateTransactionCategory(List<String> transactionIds, String newCategoryName) {
        List<SimpleTransaction> transactions = transactionRepository.findAllById(transactionIds);
        if (transactions.isEmpty()) {
            throw new IllegalArgumentException("No valid transactions found.");
        }
        User user = transactions.get(0).getUser();

        // Find or create the new category
        Category newCategory = categoryRepository.findByNameAndUser(newCategoryName.toLowerCase(), user)
                .orElseGet(() -> categoryService.createCategory(newCategoryName.toLowerCase(), user));

        for (SimpleTransaction txn : transactions) {
            BigDecimal txnAmount = txn.getAmount();
            YearMonth txnMonth = YearMonth.from(txn.getDate());
            Category oldCategory = txn.getCategory();

            // Adjust old category's budget
            budgetRepository.findByCategoryAndUserAndMonth(oldCategory, user, txnMonth)
                    .ifPresent(budget -> {
                        budget.setSpent(budget.getSpent() - txnAmount.doubleValue());
                        budgetRepository.save(budget);
                    });

            // Update transaction's category
            txn.setOriginalCategory(txn.getCategory());
            txn.setCategory(newCategory);
            transactionRepository.save(txn);

            // Update new category's budget
            budgetRepository.findByCategoryAndUserAndMonth(newCategory, user, txnMonth)
                    .ifPresentOrElse(budget -> {
                        budget.setSpent(budget.getSpent() + txnAmount.doubleValue());
                        budgetRepository.save(budget);
                    }, () -> {
                        Budget newBudget = new Budget();
                        newBudget.setCategory(newCategory);
                        newBudget.setUser(user);
                        newBudget.setMonth(txnMonth);
                        newBudget.setSpent(txnAmount.doubleValue());
                        budgetRepository.save(newBudget);
                    });
        }

        return newCategory;
    }
}
