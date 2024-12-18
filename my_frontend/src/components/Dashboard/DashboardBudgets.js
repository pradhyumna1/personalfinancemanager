import React, { useEffect, useState, useContext } from "react";
import styles from "./styles/DashboardBudgets.module.scss";
import { useNavigate } from "react-router-dom";
import { Context } from "../../context/Context";

const DashboardBudgets = ({ budgetsEndpoint, accountsUpdated }) => {
  const { state } = useContext(Context);
  const navigate = useNavigate();
  const [budgetData, setBudgetData] = useState([]); 
  const [isLoading, setIsLoading] = useState(true);

  // Fetch budgets associated with the user
  const fetchBudgets = async () => {
    const user = JSON.parse(localStorage.getItem("user"));
    try {
      const response = await fetch(
        `${budgetsEndpoint}/user/${user.username}?month=${new Date()
          .toISOString()
          .slice(0, 7)}`
      );
      if (response.ok) {
        const data = await response.json();
        setBudgetData(data || []);
      } else {
        console.error("Failed to fetch budgets.");
      }
    } catch (error) {
      console.error("Error fetching budgets:", error);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBudgets();
  }, [accountsUpdated]); // Re-fetch when accounts are updated

  const handleCreateBudget = () => {
    navigate(`/${state.user.username}/budgets`); // Navigate to the user's budgets page
  };

  if (isLoading) {
    return <p>Loading budgets...</p>;
  }

  if (!budgetData || budgetData.length === 0) {
    return (
      <div className={styles.dashboardBudgets}>
        <h3>No Budgets Found</h3>
        <p>
          Create your first budget to start tracking your spending!{" "}
          <button className={styles.createBudgetButton} onClick={handleCreateBudget}>
            Create Budget
          </button>
        </p>
      </div>
    );
  }

  // Calculate total budget and spending
  const totalBudget = budgetData.reduce((sum, budget) => sum + budget.amount, 0);
  const totalSpent = budgetData.reduce((sum, budget) => sum + budget.spent, 0);
  const spendingProgress = Math.min((totalSpent / totalBudget) * 100, 100).toFixed(2);

  return (
    <div className={styles.dashboardBudgets}>
      <h3>Budget - Month</h3>
      <p>Total Budget: ${totalBudget}</p>
      <p>Current Spending: ${totalSpent}</p>
      <div className={styles.progressBar}>
        <div
          className={styles.progress}
          style={{ width: `${spendingProgress}%` }}
        ></div>
      </div>
      <p>{spendingProgress}% of your budget used</p>
    </div>
  );
};

export default DashboardBudgets;
