import { apiClient } from './client';
import { PartHistory, PartStatus, PartSummary, TestStatus } from '../types/manufacturing';

export async function fetchParts() {
  const response = await apiClient.get<PartSummary[]>('/parts');
  return response.data;
}

export async function fetchPart(id: string) {
  const response = await apiClient.get<PartSummary>(`/parts/${id}`);
  return response.data;
}

export async function fetchPartHistory(id: string) {
  const response = await apiClient.get<PartHistory>(`/parts/${id}/history`);
  return response.data;
}

export async function createPart(payload: { partNumber: string; batchNumber: string; currentStepId?: string | null }) {
  const response = await apiClient.post<PartSummary>('/parts', payload);
  return response.data;
}

export async function updatePartStatus(
  id: string,
  payload: { status: PartStatus; testStatus?: TestStatus; currentMachineId?: string | null; currentStepId?: string | null },
) {
  const response = await apiClient.put<PartSummary>(`/parts/${id}/status`, payload);
  return response.data;
}

export async function movePartNext(id: string) {
  const response = await apiClient.post<PartSummary>(`/parts/${id}/move-next`);
  return response.data;
}
