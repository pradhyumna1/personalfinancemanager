// src/pages/NotFound.js
import React from 'react';

const NotFound = () => (
  <div className="flex flex-col items-center justify-center min-h-screen bg-gray-900 text-white">
    <h1 className="text-6xl font-bold">404</h1>
    <p className="text-lg mt-2">Page Not Found</p>
    <a href="/" className="text-blue-500 mt-4 hover:underline">
      Go Back to Home
    </a>
  </div>
);

export default NotFound;
