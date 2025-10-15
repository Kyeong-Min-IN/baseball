import axios from 'axios';

// Centralized API client
// Adjust baseURL if your backend runs elsewhere
const api = axios.create({
  withCredentials: false,
  headers: {
    'Content-Type': 'application/json'
  }
});

export default api;

