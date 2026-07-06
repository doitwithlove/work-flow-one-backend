import { apiClient } from './client';
import { ProductivitySummary } from '../types/manufacturing';

export async function fetchProductivitySummary() {
  const response = await apiClient.get<ProductivitySummary[]>('/productivity/summary');
  return response.data;
}

export async function fetchProductivityByOperator(operatorId: string) {
  const response = await apiClient.get<ProductivitySummary[]>(`/productivity/by-operator/${operatorId}`);
  return response.data;
}

export async function fetchProductivityByShift(shiftId: string) {
  const response = await apiClient.get<ProductivitySummary[]>(`/productivity/by-shift/${shiftId}`);
  return response.data;
}

export async function fetchProductivityByMachine(machineId: string) {
  const response = await apiClient.get<ProductivitySummary[]>(`/productivity/by-machine/${machineId}`);
  return response.data;
}
