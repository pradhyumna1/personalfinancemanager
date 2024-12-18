import React from "react";
import PropTypes from "prop-types";
import styles from "./styles/CategoryChip.module.scss";

const CategoryChip = ({ categoryName, categoryColor, onClick }) => {
  return (
    <div
      className={styles.categoryChip}
      style={{
        backgroundColor: categoryColor,
        color: "#fff", // Always white text for contrast
        cursor: "pointer", // Add pointer cursor to indicate interactivity
      }}
      onClick={onClick} // Handle click events
    >
      {categoryName}
    </div>
  );
};

// Prop validation
CategoryChip.propTypes = {
  categoryName: PropTypes.string.isRequired,
  categoryColor: PropTypes.string.isRequired, // Ensures the color column is passed
  onClick: PropTypes.func, // Optional onClick handler
};

// Default props
CategoryChip.defaultProps = {
  onClick: null, // Default to null if no onClick handler is passed
};

export default CategoryChip;
