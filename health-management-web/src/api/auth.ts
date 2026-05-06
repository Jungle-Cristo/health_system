import request from '../utils/request';

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  phone?: string;
}

export interface LoginRequest {
  account: string;
  password?: string;
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  userId: number;
  username: string;
}

export function register(data: RegisterRequest) {
  return request.post<any, { data: AuthResponse }>('/auth/register', data);
}

export function login(data: LoginRequest) {
  return request.post<any, { data: AuthResponse }>('/auth/login', data);
}

export function logout() {
  return request.post('/auth/logout');
}

export function clearAuth() {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('username');
}
