import React, { useState } from "react";
import {
  Box,
  Typography,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  TextField,
  Button,
} from "@mui/material";

const sxStyles = {
  modalContent: {
    backgroundColor: "#1e293b",
    color: "#ffffff",
    padding: "20px",
    borderRadius: "8px",
    boxShadow: "0 4px 12px rgba(0, 0, 0, 0.3)",
    maxWidth: "400px",
    margin: "auto",
    marginTop: "10%",
  },
  categoryList: {
    maxHeight: "200px",
    overflowY: "auto",
    backgroundColor: "#2d3748",
    borderRadius: "8px",
    padding: "10px",
    marginBottom: "20px",
  },
  listItem: {
    "&:hover": {
      backgroundColor: "#4a5568",
    },
  },
  selected: {
    backgroundColor: "#4a5568 !important",
  },
  textField: {
    "& .MuiOutlinedInput-root": {
      backgroundColor: "#2d3748",
      color: "#ffffff",
      "& fieldset": { borderColor: "#8c52ff" },
      "&:hover fieldset": { borderColor: "#8c52ff" },
      "&.Mui-focused fieldset": { borderColor: "#8c52ff" },
    },
    "& input": { color: "#ffffff" },
    marginBottom: "15px",
  },
  applyButton: {
    backgroundColor: "#8c52ff",
    color: "#fff",
    "&:hover": { backgroundColor: "#10b981" },
    width: "100%",
  },
};

const CategoryPicker = ({ categories, onCategorySelect, onClose }) => {
  const [selectedCategory, setSelectedCategory] = useState(null);
  const [newCategory, setNewCategory] = useState("");

  const formatCategory = (category) => {
    if (!category) return "Uncategorized";
    return category.replace(/_/g, " ").replace(/\b\w/g, (char) => char.toUpperCase());
  };

  const handleCategoryClick = (category) => {
    setSelectedCategory(category);
  };

  const handleNewCategoryChange = (event) => {
    setNewCategory(event.target.value);
  };

  const handleApply = () => {
    const categoryToApply = newCategory
      ? {
          name: newCategory.trim().replace(/ /g, "_").toUpperCase(),
          color: "#10b981", // Default color for new categories, can be changed
        }
      : selectedCategory;

    if (categoryToApply) {
      onCategorySelect(categoryToApply);
    }
    onClose();
  };

  return (
    <Box sx={sxStyles.modalContent}>
      <Typography variant="h6" gutterBottom>
        Select or Add a Category
      </Typography>

      <Box sx={sxStyles.categoryList}>
        <List>
          {categories.map((category) => (
            <ListItem
              key={category.name}
              disablePadding
              sx={{
                ...sxStyles.listItem,
                ...(selectedCategory?.name === category.name && sxStyles.selected),
              }}
            >
              <ListItemButton onClick={() => handleCategoryClick(category)}>
                <ListItemText
                  primary={formatCategory(category.name)}
                  style={{ color: category.color }}
                />
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </Box>

      <TextField
        placeholder="Add a new category"
        value={newCategory}
        onChange={handleNewCategoryChange}
        fullWidth
        sx={sxStyles.textField}
      />

      <Button
        variant="contained"
        sx={sxStyles.applyButton}
        onClick={handleApply}
        disabled={!newCategory.trim() && !selectedCategory}
      >
        Apply
      </Button>
    </Box>
  );
};

export default CategoryPicker;
