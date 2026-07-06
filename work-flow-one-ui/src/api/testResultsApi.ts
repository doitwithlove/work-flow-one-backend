import { apiClient } from './client';
import { TestResult, TestResultStatus } from '../types/manufacturing';

export async function fetchTestResults(partId: string) {
  const response = await apiClient.get<TestResult[]>(`/test-results/part/${partId}`);
  return response.data;
}

export async function createTestResult(payload: {
  partId: string;
  machineId: string;
  testType: string;
  expectedValue: number;
  actualValue: number;
  toleranceMin: number;
  toleranceMax: number;
  result: TestResultStatus;
}) {
  const response = await apiClient.post<TestResult>('/test-results', payload);
  return response.data;
}
