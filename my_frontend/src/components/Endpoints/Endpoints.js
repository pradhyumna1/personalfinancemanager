import React from "react";

const Endpoint = ({ endpoint, categories, schema, description, transformData }) => {
  return (
    <div>
      <h3>{endpoint}</h3>
      <p>{description}</p>
      <p>Schema: {schema}</p>
      <ul>
        {categories && categories.map((category, index) => (
          <li key={index}>{category}</li>
        ))}
      </ul>
      {/* Display or transform data if necessary */}
    </div>
  );
};

export default Endpoint;
