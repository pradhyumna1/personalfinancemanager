import React from "react";
import { Box, Typography } from "@mui/material";
import Navbar from "../components/NavBar";
import styles from "../styles/HomePage.module.scss";

const HomePage = () => {
  return (
    <Box className={styles.homePage}>
      {/* Navbar */}
      <Navbar />

      {/* Content Section */}
      <Box className={styles.content}>
        {/* Section 1 */}
        <section className={styles.featureSection}>
          <Box className={styles.textContainer}>
            <Typography variant="h2" className={styles.featureTitle}>
              Discover Spending Habits
            </Typography>
            <Typography variant="body1" className={styles.featureDescription}>
              Gain a deeper understanding of where your money goes with our
              comprehensive Spending section. View your expenses broken down by
              categories, easily filterable by custom date ranges or default to
              the current month. Visualize your spending trends over time with
              interactive line graphs, helping you identify patterns and make
              smarter financial decisions.
            </Typography>
          </Box>
          <Box className={styles.imageContainer}>
            <img
              src="/assets/spending_preview.png"
              alt="Spending preview"
              className={styles.featureImage}
            />
          </Box>
        </section>

        {/* Section 2 */}
        <section className={styles.featureSection}>
          <Box className={styles.textContainer}>
            <Typography variant="h2" className={styles.featureTitle}>
              Track Transactions
            </Typography>
            <Typography variant="body1" className={styles.featureDescription}>
              Stay on top of your finances with our detailed Transactions
              section. Review all your transactions in one place, categorized
              for clarity and ease. Add notes, adjust categories, or filter by
              date to get the insights you need.
            </Typography>
          </Box>
          <Box className={styles.imageContainer}>
            <img
              src="/assets/transactions_preview.png"
              alt="Transactions preview"
              className={styles.featureImage}
            />
          </Box>
        </section>

        {/* Section 3 */}
        <section className={styles.featureSection}>
          <Box className={styles.textContainer}>
            <Typography variant="h2" className={styles.featureTitle}>
              Manage Budgets
            </Typography>
            <Typography variant="body1" className={styles.featureDescription}>
              Create and manage budgets that work for you. Set goals, monitor
              spending, and adjust your budget in real-time to stay on track
              with your financial objectives.
            </Typography>
          </Box>
          <Box className={styles.imageContainer}>
            <img
              src="/assets/budgets_preview.png"
              alt="Budgets preview"
              className={styles.featureImage}
            />
          </Box>
        </section>
      </Box>

      {/* Background Ellipse */}
      <Box className={styles.ellipse}></Box>
    </Box>
  );
};

export default HomePage;
