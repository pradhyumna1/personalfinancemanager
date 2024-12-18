package com.example.pfm_dashboard.repository;

import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;


public interface CategoryRepository extends JpaRepository<Category, Long> {

    @Query("SELECT c FROM Category c WHERE c.name = :name AND c.user = :user")
    Optional<Category> findByNameAndUser(@Param("name") String name, @Param("user") User user);

    List<Category> findByUser(User user);

    boolean existsByNameAndUser(String name, User user);

    Optional<Category> findByName(String name); 
    
    @Query("SELECT c FROM Category c WHERE c.user = :user")
    List<Category> findAllByUser(User user);

    @Query("SELECT c FROM Category c WHERE LOWER(c.name) = LOWER(:name) AND c.user.id = :userId")
    Optional<Category> findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);
    
}
