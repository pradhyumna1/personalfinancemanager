import React, { useState } from "react";
import { Box, Typography, TextField, Button } from "@mui/material";
import styles from "./styles/BudgetProgress.module.scss";

const BudgetProgress = ({
  spent,
  budget,
  category,
  onUpdateBudget,
  budgetId,
  onClickCategory, // Add this prop
}) => {
  const [isEditing, setIsEditing] = useState(false);
  const [editedBudget, setEditedBudget] = useState(budget);

  const calculateRemaining = (budget, spent) => {
    const remaining = budget - spent;
    return {
      text: remaining >= 0 ? "Remaining:" : "Exceeded by:",
      amount: `$${Math.abs(remaining).toFixed(2)}`,
      isRemaining: remaining >= 0, // Flag for styling
    };
  };
  

  const getProgressBarStyles = (spent, budget) => {
    if (spent > budget) {
      return { backgroundColor: "#dc2626", width: "100%" }; // Exceeded budget (red)
    }
    return { backgroundColor: "#16a34a", width: `${(spent / budget) * 100}%` }; // Within budget (green)
  };

  const handleEditToggle = () => {
    setIsEditing(!isEditing);
    setEditedBudget(budget); // Reset to the current budget value
  };

  const handleSave = () => {
    if (editedBudget >= 0) {
      onUpdateBudget(budgetId, editedBudget); // Trigger budget update
      setIsEditing(false); // Exit edit mode
    }
  };

  const handleClick = () => {
    if (!isEditing && onClickCategory) {
      onClickCategory(category); // Notify the parent when clicked
    }
  };
  const remainingInfo = calculateRemaining(budget, spent);

  return (
    <Box
      className={styles.budgetProgress}
      onClick={handleClick} // Trigger click handler
      style={{ cursor: "pointer" }} // Ensure cursor changes to show clickability
    >
      <Typography className={styles.remaining}>
        {remainingInfo.text}{" "}
        <span
          style={{
            color: remainingInfo.isRemaining ? "#10b981" : "#dc2626", // Green if remaining, red if exceeded
          }}
        >
          {remainingInfo.amount}
        </span>
      </Typography>
      <Box className={styles.progressWrapper}>
        <Typography className={styles.progressLeft}>${spent.toFixed(2)}</Typography>
        <Box
          className={styles.progressBar}
          onClick={(e) => {
            e.stopPropagation(); // Prevent parent clicks when clicking the progress bar
          }}
        >
          <Box
            className={styles.progressFill}
            style={getProgressBarStyles(spent, budget)}
          ></Box>
        </Box>
        <Box className={styles.progressRight}>
          {isEditing ? (
            <>
              <TextField
                type="number"
                value={editedBudget}
                onChange={(e) => setEditedBudget(parseFloat(e.target.value))}
                size="small"
                className={styles.budgetInput}
              />
              <Button
                onClick={handleSave}
                variant="contained"
                size="small"
                className={styles.saveButton}
              >
                Save
              </Button>
            </>
          ) : (
            <Typography
              className={styles.budgetValue}
              onClick={(e) => {
                e.stopPropagation(); // Prevent triggering the parent onClick when toggling edit
                handleEditToggle();
              }}
              style={{ cursor: "pointer" }}
            >
              ${budget.toFixed(2)}
            </Typography>
          )}
        </Box>
      </Box>
    </Box>
  );
};

export default BudgetProgress;
