import axios from 'axios';
import { Timetable } from '@/types/timetable';

// Use empty string - Next.js will proxy to localhost:8080
const API_BASE_URL = '';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const timetableApi = {
  // Get demo data list
  getDemoDataList: async (): Promise<string[]> => {
    const response = await api.get('/demo-data');
    return response.data;
  },

  // Get specific demo data
  getDemoData: async (demoDataId: string): Promise<Timetable> => {
    const response = await api.get(`/demo-data/${demoDataId}`);
    return response.data;
  },

  // Solve timetable
  solveTimetable: async (timetable: Timetable): Promise<string> => {
    const response = await api.post('/timetables', timetable);
    return response.data;
  },

  // Get timetable status
  getTimetable: async (jobId: string): Promise<Timetable> => {
    const response = await api.get(`/timetables/${jobId}`);
    return response.data;
  },

  // Stop solving
  stopSolving: async (jobId: string): Promise<Timetable> => {
    const response = await api.delete(`/timetables/${jobId}`);
    return response.data;
  },
};