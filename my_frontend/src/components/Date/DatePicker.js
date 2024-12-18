import React, { useState, useRef, useEffect } from "react";
import { Box, Button, Typography, IconButton } from "@mui/material";
import styles from "./styles/DatePicker.module.scss";

// Helper to generate days of the week
const daysOfWeek = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];

// Helper to generate days in a month
const generateDaysInMonth = (year, month) => {
  const firstDayOfMonth = new Date(Date.UTC(year, month, 1)).getUTCDay();
  const days = new Date(Date.UTC(year, month + 1, 0)).getUTCDate();
  const daysArray = Array.from({ length: days }, (_, i) => i + 1);

  // Prepend empty slots for alignment
  const emptySlots = Array.from({ length: firstDayOfMonth }, () => null);

  return [...emptySlots, ...daysArray];
};

const DatePicker = ({ selectedDate, onDateChange, onClose }) => {
  // Ensure selectedDate is handled in UTC
  const initialDate = selectedDate
    ? new Date(Date.UTC(
        selectedDate.getUTCFullYear(),
        selectedDate.getUTCMonth(),
        selectedDate.getUTCDate()
      ))
    : new Date();

  const [currentDate, setCurrentDate] = useState(initialDate);
  const overlayRef = useRef();

  // Debugging: Log selectedDate and currentDate
  console.log("Parent's selectedDate (raw UTC):", selectedDate.toISOString());
  console.log("Local currentDate (UTC):", currentDate.toISOString());

  // Close the picker when clicking outside
  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (overlayRef.current && !overlayRef.current.contains(event.target)) {
        onClose();
      }
    };
    document.addEventListener("mousedown", handleOutsideClick);
    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
    };
  }, [onClose]);

  // Handle day selection
  const handleDayClick = (day) => {
    if (!day) return;
    const updatedDate = new Date(
      Date.UTC(currentDate.getUTCFullYear(), currentDate.getUTCMonth(), day)
    );
    onDateChange(updatedDate); // Notify parent
    onClose(); // Close after selection
  };

  // Handle month navigation
  const handleMonthChange = (direction) => {
    const updatedDate = new Date(
      Date.UTC(currentDate.getUTCFullYear(), currentDate.getUTCMonth() + direction, 1)
    );
    setCurrentDate(updatedDate);
  };

  // Handle year navigation
  const handleYearChange = (direction) => {
    const updatedDate = new Date(
      Date.UTC(currentDate.getUTCFullYear() + direction, currentDate.getUTCMonth(), 1)
    );
    setCurrentDate(updatedDate);
  };

  const daysInMonth = generateDaysInMonth(
    currentDate.getUTCFullYear(),
    currentDate.getUTCMonth()
  );

  return (
    <Box className={styles.overlay}>
      <Box className={styles.datePicker} ref={overlayRef}>
        {/* Header */}
        <Box className={styles.header}>
          <IconButton className={styles.navButton} onClick={() => handleYearChange(-1)}>
            &lt;&lt;
          </IconButton>
          <IconButton className={styles.navButton} onClick={() => handleMonthChange(-1)}>
            &lt;
          </IconButton>
          <Typography className={styles.currentMonthYear}>
            {currentDate.toLocaleString("default", {
              month: "long",
              year: "numeric",
              timeZone: "UTC",
            })}
          </Typography>
          <IconButton className={styles.navButton} onClick={() => handleMonthChange(1)}>
            &gt;
          </IconButton>
          <IconButton className={styles.navButton} onClick={() => handleYearChange(1)}>
            &gt;&gt;
          </IconButton>
        </Box>

        {/* Days of the week */}
        <Box className={styles.weekdays}>
          {daysOfWeek.map((day, index) => (
            <Typography key={index} className={styles.weekday}>
              {day}
            </Typography>
          ))}
        </Box>

        {/* Days grid */}
        <Box className={styles.daysGrid}>
          {daysInMonth.map((day, index) => {
            const isHighlighted =
              selectedDate &&
              day === selectedDate.getUTCDate() &&
              currentDate.getUTCMonth() === selectedDate.getUTCMonth() &&
              currentDate.getUTCFullYear() === selectedDate.getUTCFullYear();

            // Debugging: Log each day's comparison
            console.log(
              `Day: ${day}, Selected Day: ${selectedDate?.getUTCDate()}, Match: ${isHighlighted}`
            );

            return (
              <Button
                key={index}
                onClick={() => handleDayClick(day)}
                className={`${styles.dayButton} ${isHighlighted ? styles.selected : ""}`}
                disabled={!day} // Disable empty slots
              >
                {day || ""}
              </Button>
            );
          })}
        </Box>
      </Box>
    </Box>
  );
};

export default DatePicker;
