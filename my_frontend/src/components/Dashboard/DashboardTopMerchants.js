import React, { useEffect, useState } from "react";
import {
  Box,
  Typography,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  TextField,
  Button,
} from "@mui/material";
import styles from "./styles/DashboardTopMerchants.module.scss";

const DashboardTopMerchants = ({ userId, refresh }) => {
  const [merchantSpending, setMerchantSpending] = useState({});
  const [error, setError] = useState(null);
  const [selectedMonth, setSelectedMonth] = useState(new Date().toISOString().slice(0, 7));
  const [displayedMonth, setDisplayedMonth] = useState(new Date().toISOString().slice(0, 7));

  const fetchMerchantSpending = async (month) => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/spending-by-merchants?userId=${userId}&yearMonth=${month}`
      );
      if (response.ok) {
        const data = await response.json();
        const filteredData = Object.fromEntries(
          Object.entries(data).filter(([, amount]) => amount > 0)
        );
        setMerchantSpending(filteredData);
      } else {
        setError("Failed to fetch merchant spending.");
      }
    } catch (err) {
      console.error("Error fetching merchant spending:", err);
      setError("Error fetching merchant data.");
    }
  };

  useEffect(() => {
    if (userId) {
      fetchMerchantSpending(displayedMonth);
    }
  }, [userId, displayedMonth, refresh]); // Add refresh to dependency array

  const sortedMerchants = Object.entries(merchantSpending).sort(([, a], [, b]) => b - a);

  return (
    <Box className={styles.topMerchantsBox}>
      <Typography variant="h6" className={styles.title}>
        Most Spent by Merchant
      </Typography>
      <Box className={styles.monthSelector}>
        <TextField
          label="Select Month"
          type="month"
          value={selectedMonth}
          onChange={(e) => setSelectedMonth(e.target.value)}
          size="small"
          className={styles.monthInput}
          sx={{
            "& .MuiInputLabel-root": { color: "#10b981" }, // Label text color
            "& .MuiOutlinedInput-root": {
              "& fieldset": { borderColor: "#10b981" }, // Default border color
              "&:hover fieldset": { borderColor: "#10b981" }, // Hover border color
              "&.Mui-focused fieldset": { borderColor: "#10b981" }, // Focused border color
            },
            "& input": { color: "#10b981" }, // Input text color (e.g., "December 2024")
            "& .MuiButtonBase-root": { color: "#10b981" }, // Calendar icon color
          }}
        />
        <Button
          variant="outlined"
          onClick={() => setDisplayedMonth(selectedMonth)}
          className={styles.fetchButton}
        >
          Update
        </Button>
      </Box>
      {error ? (
        <Typography className={styles.error}>{error}</Typography>
      ) : sortedMerchants.length > 0 ? (
        <Table>
          <TableHead>
            <TableRow>
              <TableCell className={styles.tableHeader}>Merchant</TableCell>
              <TableCell className={styles.tableHeader} align="right">
                Amount Spent
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {sortedMerchants.map(([merchant, total], index) => (
              <TableRow key={index} className={styles.tableRow}>
                <TableCell className={styles.merchantName}>{merchant}</TableCell>
                <TableCell className={styles.amountSpent} align="right">
                  ${parseFloat(total).toFixed(2)}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      ) : (
        <Typography className={styles.noData}>No data available.</Typography>
      )}
    </Box>
  );
};

export default DashboardTopMerchants;
