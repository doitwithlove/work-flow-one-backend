import { apiClient } from './client';
import { SuperUserDashboardResponse } from '../types/superUser';

export async function fetchSuperUserDashboard() {
  const response = await apiClient.get<SuperUserDashboardResponse>('/super-user/dashboard');
  return response.data;
}
