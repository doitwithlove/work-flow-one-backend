export type UserResponse = {
  id: string;
  username: string;
  email: string;
  roles: string[];
  enabled: boolean;
  createdAt: string;
  phoneNumber: string | null;
  birthday: string | null;
  position: string | null;
  profilePictureUrl: string | null;
  socialContacts: Record<string, string>;
};
