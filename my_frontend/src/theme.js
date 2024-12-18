// src/theme.js
import { createTheme } from '@mui/material/styles';

const theme = createTheme({
  palette: {
    mode: 'dark', // Enables dark mode
    primary: {
      main: '#4CAF50', // Your primary green
    },
    secondary: {
      main: '#6366F1', // Your purple shade
    },
    background: {
      default: '#0F0F0F', // Matches Tailwind's dark color
      paper: '#1A1A1A',
    },
    text: {
      primary: '#FFFFFF',
      secondary: '#B3B3B3',
    },
  },
  typography: {
    fontFamily: 'Inter, Arial, sans-serif',
  },
});

export default theme;
