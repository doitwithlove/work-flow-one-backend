import { apiClient } from './client';
import { Shift } from '../types/manufacturing';

export async function fetchShifts() {
  const response = await apiClient.get<Shift[]>('/shifts');
  return response.data;
}

export async function fetchShift(id: string) {
  const response = await apiClient.get<Shift>(`/shifts/${id}`);
  return response.data;
}

export async function createShift(payload: Omit<Shift, 'id'>) {
  const response = await apiClient.post<Shift>('/shifts', payload);
  return response.data;
}
