import { MachineStatus, PartStatus, TestStatus } from './manufacturing';
import { OperatorMachineSession } from './manufacturing';
import { PartSummary } from './manufacturing';
import { ProductivitySummary } from './manufacturing';
import { TestResult } from './manufacturing';
import { UserResponse } from './UserResponse';

export type MachineProgress = {
  machineId: string;
  machineCode: string;
  machineName: string;
  status: MachineStatus;
  currentPartId: string | null;
  currentPartNumber: string | null;
  currentOperatorId: string | null;
  currentOperatorName: string | null;
  currentStepName: string | null;
  progressPercentage: number;
  lastSignalAt: string | null;
};

export type ProcessStepProgress = {
  stepId: string;
  stepNumber: number;
  stepName: string;
  totalParts: number;
  completedParts: number;
  inProcessParts: number;
  failedParts: number;
  waitingParts: number;
  progressPercentage: number;
};

export type SuperUserDashboardResponse = {
  totalMachines: number;
  runningMachines: number;
  idleMachines: number;
  errorMachines: number;
  stoppedMachines: number;
  totalParts: number;
  partsInProcess: number;
  partsWaitingForTest: number;
  partsPassed: number;
  partsFailed: number;
  partsReadyForNextPhase: number;
  activeOperators: number;
  activeSessions: number;
  productivitySummary: ProductivitySummary[];
  machineProgressList: MachineProgress[];
  processStepProgressList: ProcessStepProgress[];
  activeSessionList: OperatorMachineSession[];
  recentTestResults: TestResult[];
  failedParts: PartSummary[];
  partsWaitingForTestList: PartSummary[];
  operatorList: UserResponse[];
};

export type { MachineStatus, PartStatus, TestStatus, OperatorMachineSession, PartSummary, ProductivitySummary, TestResult, UserResponse };
