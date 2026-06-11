// src/components/ProtectedRoute.jsx
import React from "react";
import { Navigate } from "react-router-dom";
import { getAccessToken } from "../api"; // Adjust the import path to where your auth logic lives

const ProtectedRoute = ({ children }) => {
  const token = getAccessToken();

  // If there is no token, redirect to the login page
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Otherwise, render the protected component
  return children;
};

export default ProtectedRoute;