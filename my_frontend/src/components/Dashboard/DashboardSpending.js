import React from "react";
import { PieChart, Pie, Cell, Tooltip } from "recharts";
import styles from "./DashboardSpending.module.scss";

const DashboardSpending = ({ spendingData }) => {
  const COLORS = ["#15ffd0", "#FFBB28", "#FF8042", "#8884d8", "#82ca9d"];

  return (
    <div className={styles.dashboardSpending}>
      <h3>Spending</h3>
      <PieChart width={200} height={200}>
        <Pie
          data={spendingData}
          dataKey="value"
          nameKey="category"
          cx="50%"
          cy="50%"
          outerRadius={80}
          fill="#8884d8"
        >
          {spendingData.map((entry, index) => (
            <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip />
      </PieChart>
    </div>
  );
};

export default DashboardSpending;
