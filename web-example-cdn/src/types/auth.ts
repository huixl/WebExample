export interface LoginRequest {
    identity: string;
    credential: string;
    loginType: string;
    rememberMe?: boolean;
}

export interface LoginResponse {
    success: boolean;
    message?: string;
    data?: {
        userId: number;
        avatarUrl: string;
        nickname?: string;
        token?: string;
    };
}

export interface RegisterRequest {
    username: string;
    email: string;
    phone: string;
    password: string;
    confirmPassword: string;
    loginType: string;
}

export interface RegisterResponse {
    success: boolean;
    message?: string;
}

export interface LoginInfoResponse {
    username: string;
    avatar: string;
}

export interface User {
    userId: number;
    username: string;
    nickname: string;
    avatarUrl: string;
} 