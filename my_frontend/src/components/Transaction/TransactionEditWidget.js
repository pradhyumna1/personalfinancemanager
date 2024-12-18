import React, { useState } from "react";
import { Box, Typography, Button, TextField } from "@mui/material";
import CategoryChip from "../Category/CategoryChip";
import CategoryPicker from "../Category/CategoryPicker";
import DatePicker from "../Date/DatePicker";
import styles from "./styles/TransactionEditWidget.module.scss";

const getBankLogo = (bankName) => {
  const logoPath = `/BankLogos/${bankName
    ?.toLowerCase()
    .replace(/\s+/g, "_")
    .replace(/[^a-z0-9_]/g, "")}.png`;
  return logoPath;
};

const formatCategory = (category) => {
  if (!category) return "Uncategorized";
  return category.replace(/_/g, " ").replace(/\b\w/g, (char) => char.toUpperCase());
};

const TransactionEditWidget = ({
  transaction,
  distinctCategories,
  onClose,
  onTransactionUpdate,
}) => {
  const [editableAmount, setEditableAmount] = useState(transaction.amount?.toString() || "0.00");
  const [editableMerchant, setEditableMerchant] = useState(transaction.merchantName || "");
  const [editableCategory, setEditableCategory] = useState(transaction.category);
  const [showCategoryPicker, setShowCategoryPicker] = useState(false);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [loading, setLoading] = useState(false);
  const [editableDate, setEditableDate] = useState(
    new Date(
      Date.UTC(
        new Date(transaction.date).getUTCFullYear(),
        new Date(transaction.date).getUTCMonth(),
        new Date(transaction.date).getUTCDate()
      )
    )
  );  
  const handleCategorySelect = (category) => {
    setEditableCategory(category);
    setShowCategoryPicker(false);
  };

  const handleSave = async () => {
    setLoading(true);
    try {
      if (parseFloat(editableAmount) !== transaction.amount) {
        await fetch(`${process.env.REACT_APP_API_URL}/update-txn-amount`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            transactionIds: [transaction.transactionId],
            newAmount: parseFloat(editableAmount),
          }),
        });
      }

      if (editableMerchant !== transaction.merchantName) {
        await fetch(`${process.env.REACT_APP_API_URL}/update-txn-merchant`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            transactionIds: [transaction.transactionId],
            newMerchantName: editableMerchant,
          }),
        });
      }

      if (editableDate.toISOString().split("T")[0] !== transaction.date) {
        await fetch(`${process.env.REACT_APP_API_URL}/update-txn-date`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            transactionIds: [transaction.transactionId],
            newDate: editableDate.toISOString().split("T")[0],
          }),
        });
      }

      if (editableCategory.name !== transaction.category.name) {
        await fetch(`${process.env.REACT_APP_API_URL}/update-txn-category`, {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            transactionIds: [transaction.transactionId],
            newCategory: editableCategory.name,
          }),
        });
      }

      onTransactionUpdate(transaction.transactionId, {
        amount: parseFloat(editableAmount),
        merchantName: editableMerchant,
        date: editableDate.toISOString().split("T")[0],
        category: editableCategory,
      });

      setLoading(false);
      onClose();
    } catch (error) {
      console.error("Error saving transaction updates:", error);
      setLoading(false);
    }
  };
  console.log("Editable Date (UTC):", editableDate.toISOString());


  return (
    <Box className={styles.overlay}>
      <Box className={styles.widget}>
        <Typography variant="h6" className={styles.title}>
          Edit Transaction
        </Typography>
        <Button
          variant="contained"
          onClick={onClose}
          className={styles.closeButton}
          disabled={loading}
        >
          Close
        </Button>
        <Box className={styles.transactionDetails}>
          <Typography
            className={styles.date}
            onClick={() => setShowDatePicker((prev) => !prev)}
            style={{ cursor: "pointer", textDecoration: "underline", marginTop: "20px" }}
          >
            {editableDate.toLocaleDateString("default", {
              weekday: "long",
              month: "short",
              day: "numeric",
              year: "numeric",
              timeZone: "UTC",
            })}
          </Typography>
          {showDatePicker && (
            <DatePicker
              selectedDate={editableDate}
              onDateChange={(date) => setEditableDate(date)}
              onClose={() => setShowDatePicker(false)}
            />
          )}
          <TextField
            value={editableMerchant}
            onChange={(e) => setEditableMerchant(e.target.value)}
            className={styles.merchantInput}
            InputProps={{
              inputProps: { style: { textAlign: "center", fontSize: "1.2rem", color: "#10b981" } },
            }}
          />
        </Box>
        <Box className={styles.amountSection}>
          <TextField
            value={editableAmount}
            onChange={(e) => setEditableAmount(e.target.value)}
            className={styles.amountInput}
            InputProps={{
              inputProps: { style: { textAlign: "center", fontSize: "1.5rem" } },
            }}
          />
        </Box>
        <Box className={styles.accountInfo}>
          <img
            src={getBankLogo(transaction.bankName)}
            alt={`${transaction.bankName} logo`}
            className={styles.bankLogo}
            onError={(e) => {
              e.target.onerror = null;
              e.target.style.display = "none";
            }}
          />
          <Typography className={styles.accountName}>
            {transaction.accountName}
          </Typography>
        </Box>
        <Box className={styles.categorySection}>
          <Typography className={styles.categoryTitle}>Category</Typography>
          <CategoryChip
            categoryName={formatCategory(editableCategory?.name)}
            categoryColor={editableCategory?.color || "#6b7280"}
            onClick={() => setShowCategoryPicker((prev) => !prev)}
          />
        </Box>
        {showCategoryPicker && (
          <CategoryPicker
            categories={distinctCategories}
            onCategorySelect={handleCategorySelect}
            onClose={() => setShowCategoryPicker(false)}
          />
        )}
        <Box className={styles.actions}>
          <Button
            variant="contained"
            onClick={handleSave}
            className={styles.saveButton}
            style={{ marginTop: "30px" }}
            disabled={loading}
          >
            {loading ? "Saving..." : "Save"}
          </Button>
        </Box>
      </Box>
    </Box>
  );
};

export default TransactionEditWidget;
