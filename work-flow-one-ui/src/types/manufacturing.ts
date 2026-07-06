export type PartStatus =
  | 'CREATED'
  | 'WAITING'
  | 'IN_PROCESS'
  | 'TEST_PENDING'
  | 'PASSED'
  | 'FAILED'
  | 'REWORK_REQUIRED'
  | 'READY_FOR_NEXT_PHASE'
  | 'COMPLETED';

export type MachineStatus = 'IDLE' | 'RUNNING' | 'STOPPED' | 'ERROR' | 'MAINTENANCE';
export type TestResultStatus = 'PASS' | 'FAIL';
export type TestStatus = 'PENDING' | 'PASS' | 'FAIL' | 'NOT_REQUIRED';
export type MachineEventType = 'PROCESS_STARTED' | 'PROCESS_COMPLETED' | 'STATUS_CHANGED' | 'HEARTBEAT' | 'ERROR' | 'QUALITY_RECORDED';

export type MachineSummary = {
  id: string;
  machineCode: string;
  name: string;
  type: string;
  status: MachineStatus;
  lastSignalAt: string;
  currentOperatorId: string | null;
  currentOperatorName: string | null;
  activeSessionStartTime: string | null;
};

export type PartSummary = {
  id: string;
  partNumber: string;
  batchNumber: string;
  currentStepId: string | null;
  currentMachineId: string | null;
  status: PartStatus;
  testStatus: TestStatus;
  createdAt: string;
  updatedAt: string;
};

export type ProcessStep = {
  id: string;
  stepNumber: number;
  name: string;
  machineType: string;
  requiredTestType: string;
  nextStepId: string | null;
};

export type MachineEvent = {
  id: string;
  machineId: string;
  partId: string;
  operatorId: string | null;
  operatorSessionId: string | null;
  eventType: MachineEventType;
  status: string;
  payload: Record<string, unknown>;
  receivedAt: string;
};

export type TestResult = {
  id: string;
  partId: string;
  machineId: string;
  testType: string;
  expectedValue: number;
  actualValue: number;
  toleranceMin: number;
  toleranceMax: number;
  result: TestResultStatus;
  testedAt: string;
};

export type DashboardSummary = {
  runningMachines: number;
  idleMachines: number;
  errorMachines: number;
  partsInProcess: number;
  partsPassed: number;
  partsFailed: number;
  partsReadyForNextPhase: number;
};

export type PartHistory = {
  part: PartSummary;
  machineEvents: MachineEvent[];
  testResults: TestResult[];
};

export type Operator = {
  id: string;
  employeeCode: string;
  firstName: string;
  lastName: string;
  role: string;
  skillLevel: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type Shift = {
  id: string;
  name: string;
  startTime: string;
  endTime: string;
  targetOutput: number;
  active: boolean;
};

export type OperatorMachineSession = {
  id: string;
  operatorId: string;
  operatorName: string;
  employeeCode: string;
  machineId: string;
  machineName: string;
  shiftId: string;
  shiftName: string;
  loginTime: string;
  logoutTime: string | null;
  status: 'ACTIVE' | 'CLOSED';
};

export type ProductivitySummary = {
  operatorId: string;
  operatorName: string;
  employeeCode: string;
  shiftId: string;
  shiftName: string;
  machineId: string;
  machineName: string;
  totalPartsProcessed: number;
  passedParts: number;
  failedParts: number;
  reworkParts: number;
  totalRuntimeMinutes: number;
  totalDowntimeMinutes: number;
  qualityRate: number;
  productionRate: number;
  machineUtilization: number;
  productivityScore: number;
  calculatedAt: string;
};
