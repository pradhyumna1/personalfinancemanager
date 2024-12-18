import React, { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { Box, Typography, Button, TextField } from "@mui/material";
import Navbar from "../components/NavBar";
import BudgetProgress from "../components/Budget/BudgetProgress";
import BudgetTable from "../components/Budget/BudgetTable";
import styles from "../styles/BudgetsPage.module.scss";

const BudgetsPage = () => {
  const { username } = useParams();
  const [budgets, setBudgets] = useState([]);
  const [error, setError] = useState(null);
  const [totalBudget, setTotalBudget] = useState(0);
  const [totalSpent, setTotalSpent] = useState(0);
  const [currentMonth, setCurrentMonth] = useState(new Date().toISOString().slice(0, 7));
  const [inputMonth, setInputMonth] = useState(currentMonth);
  const [refresh, setRefresh] = useState(false);

  useEffect(() => {
    const fetchBudgets = async () => {
      try {
        const budgetResponse = await fetch(
          `${process.env.REACT_APP_API_URL}/budgets/user/${username}?month=${currentMonth}`
        );

        if (budgetResponse.status === 204) {
          console.log("No existing budgets, fetching suggested budgets...");
          fetchSuggestedBudgets();
        } else if (budgetResponse.ok) {
          const budgetData = await budgetResponse.json();
          updateTotals(budgetData);
          setBudgets(
            budgetData.map((budget) => ({
              ...budget,
              category: { name: budget.category.name || budget.category },
              transactions: budget.transactions || [],
            }))
          );
        } else {
          throw new Error("Failed to fetch budgets.");
        }
      } catch (err) {
        console.error("Error fetching budgets:", err);
        setError("Error fetching budget data.");
      }
    };

    const fetchSuggestedBudgets = async () => {
      try {
        const suggestedResponse = await fetch(
          `${process.env.REACT_APP_API_URL}/budgets/suggested?username=${username}&month=${currentMonth}`
        );

        if (suggestedResponse.ok) {
          const suggestedData = await suggestedResponse.json();

          const formattedBudgets = Object.entries(suggestedData).map(([category, categoryData], index) => {
            const { suggested, actual } = categoryData;

            return {
              id: index,
              category: { name: category },
              amount: parseFloat(suggested) || 0,
              spent: parseFloat(actual) || 0,
              transactions: [],
              exceeded: undefined,
            };
          });

          await saveSuggestedBudgets(formattedBudgets, currentMonth);
          updateTotals(formattedBudgets);
          setBudgets(formattedBudgets);
        } else {
          setError("Failed to fetch suggested budgets.");
        }
      } catch (err) {
        console.error("Error fetching suggested budgets:", err);
        setError("Error fetching suggested budgets.");
      }
    };

    const saveSuggestedBudgets = async (suggestedBudgets, selectedMonth) => {
      try {
        await fetch(`${process.env.REACT_APP_API_URL}/budgets/create-suggested`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            username,
            month: selectedMonth,
            budgets: suggestedBudgets.map(({ category, amount, spent }) => ({
              category: { name: category.name },
              amount,
              spent,
            })),
          }),
        });
      } catch (err) {
        console.error("Error saving suggested budgets:", err);
      }
    };

    const updateTotals = (budgets) => {
      const total = budgets.reduce((sum, b) => sum + (b.amount || 0), 0);
      const spent = budgets.reduce((sum, b) => sum + (b.spent || 0), 0);
      setTotalBudget(total);
      setTotalSpent(spent);
    };

    fetchBudgets();
  }, [username, currentMonth, refresh]);

  const handleMonthChange = () => {
    setCurrentMonth(inputMonth);
  };

  const handleTransactionUpdate = (transactionId, difference,categoryName,transactionMonth) => {
    if (transactionMonth !== currentMonth) {
      console.log("Transaction does not belong to the current month. Skipping update.");
      return;
    }

    setBudgets((prevBudgets) =>
      prevBudgets.map((budget) => {
        if (budget.category.name === categoryName) {
          console.log(`Updating budget for category: ${budget.category.name}`);
          return {
            ...budget,
            spent: (budget.spent || 0) + difference,
            transactions: budget.transactions.map((txn) =>
              txn.transactionId === transactionId
                ? { ...txn, amount: txn.amount + difference }
                : txn
            ),
          };
        }
        return budget;
      })
    );

    setTotalSpent((prevTotal) => {
      const updatedTotal = (prevTotal || 0) + difference;
      console.log("Updated Total Spent:", updatedTotal);
      return updatedTotal;
    });
  };

  const handleBudgetUpdate = async (budgetId, newAmount) => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/budgets/updateAmount/${budgetId}`,
        {
          method: "PATCH",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ amount: newAmount }),
        }
      );

      if (response.ok) {
        setRefresh(!refresh);
        setBudgets((prevBudgets) =>
          prevBudgets.map((budget) =>
            budget.id === budgetId ? { ...budget, amount: newAmount } : budget
          )
        );
      } else {
        console.error("Failed to update budget amount.");
      }
    } catch (err) {
      console.error("Error updating budget amount:", err);
    }
  };

  return (
    <>
      <Navbar />
      <Box className={styles.budgetsPage}>
        <Box className={styles.header}>
          <Box className={styles.changeMonth}>
            <TextField
              label="Select Month"
              type="month"
              value={inputMonth}
              onChange={(e) => setInputMonth(e.target.value)}
              size="small"
              className={styles.monthInput}
              sx={{
                "& .MuiInputLabel-root": { color: "#10b981" },
                "& .MuiOutlinedInput-root": {
                  "& fieldset": { borderColor: "#10b981" },
                  "&:hover fieldset": { borderColor: "#10b981" },
                  "&.Mui-focused fieldset": { borderColor: "#10b981" },
                },
                "& input": { color: "#10b981" },
              }}
            />
            <Button
              variant="outlined"
              onClick={handleMonthChange}
              className={styles.changeMonthButton}
            >
              Update
            </Button>
          </Box>
          <Typography variant="h5" className={styles.pageTitle}>
            {`${new Date(`${currentMonth}-01T00:00`).toLocaleString("default", {
              month: "long",
            })} Budget`}
          </Typography>
        </Box>
        <Box className={styles.totalBudget}>
          <BudgetProgress spent={totalSpent} budget={totalBudget} category="Total Budget" />
        </Box>
        <Box className={styles.categories}>
          {error && <Typography className={styles.error}>{error}</Typography>}
          <BudgetTable
            budgets={budgets}
            onUpdateBudget={handleBudgetUpdate}
            username={username}
            onTransactionUpdate={(transactionId, difference, month, categoryName) =>
              handleTransactionUpdate(transactionId, difference, month, categoryName)
            }
          />
        </Box>
      </Box>
    </>
  );
};

export default BudgetsPage;
