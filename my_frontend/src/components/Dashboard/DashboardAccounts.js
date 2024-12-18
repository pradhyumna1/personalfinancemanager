import React, { useState } from "react";
import styles from "./styles/DashboardAccounts.module.scss";
import { Box, Typography, Button } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import SyncIcon from "@mui/icons-material/Sync";

// Function to get institution logo
const getInstitutionLogo = (institutionName) => {
  const logoPath = `/BankLogos/${institutionName
    ?.toLowerCase()
    .replace(/\s+/g, "_")
    .replace(/[^a-z0-9_]/g, "")}.png`;
  return logoPath;
};

// Function to get account type logo
const getAccountTypeLogo = (type) => {
  const logoPath = `/AccountLogos/${type
    ?.charAt(0).toUpperCase() + type.slice(1).replace(/\s+/g, "")}.png`;
  return logoPath;
};

const DashboardAccounts = ({ accounts, onAddBank, onSync, ready }) => {
  const accountGroups = accounts.reduce(
    (acc, account) => {
      const type = account.accountSubtype || "unknown";

      // Consolidate student and mortgage accounts under "loan"
      const normalizedType = ["student", "mortgage"].includes(type)
        ? "loan"
        : type;

      if (!acc[normalizedType]) {
        acc[normalizedType] = { accounts: [], totalBalance: 0 };
      }

      acc[normalizedType].accounts.push(account);
      acc[normalizedType].totalBalance += account.currentBalance || 0;

      // Adjust net balance for short-term accounts
      if (normalizedType === "credit card") {
        acc.netBalance -= account.currentBalance || 0;
      } else if (normalizedType === "checking") {
        acc.netBalance += account.currentBalance || 0;
      }
      return acc;
    },
    { netBalance: 0 }
  );

  const [expandedSections, setExpandedSections] = useState({});

  const toggleSection = (type) => {
    setExpandedSections((prev) => ({
      ...prev,
      [type]: !prev[type],
    }));
  };

  return (
    <Box className={styles.dashboardAccounts}>
      <Typography variant="h6" className={styles.title}>
        Accounts Linked
      </Typography>

      {/* Short-Term Accounts */}
      {["checking", "credit card"].map((type) => (
        <Box key={type} className={styles.accountType}>
          <Box
            className={`${styles.dropdownHeader} ${
              expandedSections[type] ? styles.expanded : ""
            }`}
            onClick={() => toggleSection(type)}
          >
            <Box className={styles.accountTypeHeader}>
              <img
                src={getAccountTypeLogo(type)}
                alt={`${type} logo`}
                className={styles.accountTypeIcon}
                onError={(e) => {
                  e.target.onerror = null;
                  e.target.style.display = "none";
                }}
              />
              <Typography className={styles.accountHeader}>
                {type === "checking" ? "Checking" : "Credit Card"}
              </Typography>
            </Box>
            <Typography
              className={
                type === "credit card"
                  ? styles.totalBalanceRed
                  : styles.totalBalance
              }
            >
              $
              {accountGroups[type]?.totalBalance?.toFixed(2) || "0.00"}
            </Typography>
          </Box>
          {expandedSections[type] && (
            <Box className={styles.accountList}>
              {accountGroups[type]?.accounts.map((acc) => (
                <Box key={acc.accountId} className={styles.account}>
                  <Box className={styles.accountInfo}>
                    {acc.institutionName && (
                      <img
                        src={getInstitutionLogo(acc.institutionName)}
                        alt={`${acc.institutionName} logo`}
                        className={styles.logo}
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.style.display = "none"; // Hide broken logos
                        }}
                      />
                    )}
                    {!acc.institutionName && (
                      <Typography className={styles.institutionFallback}>
                        {acc.institutionName || "Institution Not Found"}
                      </Typography>
                    )}
                    <Typography className={styles.accountName}>
                      {acc.accountName || "Unnamed Account"}
                    </Typography>
                  </Box>
                  <Typography className={styles.balance}>
                    ${acc.currentBalance?.toFixed(2) || "N/A"}
                  </Typography>
                </Box>
              ))}
            </Box>
          )}
        </Box>
      ))}

      {/* Net Balance Section */}
      <Box className={styles.netBalanceBox}>
        <Box className={styles.infoHeader}>
          <Box className={styles.accountTypeHeader}>
            <img
              src="/AccountLogos/NetCash.png"
              alt="Net Cash"
              className={styles.accountTypeIcon}
              onError={(e) => {
                e.target.onerror = null;
                e.target.style.display = "none"; // Hide broken icons
              }}
            />
            <Typography className={styles.accountHeader}>Net Cash</Typography>
          </Box>
          <Typography
            className={`${styles.netBalanceText} ${
              accountGroups.netBalance >= 0 ? styles.positive : styles.negative
            }`}
          >
            ${accountGroups.netBalance.toFixed(2)}
          </Typography>
          <Box className={styles.infoIcon} title="Available Working Cash Flow">
            i
          </Box>
        </Box>
      </Box>

      {/* Long-Term Accounts */}
      <Box>
        {["savings", "loan"].map((type) => (
          <Box key={type} className={styles.accountType}>
            <Box
              className={`${styles.dropdownHeader} ${
                expandedSections[type] ? styles.expanded : ""
              }`}
              onClick={() => toggleSection(type)}
            >
              <Box className={styles.accountTypeHeader}>
                <img
                  src={getAccountTypeLogo(type)}
                  alt={`${type} logo`}
                  className={styles.accountTypeIcon}
                  onError={(e) => {
                    e.target.onerror = null;
                    e.target.style.display = "none";
                  }}
                />
                <Typography className={styles.accountHeader}>
                  {type === "loan" ? "Loans" : type.charAt(0).toUpperCase() + type.slice(1)}
                </Typography>
              </Box>
              <Typography className={styles.totalBalance}>
                $
                {accountGroups[type]?.totalBalance?.toFixed(2) || "0.00"}
              </Typography>
            </Box>
            {expandedSections[type] && (
              <Box className={styles.accountList}>
                {accountGroups[type]?.accounts.map((acc) => (
                  <Box key={acc.accountId} className={styles.account}>
                    <Box className={styles.accountInfo}>
                      {acc.institutionName && (
                        <img
                          src={getInstitutionLogo(acc.institutionName)}
                          alt={`${acc.institutionName} logo`}
                          className={styles.logo}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.style.display = "none";
                          }}
                        />
                      )}
                      {!acc.institutionName && (
                        <Typography className={styles.institutionFallback}>
                          {acc.institutionName || "Institution Not Found"}
                        </Typography>
                      )}
                      <Typography className={styles.accountName}>
                        {acc.accountName || "Unnamed Account"}
                      </Typography>
                    </Box>
                    <Typography className={styles.balance}>
                      ${acc.currentBalance?.toFixed(2) || "N/A"}
                    </Typography>
                  </Box>
                ))}
              </Box>
            )}
          </Box>
        ))}
      </Box>

      <Button
        onClick={onAddBank}
        disabled={!ready}
        className={styles.addBankButton}
        startIcon={<AddIcon />}
      >
        Add Bank
      </Button>
      <Button
        onClick={onSync}
        className={styles.syncButton}
        startIcon={<SyncIcon />}
      >
        Sync Transactions
      </Button>
    </Box>
  );
};

export default DashboardAccounts;
