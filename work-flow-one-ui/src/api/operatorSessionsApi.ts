import { apiClient } from './client';
import { OperatorMachineSession } from '../types/manufacturing';

export async function startOperatorSession(payload: { operatorId: string; machineId: string; shiftId: string }) {
  const response = await apiClient.post<OperatorMachineSession>('/operator-sessions/start', payload);
  return response.data;
}

export async function endOperatorSession(payload: { sessionId: string }) {
  const response = await apiClient.post<OperatorMachineSession>('/operator-sessions/end', payload);
  return response.data;
}

export async function fetchActiveOperatorSessions() {
  const response = await apiClient.get<OperatorMachineSession[]>('/operator-sessions/active');
  return response.data;
}

export async function fetchOperatorSessionsByOperator(operatorId: string) {
  const response = await apiClient.get<OperatorMachineSession[]>(`/operator-sessions/operator/${operatorId}`);
  return response.data;
}

export async function fetchOperatorSessionsByMachine(machineId: string) {
  const response = await apiClient.get<OperatorMachineSession[]>(`/operator-sessions/machine/${machineId}`);
  return response.data;
}
