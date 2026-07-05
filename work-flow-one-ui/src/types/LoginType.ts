export type ApiResponse<T> = {
  timestamp: string;
  status: number;
  message: string;
  data: T;
};
export type TokenResponse = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
};
export type UserResponse = {
  id: string;
  username: string;
  email: string;
  roles: string[];
  enabled: boolean;
  createdAt: string;
};
export type Session = {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
};

export type Notice = {
  tone: 'success' | 'error' | 'info';
  text: string;
};