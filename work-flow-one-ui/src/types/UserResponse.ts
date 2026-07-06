export type UserResponse = {
  id: string;
  username: string;
  email: string;
  fullName: string | null;
  roles: string[];
  enabled: boolean;
  active: boolean;
  createdAt: string;
  phoneNumber: string | null;
  birthday: string | null;
  position: string | null;
  profilePictureUrl: string | null;
  socialContacts: Record<string, string>;
};
