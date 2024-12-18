import './index.css';
import reportWebVitals from './reportWebVitals';

import React from "react";
import { createRoot } from "react-dom/client"; // Import createRoot from react-dom/client
import { BrowserRouter as Router } from "react-router-dom"; // Import Router
import App from "./App";
import { ContextProvider } from "./context/Context";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(ArcElement, Tooltip, Legend);


// Get the root element from the HTML
const container = document.getElementById("root");
const root = createRoot(container); // Create a root

root.render(
    <ContextProvider>
      <App />
    </ContextProvider>
);

// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();

