import { AuthResponse, LoginRequest } from '../types/api';

const API_BASE_URL = 'http://localhost:8082/api';

class AuthService {
  private tokenKey = 'gefrierschrank_token';
  private userKey = 'gefrierschrank_user';

  async login(credentials: LoginRequest): Promise<AuthResponse> {
    const response = await fetch(`${API_BASE_URL}/auth/signin`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      throw new Error('Login failed');
    }

    const authData: AuthResponse = await response.json();
    
    // Store token and user data
    localStorage.setItem(this.tokenKey, authData.accessToken);
    localStorage.setItem(this.userKey, JSON.stringify({
      id: authData.id,
      username: authData.username,
      email: authData.email,
      roles: authData.roles,
    }));

    return authData;
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getUser(): { id: number; username: string; email: string; roles: string[] } | null {
    const userStr = localStorage.getItem(this.userKey);
    return userStr ? JSON.parse(userStr) : null;
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  isAdmin(): boolean {
    const user = this.getUser();
    return user?.roles?.includes('ROLE_ADMIN') || false;
  }

  getAuthHeaders(): Record<string, string> {
    const token = this.getToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }
}

export const authService = new AuthService();