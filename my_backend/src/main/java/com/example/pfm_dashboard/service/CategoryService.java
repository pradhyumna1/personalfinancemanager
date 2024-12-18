package com.example.pfm_dashboard.service;

import com.example.pfm_dashboard.model.Category;
import com.example.pfm_dashboard.model.User;
import com.example.pfm_dashboard.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Arrays;


@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;


    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category findOrCreateCategory(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
                .orElseGet(() -> {
                    Category newCategory = new Category();
                    newCategory.setName(name);
                    newCategory.setUser(user);
                    return categoryRepository.save(newCategory);
                });
    }
    public Optional<Category> findCategoryByNameAndUser(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user);
    }

    public boolean doesCategoryExist(String name, User user) {
        return categoryRepository.existsByNameAndUser(name, user);
    }

    public List<Category> getCategoriesForUser(User user) {
        return categoryRepository.findByUser(user);
    }


    public void deleteCategory(String name, User user) {
        categoryRepository.findByNameAndUser(name, user)
                .ifPresent(categoryRepository::delete);
    }


    public List<Category> findOrCreateCategories(List<String> categoryNames, User user) {
        return categoryNames.stream()
                .map(name -> findOrCreateCategory(name, user))
                .toList();
    }
    
    public Category createCategory(String name, User user) {
        String[] colors = { "#10b981", "#8c52ff", "#f97316", "#3b82f6", "#e11d48", "#14b8a6", "#facc15", "#6366f1", "#ec4899" };
        
        // Fetch all existing colors for the user's categories
        List<String> existingColors = categoryRepository.findAllByUser(user).stream()
                .map(Category::getColor)
                .toList();
    
        // Filter out the already used colors
        List<String> availableColors = Arrays.stream(colors)
                .filter(color -> !existingColors.contains(color))
                .toList();
    
        // If all colors have been used, reset the pool (allow reuse)
        String assignedColor;
        if (availableColors.isEmpty()) {
            assignedColor = colors[new Random().nextInt(colors.length)];
        } else {
            assignedColor = availableColors.get(new Random().nextInt(availableColors.size()));
        }
    
        // Create and save the new category
        Category category = new Category(name, assignedColor, user);
        return categoryRepository.save(category);
    }
    
}
