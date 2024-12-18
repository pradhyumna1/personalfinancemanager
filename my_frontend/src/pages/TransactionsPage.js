import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Box,
  Typography,
  Button,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Checkbox,
  Modal,
} from "@mui/material";
import Navbar from "../components/NavBar";
import TransactionEditWidget from "../components/Transaction/TransactionEditWidget";
import CategoryChip from "../components/Category/CategoryChip";
import CategoryPicker from "../components/Category/CategoryPicker";
import AddTransactionWidget from "../components/Transaction/AddTransactionWidget";
import styles from "../styles/TransactionsPage.module.scss";

const sxStyles = {
  searchInput: {
    "& .MuiInputLabel-root": { color: "#8c52ff" },
    "& .MuiInputLabel-root.Mui-focused": { color: "#8c52ff" },
    "& .MuiOutlinedInput-root": {
      "& fieldset": { borderColor: "#8c52ff" },
      "&:hover fieldset": { borderColor: "#8c52ff" },
      "&.Mui-focused fieldset": { borderColor: "#8c52ff" },
    },
    "& input": { color: "#8c52ff" },
  },
  addButton: {
    backgroundColor: "#10b981",
    color: "#fff",
    "&:hover": { backgroundColor: "#8c52ff" },
    marginRight: "auto",
  },
  selectButton: {
    backgroundColor: "#8c52ff",
    color: "#fff",
    "&:hover": { backgroundColor: "#10b981" },
    marginLeft: "auto",
  },
  changeCategoryButton: {
    backgroundColor: "#8c52ff",
    color: "#fff",
    "&:hover": { backgroundColor: "#10b981" },
    marginTop: "10px",
  },
};

const TransactionsPage = () => {
  const { username } = useParams();
  const [transactions, setTransactions] = useState([]);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [selectedTransactions, setSelectedTransactions] = useState([]);
  const [isSelectionMode, setIsSelectionMode] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isAddTransactionOpen, setIsAddTransactionOpen] = useState(false);
  const [error, setError] = useState(null);

  const formatCategory = (category) => {
    if (!category) return "Uncategorized";
    return category.replace(/_/g, " ").replace(/\b\w/g, (char) => char.toUpperCase());
  };

  const formatDate = (dateString) => {
    const date = new Date(dateString);
    return date.toLocaleDateString("en-US", { timeZone: "UTC" });
  };

  const distinctCategories = [
    ...new Map(
      transactions.map((transaction) => [
        transaction.category.name,
        transaction.category,
      ])
    ).values(),
  ];

  useEffect(() => {
    const fetchTransactions = async () => {
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/get-saved-transactions?username=${username}`
        );
        if (response.ok) {
          const data = await response.json();
          setTransactions(data);
        } else {
          setError("Failed to fetch transactions.");
        }
      } catch (err) {
        console.error("Error fetching transactions:", err);
        setError("Error fetching transactions.");
      }
    };

    fetchTransactions();
  }, [username]);

  const filteredTransactions = transactions.filter(
    (transaction) =>
      transaction.merchantName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      transaction.category.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      transaction.amount.toString().includes(searchQuery)
  );

  const handleRowClick = (transaction) => {
    setSelectedTransaction(transaction);
  };

  const handleCloseWidget = () => {
    setSelectedTransaction(null);
  };

  const handleTransactionUpdate = (id, updatedFields) => {
    setTransactions((prev) =>
      prev.map((txn) =>
        txn.transactionId === id
          ? {
              ...txn,
              ...updatedFields,
            }
          : txn
      )
    );
  };

  const handleCheckboxChange = (transactionId) => {
    setSelectedTransactions((prevSelected) =>
      prevSelected.includes(transactionId)
        ? prevSelected.filter((id) => id !== transactionId)
        : [...prevSelected, transactionId]
    );
  };

  const openModal = () => {
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
  };

  const handleCategoryChange = async (newCategory) => {
    const formattedCategory = newCategory.name;

    try {
      const response = await fetch(`${process.env.REACT_APP_API_URL}/update-txn-category`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          transactionIds: selectedTransactions,
          newCategory: formattedCategory,
        }),
      });

      if (response.ok) {
        const updatedCategory = await response.json();

        setTransactions((prev) =>
          prev.map((txn) =>
            selectedTransactions.includes(txn.transactionId)
              ? {
                  ...txn,
                  category: {
                    ...txn.category,
                    name: updatedCategory.name,
                    color: updatedCategory.color,
                  },
                }
              : txn
          )
        );

        setSelectedTransactions([]);
        closeModal();
      } else {
        console.error("Failed to update transaction category");
      }
    } catch (err) {
      console.error("Error updating transaction category:", err);
    }
  };

  const handleAddTransaction = (newTransaction) => {
    setTransactions((prev) => [newTransaction, ...prev]);
    setIsAddTransactionOpen(false);
  };

  return (
    <>
      <Navbar />
      <Box className={styles.transactionsPage}>
        <Box className={styles.searchBar}>
          <TextField
            label="Search Transactions"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            fullWidth
            className={styles.searchInput}
            sx={sxStyles.searchInput}
          />
        </Box>

        <Box className={styles.header}>
          <Button
            variant="contained"
            sx={sxStyles.addButton}
            onClick={() => setIsAddTransactionOpen(true)}
          >
            Add New Transaction
          </Button>
          <Button
            variant="contained"
            sx={sxStyles.selectButton}
            onClick={() => setIsSelectionMode(!isSelectionMode)}
          >
            {isSelectionMode ? "Exit Selection Mode" : "Select Transactions"}
          </Button>
        </Box>

        {isSelectionMode && selectedTransactions.length > 0 && (
          <Button
            variant="contained"
            sx={sxStyles.changeCategoryButton}
            onClick={openModal}
          >
            Change Category ({selectedTransactions.length} Selected)
          </Button>
        )}

        <Box className={styles.tableContainer}>
          <Table>
            <TableHead>
              <TableRow>
                {isSelectionMode && <TableCell />} 
                <TableCell>Date</TableCell>
                <TableCell>Merchant Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Amount</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredTransactions.map((transaction) => (
                <TableRow
                  key={transaction.transactionId}
                  style={{ cursor: "pointer" }}
                  onClick={
                    isSelectionMode
                      ? () => handleCheckboxChange(transaction.transactionId)
                      : () => handleRowClick(transaction)
                  }
                >
                  {isSelectionMode && (
                    <TableCell>
                      <Checkbox
                        checked={selectedTransactions.includes(transaction.transactionId)}
                      />
                    </TableCell>
                  )}
                  <TableCell>{formatDate(transaction.date)}</TableCell>
                  <TableCell>{transaction.merchantName}</TableCell>
                  <TableCell>
                    <CategoryChip
                      categoryName={formatCategory(transaction.category.name)}
                      categoryColor={transaction.category.color}
                    />
                  </TableCell>
                  <TableCell
                    style={{
                      color: transaction.amount < 0 ? "#10b981" : "#ffffff",
                      fontWeight: transaction.amount < 0 ? "bold" : "normal",
                    }}
                  >
                    ${Math.abs(transaction.amount).toFixed(2)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Box>

        {selectedTransaction && (
          <TransactionEditWidget
            transaction={selectedTransaction}
            distinctCategories={distinctCategories}
            onClose={handleCloseWidget}
            onTransactionUpdate={handleTransactionUpdate}
          />
        )}
        {error && <Typography className={styles.error}>{error}</Typography>}

        <Modal open={isModalOpen} onClose={closeModal}>
          <CategoryPicker
            categories={distinctCategories}
            onCategorySelect={handleCategoryChange}
            onClose={closeModal}
          />
        </Modal>

        <Modal open={isAddTransactionOpen} onClose={() => setIsAddTransactionOpen(false)}>
          <AddTransactionWidget
            username={username}
            categories={distinctCategories}
            onAddTransaction={handleAddTransaction}
            onClose={() => setIsAddTransactionOpen(false)}
          />
        </Modal>
      </Box>
    </>
  );
};

export default TransactionsPage;
