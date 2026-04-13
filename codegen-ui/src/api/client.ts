import axios from 'axios'

const baseURL = (import.meta.env.VITE_API_URL as string | undefined) ?? 'http://localhost:8080'

export const apiClient = axios.create({
  baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
})
