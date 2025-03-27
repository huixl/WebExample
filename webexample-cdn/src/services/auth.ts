import { LoginInfoResponse, LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types/auth';
import axios from '../utils/axios';

export const authService = {
    async login(data: LoginRequest): Promise<LoginResponse> {
        const response = await axios.post<LoginResponse>('/api/login', data);
        return response.data;
    },

    async register(data: RegisterRequest): Promise<RegisterResponse> {
        const response = await axios.post<RegisterResponse>('/api/register', data);
        return response.data;
    },

    async getLoginInfo(): Promise<void> {
       await axios.get<LoginInfoResponse>('/api/test');
    },

    async logout(): Promise<void> {
        await axios.post('/logout');
    }
}; 