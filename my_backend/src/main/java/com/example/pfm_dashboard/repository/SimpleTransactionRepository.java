package com.example.pfm_dashboard.repository;

import com.example.pfm_dashboard.model.SimpleTransaction;
import com.example.pfm_dashboard.model.Item;
import com.example.pfm_dashboard.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SimpleTransactionRepository extends JpaRepository<SimpleTransaction, String> {

    List<SimpleTransaction> findByUserId(Long userId);

    List<SimpleTransaction> findByItem(Item item);

    List<SimpleTransaction> findByUserIdOrderByDateDesc(Long userId);


    @Query("SELECT t FROM SimpleTransaction t JOIN FETCH t.item WHERE t.user.id = :userId AND t.date BETWEEN :startDate AND :endDate")
    List<SimpleTransaction> findByUserIdAndDateBetween(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT t FROM SimpleTransaction t WHERE t.user.id = :userId AND t.category = :category")
    List<SimpleTransaction> findByUserIdAndCategory(
        @Param("userId") Long userId, 
        @Param("category") Category category // Updated to use Category entity
    );

    @Query("SELECT t FROM SimpleTransaction t WHERE t.user.id = :userId AND t.category = :category AND t.date BETWEEN :startDate AND :endDate")
    List<SimpleTransaction> findByUserIdAndCategoryAndDateBetween(
        @Param("userId") Long userId, 
        @Param("category") Category category, // Updated to use Category entity
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    void deleteByTransactionId(String transactionId);

    @Query("SELECT MIN(t.date) FROM SimpleTransaction t WHERE t.user.id = :userId")
    Optional<LocalDate> findOldestTransactionDateByUserId(@Param("userId") Long userId);
}
