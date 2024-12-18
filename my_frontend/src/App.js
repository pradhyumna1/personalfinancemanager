import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import HomePage from "./pages/HomePage";
import Dashboard from "./pages/Dashboard";
import Register from "./pages/Register";
import BudgetsPage from "./pages/BudgetsPage";
import { ContextProvider } from "./context/Context";
import TransactionsPage from "./pages/TransactionsPage";


function App() {
  return (
    <ContextProvider>
      <Router>
        <Routes>
          {/* Home Route */}
          <Route path="/" element={<HomePage />} />
          {/* Register/Login Route */}
          <Route path="/register" element={<Register />} />
          {/* Dashboard Route */}
          <Route path="/:username/dashboard" element={<Dashboard />} />
          <Route path="/:username/budgets" element={<BudgetsPage />} />
          <Route path="/:username/transactions" element={<TransactionsPage />} />
        </Routes>
      </Router>
    </ContextProvider>
  );
}

export default App;
