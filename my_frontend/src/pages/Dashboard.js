import React, { useContext, useEffect, useState } from "react";
import { usePlaidLink } from "react-plaid-link";
import { Context } from "../context/Context";
import { useParams, useNavigate } from "react-router-dom";
import Navbar from "../components/NavBar";
import { Box } from "@mui/material";
import DashboardBudgets from "../components/Dashboard/DashboardBudgets";
import DashboardAccounts from "../components/Dashboard/DashboardAccounts";
import DashboardTopMerchants from "../components/Dashboard/DashboardTopMerchants";

const Dashboard = () => {
  const { dispatch } = useContext(Context);
  const { username } = useParams();
  const navigate = useNavigate();
  const [userId, setUserId] = useState(null);
  const [linkToken, setLinkToken] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [refreshMerchants, setRefreshMerchants] = useState(false); // Trigger for refreshing merchants

  // Check if the user is logged in
  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    if (!user) {
      navigate("/login");
      return;
    }
    if (user.username !== username) {
      navigate(`/${user.username}/dashboard`);
    } else {
      dispatch({ type: "LOGIN", payload: user });
      setUserId(user.id);
    }
  }, [username, navigate, dispatch]);

  // Generate Plaid Link Token
  useEffect(() => {
    const generateLinkToken = async () => {
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/create_link_token`,
          { method: "POST" }
        );
        if (response.ok) {
          const data = await response.json();
          setLinkToken(data.linkToken);
        } else {
          console.error("Failed to generate link token.");
        }
      } catch (error) {
        console.error("Error generating link token:", error);
      }
    };

    generateLinkToken();
  }, []);

  // Plaid Link Setup
  const { open, ready } = usePlaidLink({
    token: linkToken,
    onSuccess: async (publicToken) => {
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/set_access_token`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ publicToken, username }),
          }
        );
        if (response.ok) {
          fetchAccounts();
        } else {
          console.error("Failed to save access token.");
        }
      } catch (error) {
        console.error("Error linking bank:", error);
      }
    },
    onExit: (error) => {
      if (error) console.error("Plaid Link exited with error:", error);
    },
  });

  const fetchAccounts = async () => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/get_bank_info?username=${username}`
      );
      if (response.ok) {
        const data = await response.json();
        setAccounts(data);
      } else {
        console.error("Failed to fetch accounts.");
      }
    } catch (error) {
      console.error("Error fetching accounts:", error);
    }
  };

  const fetchTransactions = async () => {
    try {
      const response = await fetch(
        `${process.env.REACT_APP_API_URL}/get_transactions?username=${username}`
      );
      if (response.ok) {
        const data = await response.json();
        setRefreshMerchants((prev) => !prev); // Toggle refresh state for DashboardTopMerchants
      } else {
        console.error("Failed to fetch transactions.");
      }
    } catch (error) {
      console.error("Error fetching transactions:", error);
    }
  };

  useEffect(() => {
    if (linkToken) {
      fetchAccounts();
    }
  }, [linkToken]);

  return (
    <>
      <Navbar />
      <Box className="relative bg-gray-900 text-gray-100 min-h-screen p-6">
        <Box className="grid grid-cols-3 gap-6">
          <Box className="col-span-2">
            <DashboardBudgets budgetsEndpoint="/api/budgets" />
          </Box>
          <Box>
            <DashboardAccounts
              accounts={accounts}
              onAddBank={open}
              onSync={fetchTransactions} // Call fetchTransactions when syncing
              ready={ready}
            />
            <DashboardTopMerchants
              userId={userId}
              refresh={refreshMerchants} // Pass refresh state to DashboardTopMerchants
            />
          </Box>
        </Box>
      </Box>
    </>
  );
};

export default Dashboard;
