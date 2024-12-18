import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  TextField,
  Button,
  ToggleButton,
  ToggleButtonGroup,
  Select,
  MenuItem,
  IconButton,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import styles from "./styles/AddTransactionWidget.module.scss";

const sxStyles = {
  container: {
    position: "fixed",
    top: "50%",
    left: "50%",
    transform: "translate(-50%, -50%)",
    backgroundColor: "#1e293b",
    color: "#ffffff",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.3)",
    maxWidth: "400px",
    width: "100%",
    zIndex: 1000,
  },
  overlay: {
    position: "fixed",
    top: 0,
    left: 0,
    width: "100%",
    height: "100%",
    backgroundColor: "rgba(0, 0, 0, 0.6)",
    zIndex: 999,
  },
  closeButton: {
    position: "absolute",
    top: "10px",
    right: "10px",
    color: "#ffffff",
  },
  inputField: {
    marginBottom: "15px",
    "& .MuiOutlinedInput-root": {
      backgroundColor: "#1f2937",
      color: "#ffffff",
      "& fieldset": { borderColor: "#8c52ff" },
      "&:hover fieldset": { borderColor: "#10b981" },
      "&.Mui-focused fieldset": { borderColor: "#10b981" },
    },
    "& input": { color: "#ffffff" },
  },
  toggleButtonGroup: {
    display: "flex",
    justifyContent: "space-between",
    marginBottom: "15px",
  },
  submitButton: {
    backgroundColor: "#8c52ff",
    color: "#ffffff",
    fontWeight: "bold",
    textTransform: "uppercase",
    "&:hover": { backgroundColor: "#10b981" },
  },
};

const AddTransactionWidget = ({
  username,
  categories,
  onClose,
  onTransactionAdded,
}) => {
  const [merchantName, setMerchantName] = useState("");
  const [amount, setAmount] = useState("");
  const [isIncome, setIsIncome] = useState(false);
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null); // Store the full account object
  const [selectedCategory, setSelectedCategory] = useState("");
  const [newCategory, setNewCategory] = useState("");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));

  useEffect(() => {
    const fetchAccounts = async () => {
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/accounts/user/${username}/accounts-with-banks`
        );
        if (response.ok) {
          const data = await response.json();
          setAccounts(data);
        } else {
          console.error("Failed to fetch accounts.");
        }
      } catch (err) {
        console.error("Error fetching accounts:", err);
      }
    };

    fetchAccounts();
  }, [username]);

  const handleSubmit = async () => {
    if (!merchantName || !amount || !selectedAccount || !(selectedCategory || newCategory) || !date) {
      alert("Please fill out all fields.");
      return;
    }

    const transactionAmount = isIncome
      ? `-${Math.abs(amount)}`
      : amount;

    const categoryToUse = newCategory
      ? {
          name: newCategory.trim().replace(/ /g, "_").toUpperCase(),
          color: "#10b981",
        }
      : { name: selectedCategory };

    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/add-transaction`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            username,
            merchantName,
            amount: parseFloat(transactionAmount),
            accountName: selectedAccount.accountName, // Use accountName from the JSON
            category: categoryToUse.name,
            date,
          }),
        }
      );

      if (response.ok) {
        const newTransaction = await response.json();
        onTransactionAdded(newTransaction);
        onClose();
      } else {
        console.error("Failed to add transaction.");
      }
    } catch (err) {
      console.error("Error adding transaction:", err);
    }
  };

  return (
    <>
      <Box sx={sxStyles.overlay} onClick={onClose} />
      <Box sx={sxStyles.container} className={styles.widget}>
        <IconButton sx={sxStyles.closeButton} onClick={onClose}>
          <CloseIcon />
        </IconButton>

        <Typography variant="h6" className={styles.title}>
          Add Transaction
        </Typography>

        <TextField
          label="Merchant Name"
          value={merchantName}
          onChange={(e) => setMerchantName(e.target.value)}
          fullWidth
          sx={sxStyles.inputField}
        />

        <TextField
          label="Amount"
          type="number"
          value={amount}
          onChange={(e) => setAmount(e.target.value)}
          fullWidth
          sx={{
            ...sxStyles.inputField,
            "& input": {
              color: isIncome ? "#10b981" : "#ffffff",
              fontWeight: "bold",
            },
          }}
          InputProps={{
            startAdornment: <span style={{ color: isIncome ? "#10b981" : "#ffffff" }}>$</span>,
          }}
        />

        <ToggleButtonGroup
          value={isIncome}
          exclusive
          onChange={(e, newValue) => setIsIncome(newValue)}
          sx={sxStyles.toggleButtonGroup}
        >
          <ToggleButton value={false}>Expense</ToggleButton>
          <ToggleButton value={true}>Income</ToggleButton>
        </ToggleButtonGroup>

        <Select
          value={
            selectedAccount
              ? `${selectedAccount.accountName} - ${selectedAccount.bankName}`
              : ""
          }
          onChange={(e) =>
            setSelectedAccount(
              accounts.find(
                (account) =>
                  `${account.accountName} - ${account.bankName}` === e.target.value
              )
            )
          }
          displayEmpty
          fullWidth
          sx={sxStyles.inputField}
        >
          <MenuItem value="" disabled>
            Select Account
          </MenuItem>
          {accounts.map((account, index) => (
            <MenuItem
              key={index}
              value={`${account.accountName} - ${account.bankName}`}
            >
              {`${account.accountName} - ${account.bankName}`}
            </MenuItem>
          ))}
        </Select>

        <Select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          displayEmpty
          fullWidth
          sx={sxStyles.inputField}
        >
          <MenuItem value="" disabled>
            Select Category
          </MenuItem>
          {categories.map((category, index) => (
            <MenuItem
              key={index}
              value={category.name}
              style={{ color: category.color, fontWeight: "bold" }}
            >
              {category.name}
            </MenuItem>
          ))}
          <MenuItem value="newCategory">Add New Category</MenuItem>
        </Select>

        {selectedCategory === "newCategory" && (
          <TextField
            placeholder="Enter New Category"
            value={newCategory}
            onChange={(e) => setNewCategory(e.target.value)}
            fullWidth
            sx={sxStyles.inputField}
          />
        )}

        <TextField
          label="Date"
          type="date"
          value={date}
          onChange={(e) => setDate(e.target.value)}
          fullWidth
          sx={sxStyles.inputField}
        />

        <Button onClick={handleSubmit} variant="contained" sx={sxStyles.submitButton}>
          Add Transaction
        </Button>
      </Box>
    </>
  );
};

export default AddTransactionWidget;
