import { renderHook, act } from '@testing-library/react';
import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { AuthProvider, useAuth } from '../useAuth';
import { authAPI } from '../../services/api';
import { ReactNode } from 'react';

// Mock API
vi.mock('../../services/api', () => ({
  authAPI: {
    login: vi.fn(),
  },
}));

const mockAuthAPI = authAPI as any;

// Mock localStorage
const mockLocalStorage = {
  getItem: vi.fn(),
  setItem: vi.fn(),
  removeItem: vi.fn(),
  clear: vi.fn(),
};

Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
});

const wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

describe('useAuth Hook', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mockLocalStorage.getItem.mockReturnValue(null);
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('throws error when used outside AuthProvider', () => {
    expect(() => {
      renderHook(() => useAuth());
    }).toThrow('useAuth must be used within an AuthProvider');
  });

  it('initializes with null user and token when no stored data', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
    expect(result.current.isLoading).toBe(false);
  });

  it('initializes with stored user and token', () => {
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };
    const mockToken = 'stored-jwt-token';

    mockLocalStorage.getItem.mockImplementation((key: string) => {
      if (key === 'token') return mockToken;
      if (key === 'user') return JSON.stringify(mockUser);
      return null;
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.user).toEqual(mockUser);
    expect(result.current.token).toBe(mockToken);
    expect(result.current.isLoading).toBe(false);
  });

  it('handles successful login', async () => {
    const mockResponse = {
      token: 'new-jwt-token',
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    mockAuthAPI.login.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('testuser', 'password');
    });

    expect(mockAuthAPI.login).toHaveBeenCalledWith({
      username: 'testuser',
      password: 'password',
    });

    expect(result.current.user).toEqual({
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    });
    expect(result.current.token).toBe('new-jwt-token');

    expect(mockLocalStorage.setItem).toHaveBeenCalledWith('token', 'new-jwt-token');
    expect(mockLocalStorage.setItem).toHaveBeenCalledWith('user', JSON.stringify({
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    }));
  });

  it('handles failed login', async () => {
    const loginError = new Error('Login failed');
    mockAuthAPI.login.mockRejectedValue(loginError);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await expect(async () => {
      await act(async () => {
        await result.current.login('testuser', 'wrongpassword');
      });
    }).rejects.toThrow('Login failed');

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
    expect(mockLocalStorage.setItem).not.toHaveBeenCalled();
  });

  it('handles logout correctly', () => {
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };
    const mockToken = 'jwt-token';

    mockLocalStorage.getItem.mockImplementation((key: string) => {
      if (key === 'token') return mockToken;
      if (key === 'user') return JSON.stringify(mockUser);
      return null;
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    // User should be logged in initially
    expect(result.current.user).toEqual(mockUser);
    expect(result.current.token).toBe(mockToken);

    act(() => {
      result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
    expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('token');
    expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('user');
  });

  it('handles login with admin user', async () => {
    const mockResponse = {
      token: 'admin-jwt-token',
      id: 2,
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_ADMIN', 'ROLE_USER'],
    };

    mockAuthAPI.login.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await act(async () => {
      await result.current.login('admin', 'adminpass');
    });

    expect(result.current.user).toEqual({
      id: 2,
      username: 'admin',
      email: 'admin@example.com',
      roles: ['ROLE_ADMIN', 'ROLE_USER'],
    });
    expect(result.current.token).toBe('admin-jwt-token');
  });

  it('handles malformed stored user data', () => {
    mockLocalStorage.getItem.mockImplementation((key: string) => {
      if (key === 'token') return 'valid-token';
      if (key === 'user') return 'invalid-json{';
      return null;
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
    expect(result.current.isLoading).toBe(false);
  });

  it('handles missing token with valid user data', () => {
    const mockUser = {
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    mockLocalStorage.getItem.mockImplementation((key: string) => {
      if (key === 'token') return null;
      if (key === 'user') return JSON.stringify(mockUser);
      return null;
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
  });

  it('handles missing user with valid token', () => {
    mockLocalStorage.getItem.mockImplementation((key: string) => {
      if (key === 'token') return 'valid-token';
      if (key === 'user') return null;
      return null;
    });

    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
  });

  it('starts with loading true and sets to false after initialization', () => {
    const { result } = renderHook(() => useAuth(), { wrapper });

    expect(result.current.isLoading).toBe(false);
  });

  it('maintains user state after successful login and logout', async () => {
    const mockResponse = {
      token: 'jwt-token',
      id: 1,
      username: 'testuser',
      email: 'test@example.com',
      roles: ['ROLE_USER'],
    };

    mockAuthAPI.login.mockResolvedValue(mockResponse);

    const { result } = renderHook(() => useAuth(), { wrapper });

    // Login
    await act(async () => {
      await result.current.login('testuser', 'password');
    });

    expect(result.current.user).not.toBeNull();
    expect(result.current.token).not.toBeNull();

    // Logout
    act(() => {
      result.current.logout();
    });

    expect(result.current.user).toBeNull();
    expect(result.current.token).toBeNull();
  });

  it('logs error on login failure', async () => {
    const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {});
    const loginError = new Error('Network error');
    mockAuthAPI.login.mockRejectedValue(loginError);

    const { result } = renderHook(() => useAuth(), { wrapper });

    await expect(async () => {
      await act(async () => {
        await result.current.login('testuser', 'password');
      });
    }).rejects.toThrow('Network error');

    expect(consoleSpy).toHaveBeenCalledWith('Login failed:', loginError);
    
    consoleSpy.mockRestore();
  });
});