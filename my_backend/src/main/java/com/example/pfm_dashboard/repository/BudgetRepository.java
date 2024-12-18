package com.example.pfm_dashboard.repository;

import com.example.pfm_dashboard.model.Budget;
import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    @Query("SELECT b FROM Budget b WHERE b.user = :user AND b.month = :month ORDER BY b.category.name ASC")
    List<Budget> findByUserAndMonth(@Param("user") User user, @Param("month") YearMonth month);

    @Query("SELECT b FROM Budget b WHERE b.category = :category AND b.user = :user AND b.month = :month")
    Optional<Budget> findByCategoryAndUserAndMonth(
        @Param("category") Category category, 
        @Param("user") User user, 
        @Param("month") YearMonth month
    );

    @Query("SELECT b FROM Budget b WHERE b.category = :category AND b.user = :user")
    List<Budget> findByCategoryAndUser(@Param("category") Category category, @Param("user") User user);

    List<Budget> findByCategory(Category category);
    @Modifying
    @Query("UPDATE Budget b SET b.amount = :amount WHERE b.id = :budgetId")
    void updateBudgetAmount(@Param("budgetId") Long budgetId, @Param("amount") Double amount);

    @Query("SELECT DISTINCT b.month FROM Budget b WHERE b.user = :user")
    Set<YearMonth> findDistinctMonthsByUser(@Param("user") User user);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN TRUE ELSE FALSE END " +
       "FROM Budget b WHERE b.user = :user AND b.month = :month")
    boolean existsByUserAndMonth(@Param("user") User user, @Param("month") YearMonth month);



}
