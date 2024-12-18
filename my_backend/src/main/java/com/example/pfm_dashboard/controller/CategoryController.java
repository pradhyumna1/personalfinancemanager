package com.example.pfm_dashboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.pfm_dashboard.service.CommonService;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CommonService commonService;

    @Autowired
    public CategoryController(CommonService commonService) {
        this.commonService = commonService;
    }

    @DeleteMapping("/delete-category")
    public ResponseEntity<String> deleteCategory(@RequestParam String username, @RequestBody Map<String, String> requestBody) {
        String categoryName = requestBody.get("categoryName");

        if (categoryName == null || categoryName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Category name is required.");
        }

        try {
            // Step 1: Reassign transactions and propagate updates to budgets
            commonService.reassignTransactions(username, categoryName);

            // Step 2: Delete the category and its associated budgets
            commonService.deleteCategoryAndBudgets(username, categoryName);

            return ResponseEntity.ok("Category '" + categoryName + "' deleted successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while deleting the category.");
        }
    }
}
