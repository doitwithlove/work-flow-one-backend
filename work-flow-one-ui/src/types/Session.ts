export type Session = {
  accessToken: string;
  refreshToken: string;
  expiresAt: number;
  userId: string;
  username: string;
  roles: string[];
};
