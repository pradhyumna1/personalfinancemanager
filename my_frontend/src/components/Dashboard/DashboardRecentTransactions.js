import React from "react";
import styles from "./DashboardRecentTransactions.module.scss";

const DashboardRecentTransactions = ({ transactions }) => {
  return (
    <div className={styles.dashboardRecentTransactions}>
      <h3>Recent Transactions</h3>
      <ul>
        {transactions.map((txn, index) => (
          <li key={index}>
            <span>{txn.date}</span>
            <span>{txn.merchantName || "Unknown"}</span>
            <span>${txn.amount.toFixed(2)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default DashboardRecentTransactions;
