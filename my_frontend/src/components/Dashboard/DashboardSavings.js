import React from "react";
import styles from "./DashboardSavings.module.scss";

const DashboardSavings = ({ savings }) => {
  return (
    <div className={styles.dashboardSavings}>
      <h3>Savings</h3>
      <ul>
        {savings.map((saving, index) => (
          <li key={index}>
            <span>{saving.name}</span>
            <span>${saving.amount.toFixed(2)}</span>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default DashboardSavings;
