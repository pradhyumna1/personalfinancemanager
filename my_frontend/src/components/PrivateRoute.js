import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { useContext } from 'react';
import { Context } from '../context/Context';

const PrivateRoute = () => {
  const { state } = useContext(Context);

  return state.isLoggedIn ? <Outlet /> : <Navigate to="/register" />;
};

export default PrivateRoute;
