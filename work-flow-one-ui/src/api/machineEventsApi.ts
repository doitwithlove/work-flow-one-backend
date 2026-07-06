import { apiClient } from './client';
import { MachineEvent, MachineEventType } from '../types/manufacturing';

export async function fetchMachineEvents() {
  const response = await apiClient.get<MachineEvent[]>('/machine-events');
  return response.data;
}

export async function fetchMachineEventsByPart(partId: string) {
  const response = await apiClient.get<MachineEvent[]>(`/machine-events/part/${partId}`);
  return response.data;
}

export async function createMachineEvent(payload: {
  machineId: string;
  partId: string;
  eventType: MachineEventType;
  status: string;
  payload?: Record<string, unknown>;
}) {
  const response = await apiClient.post<MachineEvent>('/machine-events', payload);
  return response.data;
}
