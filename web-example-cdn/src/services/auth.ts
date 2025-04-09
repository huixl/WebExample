import { LoginRequest, LoginResponse, RegisterRequest, RegisterResponse } from '../types/auth';
import axios from '../utils/axios';

export const authService = {
    async login(data: LoginRequest): Promise<LoginResponse> {
        const response = await axios.post<LoginResponse>('/sso/login', data);
        return response.data;
    },

    async register(data: RegisterRequest): Promise<RegisterResponse> {
        const response = await axios.post<RegisterResponse>('/sso/register', data);
        return response.data;
    },

    async logout(): Promise<void> {
        await axios.post('/sso/logout');
    }
}; 