import { apiClient } from './client';
import { Operator } from '../types/manufacturing';

export async function fetchOperators() {
  const response = await apiClient.get<Operator[]>('/operators');
  return response.data;
}

export async function fetchOperator(id: string) {
  const response = await apiClient.get<Operator>(`/operators/${id}`);
  return response.data;
}

export async function createOperator(payload: Omit<Operator, 'id' | 'createdAt' | 'updatedAt'>) {
  const response = await apiClient.post<Operator>('/operators', payload);
  return response.data;
}

export async function updateOperator(id: string, payload: Partial<Omit<Operator, 'id' | 'employeeCode' | 'createdAt' | 'updatedAt'>>) {
  const response = await apiClient.put<Operator>(`/operators/${id}`, payload);
  return response.data;
}

export async function deleteOperator(id: string) {
  await apiClient.delete(`/operators/${id}`);
}
