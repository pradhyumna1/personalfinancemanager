import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Button,
  Checkbox,
  Modal,
  TextField,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
} from "@mui/material";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import TransactionEditWidget from "../Transaction/TransactionEditWidget";
import styles from "./styles/BudgetTransactionsWidget.module.scss";

const sxStyles = {
  modalContent: {
    backgroundColor: "#1e293b",
    color: "#ffffff",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.3)",
  },
  modalTextField: {
    "& .MuiOutlinedInput-root": {
      backgroundColor: "#1f2937",
      color: "#ffffff",
      "& fieldset": { borderColor: "#8c52ff" },
      "&:hover fieldset": { borderColor: "#8c52ff" },
      "&.Mui-focused fieldset": { borderColor: "#8c52ff" },
    },
    "& input": { color: "#ffffff" },
    "&::placeholder": { color: "#a3a3a3" },
  },
  selectButton: {
    backgroundColor: "#8c52ff",
    color: "#fff",
    "&:hover": { backgroundColor: "#10b981" },
    marginLeft: "auto",
  },
  spacedButton: {
    backgroundColor: "#dc2626",
    marginTop: "10px",
    marginBottom: "15px",
  },
  deleteButton: {
    backgroundColor: "#dc2626",
    color: "#fff",
    "&:hover": { backgroundColor: "#ff4d4d" },
  },
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString("en-US", { timeZone: "UTC" });
};

const formatCategory = (category) => {
  if (!category) return "Uncategorized";
  return category.replace(/_/g, " ").replace(/\b\w/g, (char) => char.toUpperCase());
};

const getBankLogo = (bankName) => {
  const logoPath = `/BankLogos/${bankName
    ?.toLowerCase()
    .replace(/\s+/g, "_")
    .replace(/[^a-z0-9_]/g, "")}.png`;
  return logoPath;
};

const BudgetTransactionsWidget = ({
  category,
  username,
  onClose,
  onSpentUpdate,
  onCategoryDeleted,
}) => {
  const [transactions, setTransactions] = useState({});
  const [error, setError] = useState(null);
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newCategory, setNewCategory] = useState("");
  const [menuAnchor, setMenuAnchor] = useState(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/budgets/transactions-by-category?username=${username}&category=${category}`
        );

        if (response.ok) {
          const data = await response.json();
          Object.keys(data).forEach((month) => {
            data[month] = data[month]
              .map((transaction) => ({
                ...transaction,
                amount: parseFloat(transaction.amount) || 0,
              }))
              .sort((a, b) => new Date(b.date) - new Date(a.date));
          });
          setTransactions(data);
        } else {
          setError("Failed to fetch transactions.");
        }
      } catch (err) {
        console.error("Error fetching transactions:", err);
        setError("Error fetching transactions.");
      }
    };

    if (category && username) fetchTransactions();
  }, [category, username]);

  const handleTransactionUpdate = (transactionId, updatedTransaction) => {
    let transactionMonth = null;

    Object.entries(transactions).forEach(([month, txns]) => {
      const txn = txns.find((txn) => txn.transactionId === transactionId);
      if (txn) transactionMonth = month;
    });

    setTransactions((prev) => {
      const updated = Object.fromEntries(
        Object.entries(prev).map(([month, txns]) => [
          month,
          txns.map((txn) =>
            txn.transactionId === transactionId ? { ...txn, ...updatedTransaction } : txn
          ),
        ])
      );
      return updated;
    });

    if (onSpentUpdate) {
      const amountDifference = updatedTransaction.amount - transactions[transactionMonth].find(
        (txn) => txn.transactionId === transactionId
      ).amount;
      onSpentUpdate(transactionId, amountDifference, category, transactionMonth);
    }
  };

  const handleCheckboxChange = (transactionId) => {
    setSelectedTransactions((prevSelected) =>
      prevSelected.includes(transactionId)
        ? prevSelected.filter((id) => id !== transactionId)
        : [...prevSelected, transactionId]
    );
  };

  const openModal = () => setIsModalOpen(true);
  const closeModal = () => {
    setIsModalOpen(false);
    setNewCategory("");
  };

  const applyCategoryChange = async () => {
    const formattedCategory = newCategory.trim().replace(/ /g, "_").toUpperCase();
    if (!formattedCategory) {
      alert("Please enter a valid category.");
      return;
    }
    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/update-txn-category`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ transactionIds: selectedTransactions, newCategory: formattedCategory }),
      });
      if (response.ok) {
        setSelectedTransactions([]);
        closeModal();
      } else {
        console.error("Failed to update transaction category");
      }
    } catch (err) {
      console.error("Error updating transaction category:", err);
    }
  };

  const handleMenuClick = (event) => setMenuAnchor(event.currentTarget);
  const handleMenuClose = () => setMenuAnchor(null);

  const handleDeleteClick = () => {
    setMenuAnchor(null);
    setIsDeleteDialogOpen(true);
  };

  const handleDeleteCategory = async () => {
    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/delete-category?username=${username}`, {
        method: "DELETE",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ categoryName: category }),
      });

      if (response.ok) {
        setIsDeleteDialogOpen(false);
        if (onCategoryDeleted) {
          onCategoryDeleted(category);
        }
      } else {
        console.error("Failed to delete category");
      }
    } catch (err) {
      console.error("Error deleting category:", err);
    }
  };

  const formatMonth = (month) => {
    const [year, monthIndex] = month.split("-");
    const date = new Date(year, monthIndex - 1);
    return date.toLocaleString("default", { month: "long", timeZone: "UTC" });
  };

  return (
    <Box className={styles.overlay}>
      <Box className={styles.widget}>
        <Box className={styles.widgetHeader}>
          <Typography variant="h6" className={styles.title}>
            Transactions - {formatCategory(category)}
          </Typography>
          <IconButton onClick={handleMenuClick}>
            <MoreVertIcon />
          </IconButton>
          <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={handleMenuClose}>
            <MenuItem onClick={handleDeleteClick} sx={{ color: "#dc2626" }}>
              Delete Category
            </MenuItem>
          </Menu>
        </Box>
        <Box style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <Button variant="contained" onClick={onClose} className={styles.closeButton} sx={sxStyles.spacedButton}>
            Close
          </Button>
          <Button
            variant="contained"
            className={styles.selectTransactionsButton}
            sx={sxStyles.selectButton}
            onClick={() => setIsSelectionMode((prev) => !prev)}
          >
            {isSelectionMode ? "Exit Selection Mode" : "Select Transactions"}
          </Button>
        </Box>
        {isSelectionMode && selectedTransactions.length > 0 && (
          <Button variant="contained" className={styles.changeCategoryButton} onClick={openModal}>
            Change Category ({selectedTransactions.length} Selected)
          </Button>
        )}
        {error && <Typography className={styles.error}>{error}</Typography>}
        {Object.keys(transactions).length > 0 ? (
          <Box>
            {Object.entries(transactions)
              .sort(([a], [b]) => new Date(b) - new Date(a))
              .map(([month, transactions]) => (
                <Box key={month} className={styles.monthGroup}>
                  <Typography variant="h6" className={styles.monthTitle}>
                    {formatMonth(month)}
                  </Typography>
                  <Box>
                    {transactions.map((transaction) => (
                      <Box
                        key={transaction.transactionId}
                        className={styles.transaction}
                        onClick={
                          isSelectionMode
                            ? () => handleCheckboxChange(transaction.transactionId)
                            : () => setSelectedTransaction(transaction)
                        }
                        style={{ cursor: "pointer" }}
                      >
                        {isSelectionMode && (
                          <Checkbox
                            checked={selectedTransactions.includes(transaction.transactionId)}
                            className={styles.checkbox}
                          />
                        )}
                        <Typography className={styles.transactionDate}>
                          {new Date(transaction.date).toLocaleDateString("en-US", {
                            month: "short",
                            day: "numeric",
                            timeZone: "UTC",
                          })}
                        </Typography>
                        <Typography className={styles.merchantName}>
                          {transaction.merchantName}
                        </Typography>
                        <Box className={styles.accountInfo}>
                          <img
                            src={getBankLogo(transaction.bankName)}
                            alt={`${transaction.bankName} logo`}
                            className={styles.bankLogo}
                            onError={(e) => (e.target.style.display = "none")}
                          />
                          <Typography className={styles.accountName}>
                            {transaction.accountName}
                          </Typography>
                        </Box>
                        <Typography
                          className={styles.amount}
                          style={{
                            color: transaction.amount < 0 ? "#dc2626" : "#10b981",
                          }}
                        >
                          ${transaction.amount.toFixed(2)}
                        </Typography>
                      </Box>
                    ))}
                  </Box>
                </Box>
              ))}
          </Box>
        ) : (
          <Typography className={styles.noData}>
            No transactions available for this category.
          </Typography>
        )}
      </Box>

      {selectedTransaction && (
        <TransactionEditWidget
          transaction={selectedTransaction}
          onClose={() => setSelectedTransaction(null)}
          onTransactionUpdate={(id, updatedTransaction) =>
            handleTransactionUpdate(id, updatedTransaction)
          }
        />
      )}

      <Modal open={isModalOpen} onClose={closeModal}>
        <Box className={styles.modalContent} sx={sxStyles.modalContent}>
          <Button variant="h6">Change Category</Button>
          <TextField
            placeholder="Enter new category"
            value={newCategory}
            onChange={(e) => setNewCategory(e.target.value)}
            fullWidth
            sx={sxStyles.modalTextField}
          />
          <Button onClick={applyCategoryChange} variant="contained" className={styles.applyButton}>
            Apply
          </Button>
        </Box>
      </Modal>

      <Dialog open={isDeleteDialogOpen} onClose={() => setIsDeleteDialogOpen(false)}>
        <DialogTitle>Delete Category</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the category "{formatCategory(category)}"? All associated budgets will
            be deleted, and all transactions will be reassigned to the category "Other."
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setIsDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteCategory} sx={sxStyles.deleteButton}>
            Confirm
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default BudgetTransactionsWidget;
