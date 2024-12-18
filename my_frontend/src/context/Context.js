import React, { createContext, useReducer, useContext, useEffect } from "react";

// Initial state of the context
const initialState = {
  isLoggedIn: !!localStorage.getItem("user"), // Check if user data is in localStorage
  user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : null, // Load user data from localStorage
  linkToken: null, // Stores Plaid link token if used
  linkSuccess: false, // Tracks if Plaid link was successful
};

// Reducer function to manage context state changes
function reducer(state, action) {
  switch (action.type) {
    case "LOGIN":
      return {
        ...state,
        isLoggedIn: true, // Set to true on successful login
        user: action.payload, // Store user information
      };
    case "LOGOUT":
      localStorage.removeItem("user"); // Remove user data from localStorage on logout
      return {
        ...state,
        isLoggedIn: false, // Reset login state
        user: null, // Clear user information
      };
    case "SET_LINK_TOKEN":
      return { ...state, linkToken: action.payload };
    case "SET_LINK_SUCCESS":
      return { ...state, linkSuccess: true };
    default:
      return state;
  }
}

// Create Context
export const Context = createContext();

// Context Provider to wrap the application
export const ContextProvider = ({ children }) => {
  const [state, dispatch] = useReducer(reducer, initialState);

  // Persist user state changes to localStorage
  useEffect(() => {
    if (state.isLoggedIn && state.user) {
      localStorage.setItem("user", JSON.stringify(state.user));
    } else {
      localStorage.removeItem("user");
    }
  }, [state.isLoggedIn, state.user]);

  return (
    <Context.Provider value={{ state, dispatch }}>
      {children}
    </Context.Provider>
  );
};

// Custom hook to access context state
export const useAppContext = () => {
  return useContext(Context);
};

// Helper function to check login status
export const useIsLoggedIn = () => {
  const { state } = useAppContext();
  return state.isLoggedIn && state.user != null;
};
