import { apiClient } from './client';
import { DashboardSummary } from '../types/manufacturing';

export async function fetchDashboardSummary() {
  const response = await apiClient.get<DashboardSummary>('/dashboard/summary');
  return response.data;
}
