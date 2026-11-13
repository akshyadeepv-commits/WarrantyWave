export type UserRole = 'ROLE_ADMIN' | 'ROLE_CLAIMS_OFFICER' | 'ROLE_FINANCE_OFFICER' | 'ROLE_CUSTOMER_REP';

export interface User {
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
  phoneNumber?: string;
  active: boolean;
}

export interface AuthRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  type: string;
  id: number;
  username: string;
  fullName: string;
  email: string;
  role: UserRole;
}
