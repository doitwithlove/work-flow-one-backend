import { apiClient } from './client';
import { MachineStatus, MachineSummary } from '../types/manufacturing';

export async function fetchMachines() {
  const response = await apiClient.get<MachineSummary[]>('/machines');
  return response.data;
}

export async function createMachine(payload: {
  machineCode: string;
  name: string;
  type: string;
  status: MachineStatus;
}) {
  const response = await apiClient.post<MachineSummary>('/machines', payload);
  return response.data;
}

export async function updateMachineStatus(id: string, status: MachineStatus) {
  const response = await apiClient.put<MachineSummary>(`/machines/${id}/status`, { status });
  return response.data;
}
