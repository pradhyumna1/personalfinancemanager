import React, { useState } from "react";
import {
  Box,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Typography,
} from "@mui/material";
import BudgetProgress from "./BudgetProgress";
import BudgetTransactionsWidget from "./BudgetTransactionsWidget";
import styles from "./styles/BudgetTable.module.scss";

const formatCategory = (category) => {
  if (!category) return "Uncategorized"; // Fallback for undefined categories
  return category.replace(/_/g, " ").replace(/\b\w/g, (char) => char.toUpperCase());
};

const BudgetTable = ({
  budgets,
  onUpdateBudget,
  username,
  onTransactionUpdate,
  onCategoryUpdate,
  onCategorySelect, // New prop for passing selected category back to BudgetsPage
}) => {
  const [selectedCategory, setSelectedCategory] = useState(null); // Tracks the currently selected category

  // Handles when a category is clicked in the table
  const handleCategoryClick = (category) => {
    if (category) {
      console.log("Category clicked:", category);
      setSelectedCategory(category);
      if (onCategorySelect) {
        onCategorySelect(category); // Pass the selected category to the parent component (BudgetsPage)
      }
    }
  };

  // Handles closing the transactions widget
  const handleCloseWidget = () => {
    setSelectedCategory(null);
    if (onCategorySelect) {
      onCategorySelect(null); // Clear the selected category in the parent
    }
  };

  // Handles transaction updates passed to the parent
  const handleTransactionUpdate = (transactionId, difference, category, transactionMonth) => {
    if (onTransactionUpdate) {
      console.log("Transaction Month passed to BudgetsPage:", transactionMonth);
      console.log("Category passed to BudgetsPage:", category);
      onTransactionUpdate(transactionId, difference, category, transactionMonth);
    }
  };

  // Handles category changes passed to the parent
  const handleChangeCategory = (transactionIds, newCategory) => {
    if (onCategoryUpdate) {
      onCategoryUpdate(transactionIds, newCategory);
    }
  };

  return (
    <Box className={styles.budgetTable}>
      <Typography variant="h6" className={styles.title}>
        Budget Overview
      </Typography>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell
              style={{
                color: "#ffffff",
                fontWeight: "bold",
                width: "10%",
                padding: "8px",
              }}
            >
              Category
            </TableCell>
            <TableCell
              style={{
                color: "#ffffff",
                fontWeight: "bold",
                width: "90%",
                padding: "8px",
              }}
            ></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {budgets
            .slice() // Clone the budgets array to avoid mutation
            .sort((a, b) => a.category.name.localeCompare(b.category.name)) // Sort categories alphabetically
            .map((budget) => (
              <TableRow key={budget.id}>
                <TableCell
                  style={{
                    color: "#ffffff",
                    fontWeight: "bold",
                    padding: "8px",
                  }}
                >
                  {formatCategory(budget.category.name)} {/* Format category for display */}
                </TableCell>
                <TableCell style={{ padding: "8px" }}>
                  <BudgetProgress
                    spent={budget.spent} // Pass the current spent amount
                    budget={budget.amount} // Pass the budgeted amount
                    category={budget.category.name} // Pass the category name for the widget
                    onUpdateBudget={onUpdateBudget} // Callback for updating budgets
                    budgetId={budget.id} // Pass the unique budget ID
                    onClickCategory={handleCategoryClick} // Handle category clicks
                  />
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>

      {/* Render the transactions widget if a category is selected */}
      {selectedCategory && (
        <BudgetTransactionsWidget
          category={selectedCategory} // Pass the selected category name
          username={username} // Pass the username
          onClose={handleCloseWidget} // Handle widget close
          onSpentUpdate={handleTransactionUpdate} // Handle transaction updates
          onCategoryUpdate={handleChangeCategory} // Handle category changes
        />
      )}
    </Box>
  );
};

export default BudgetTable;
