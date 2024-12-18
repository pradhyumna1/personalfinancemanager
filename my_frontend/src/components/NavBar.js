import React, { useState, useEffect } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import styles from "../styles/NavBar.module.scss";

const Navbar = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const [loggedInUser, setLoggedInUser] = useState(null);
  const [dropdownVisible, setDropdownVisible] = useState(false);

  // Check user login status
  useEffect(() => {
    const user = JSON.parse(localStorage.getItem("user"));
    setLoggedInUser(user);
  }, []);

  // Handle dynamic updates to localStorage
  useEffect(() => {
    const handleStorageChange = () => {
      const user = JSON.parse(localStorage.getItem("user"));
      setLoggedInUser(user);
    };

    window.addEventListener("storage", handleStorageChange);
    return () => window.removeEventListener("storage", handleStorageChange);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    setLoggedInUser(null);
    navigate("/register");
  };

  const handleDropdownToggle = (e) => {
    e.stopPropagation();
    setDropdownVisible((prevState) => !prevState);
  };

  const closeDropdown = () => setDropdownVisible(false);

  useEffect(() => {
    document.addEventListener("click", closeDropdown);
    return () => document.removeEventListener("click", closeDropdown);
  }, []);

  return (
    <header className={styles.header}>
      <Link to="/" className={styles.logo}>
        BudgetSphere
      </Link>
      <nav className={styles.navBar}>
        <Link
          to={loggedInUser ? `/${loggedInUser.username}/dashboard` : "/register"}
          className={`${styles.navLink} ${
            location.pathname.startsWith("/dashboard") ? styles.active : ""
          }`}
        >
          Dashboard
        </Link>
        <Link
          to={loggedInUser ? `/${loggedInUser.username}/spending` : "/register"}
          className={`${styles.navLink} ${
            location.pathname.startsWith("/spending") ? styles.active : ""
          }`}
        >
          Spending
        </Link>
        <Link
          to={
            loggedInUser ? `/${loggedInUser.username}/transactions` : "/register"
          }
          className={`${styles.navLink} ${
            location.pathname.startsWith("/transactions") ? styles.active : ""
          }`}
        >
          Transactions
        </Link>
        <Link
          to={loggedInUser ? `/${loggedInUser.username}/budgets` : "/register"}
          className={`${styles.navLink} ${
            location.pathname.startsWith("/budgets") ? styles.active : ""
          }`}
        >
          Budgets
        </Link>
        {loggedInUser ? (
          <div
            className={styles.userInfo}
            onClick={(e) => handleDropdownToggle(e)}
          >
            <div className={styles.avatar}>
              {loggedInUser.username.charAt(0).toUpperCase()}
            </div>
            <span>{loggedInUser.username}</span>
            {dropdownVisible && (
              <div className={styles.dropdown}>
                <div onClick={handleLogout} className={styles.dropdownItem}>
                  Logout
                </div>
              </div>
            )}
          </div>
        ) : (
          <Link
            to="/register"
            className={`${styles.navLink} ${
              location.pathname === "/register" ? styles.active : ""
            }`}
          >
            Log In/Register
          </Link>
        )}
      </nav>
    </header>
  );
};

export default Navbar;
