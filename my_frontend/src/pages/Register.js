import React, { useState, useContext } from "react";
import { useNavigate } from "react-router-dom";
import { Context } from "../context/Context";

const Register = () => {
  const { dispatch } = useContext(Context);
  const navigate = useNavigate();

  const [isLogin, setIsLogin] = useState(false); // Toggle between login and register
  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    address: "",
    phoneNumber: "",
    countryCode: "+1", // Default to USA
    username: "",
    usernameOrEmail: "", // Combined input for username or email during login
    email: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [addressSuggestions, setAddressSuggestions] = useState([]);
  const [manualAddress, setManualAddress] = useState(false); // For manual address entry

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleAddressChange = async (query) => {
    if (manualAddress) return; // Skip autocomplete if manual entry is selected

    if (query.trim() === "") {
      setAddressSuggestions([]);
      return;
    }

    try {
      const response = await fetch(
        `https://nominatim.openstreetmap.org/search?q=${query}&format=json`
      );
      if (response.ok) {
        const data = await response.json();
        setAddressSuggestions(data);
      } else {
        console.error("Failed to fetch address suggestions.");
      }
    } catch (error) {
      console.error("Error fetching address suggestions:", error);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (isLogin) {
      // Login process
      try {
        const response = await fetch(
          `${process.env.REACT_APP_API_URL}/users/login`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              usernameOrEmail: formData.usernameOrEmail,
              password: formData.password,
            }),
          }
        );

        if (response.ok) {
          const userData = await response.json();
          localStorage.setItem("user", JSON.stringify(userData));
          dispatch({ type: "LOGIN", payload: userData });
          navigate(`/${userData.username}/dashboard`);
        } else {
          setError("Invalid credentials. Please try again.");
        }
      } catch (err) {
        setError("Something went wrong. Please try again.");
      }
    } else {
      // Registration process: Call /registerUser and /registerUserDetails
      try {
        // Step 1: Register user
        const userResponse = await fetch(
          `${process.env.REACT_APP_API_URL}/users/registerUser`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              username: formData.username,
              email: formData.email,
              password: formData.password,
            }),
          }
        );

        if (!userResponse.ok) {
          throw new Error("Failed to register user. Please try again.");
        }

        const userData = await userResponse.json();

        // Step 2: Register user details
        const detailsResponse = await fetch(
          `${process.env.REACT_APP_API_URL}/users/registerUserDetails`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              userId: userData.id,
              firstName: formData.firstName,
              lastName: formData.lastName,
              address: formData.address,
              phoneNumber: `${formData.countryCode}${formData.phoneNumber}`,
            }),
          }
        );

        if (!detailsResponse.ok) {
          throw new Error("Failed to save user details. Please try again.");
        }
        
        localStorage.setItem("user", JSON.stringify(userData));
        dispatch({ type: "LOGIN", payload: userData });
        navigate(`/${userData.username}/dashboard`);
      } catch (err) {
        setError(err.message || "Something went wrong. Please try again.");
      }
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen bg-gray-900 text-gray-100">
      <div className="w-full max-w-md p-6 bg-gray-800 rounded-lg shadow-lg">
        <h2 className="text-2xl font-bold text-center text-green-400">
          {isLogin ? "Log In" : "Register"}
        </h2>
        <form onSubmit={handleSubmit} className="mt-6 space-y-4">
          {!isLogin && (
            <>
              <div>
                <label
                  htmlFor="firstName"
                  className="block text-sm font-medium text-gray-300"
                >
                  First Name
                </label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                  className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                />
              </div>
              <div>
                <label
                  htmlFor="lastName"
                  className="block text-sm font-medium text-gray-300"
                >
                  Last Name
                </label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                  className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                />
              </div>
              <div>
                <label
                  htmlFor="phoneNumber"
                  className="block text-sm font-medium text-gray-300"
                >
                  Phone Number
                </label>
                <div className="flex">
                  <select
                    name="countryCode"
                    value={formData.countryCode}
                    onChange={handleChange}
                    className="bg-gray-700 text-gray-100 border border-gray-600 rounded-md p-2 focus:ring focus:ring-green-400 focus:outline-none mr-2"
                    required
                  >
                    <option value="+1">USA (+1)</option>
                    <option value="+44">UK (+44)</option>
                    <option value="+91">India (+91)</option>
                    <option value="+61">Australia (+61)</option>
                    <option value="+1">Canada (+1)</option>
                  </select>
                  <input
                    type="text"
                    id="phoneNumber"
                    name="phoneNumber"
                    value={formData.phoneNumber}
                    onChange={handleChange}
                    required
                    className="flex-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                    placeholder="Phone number"
                  />
                </div>
              </div>
              <div>
                <label
                  htmlFor="username"
                  className="block text-sm font-medium text-gray-300"
                >
                  Username
                </label>
                <input
                  type="text"
                  id="username"
                  name="username"
                  value={formData.username}
                  onChange={handleChange}
                  required
                  className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                />
              </div>
              <div>
                <label
                  htmlFor="address"
                  className="block text-sm font-medium text-gray-300"
                >
                  Address
                </label>
                {!manualAddress ? (
                  <div>
                    <input
                      type="text"
                      id="address"
                      name="address"
                      value={formData.address}
                      onChange={(e) => {
                        handleChange(e);
                        handleAddressChange(e.target.value);
                      }}
                      required
                      className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                    />
                    {addressSuggestions.length > 0 && (
                      <ul className="bg-gray-700 text-gray-100 rounded-md mt-2 max-h-40 overflow-y-auto">
                        {addressSuggestions.map((suggestion, index) => (
                          <li
                            key={index}
                            className="p-2 cursor-pointer hover:bg-gray-600"
                            onClick={() => {
                              setFormData({
                                ...formData,
                                address: suggestion.display_name,
                              });
                              setAddressSuggestions([]);
                            }}
                          >
                            {suggestion.display_name}
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                ) : (
                  <input
                    type="text"
                    id="manualAddress"
                    name="address"
                    value={formData.address}
                    onChange={handleChange}
                    required
                    className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                  />
                )}
                <button
                  type="button"
                  className="mt-2 text-green-400 underline"
                  onClick={() => {
                    setManualAddress(!manualAddress);
                    setAddressSuggestions([]);
                  }}
                >
                  {manualAddress
                    ? "Use autocomplete"
                    : "Enter address manually"}
                </button>
              </div>
              <div>
                <label
                  htmlFor="email"
                  className="block text-sm font-medium text-gray-300"
                >
                  Email
                </label>
                <input
                  type="email"
                  id="email"
                  name="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                  className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
                />
              </div>
            </>
          )}
          {isLogin && (
            <div>
              <label
                htmlFor="usernameOrEmail"
                className="block text-sm font-medium text-gray-300"
              >
                Username or Email
              </label>
              <input
                type="text"
                id="usernameOrEmail"
                name="usernameOrEmail"
                value={formData.usernameOrEmail}
                onChange={handleChange}
                required
                className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
              />
            </div>
          )}
          <div>
            <label
              htmlFor="password"
              className="block text-sm font-medium text-gray-300"
            >
              Password
            </label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              required
              className="w-full mt-1 p-2 bg-gray-700 text-gray-100 rounded-md border border-gray-600 focus:ring focus:ring-green-400 focus:outline-none"
            />
          </div>
          {error && (
            <p className="text-sm text-red-400 text-center">{error}</p>
          )}
          <button
            type="submit"
            className="w-full py-2 bg-green-400 text-gray-900 font-semibold rounded-md shadow-md hover:bg-green-300 transition"
          >
            {isLogin ? "Log In" : "Register"}
          </button>
        </form>
        <button
          onClick={() => setIsLogin(!isLogin)}
          className="mt-4 text-sm text-green-400 underline hover:text-green-300 transition"
        >
          {isLogin
            ? "Need to register? Sign up here."
            : "Already have an account? Log in here."}
        </button>
      </div>
    </div>
  );
};

export default Register;
